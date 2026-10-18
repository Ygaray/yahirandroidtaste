package io.github.ygaray.yahirandroidtaste.component

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Build
import android.util.Log
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import org.osmdroid.config.Configuration

private const val USER_AGENT_HEADER = "User-Agent"
private const val LOG_TAG = "YatOsmdroid"
private const val OSM_TILE_POLICY_URL = "https://operations.osmfoundation.org/policies/tiles/"

/** The hub's own prefs file for osmdroid; the consumer owns the request-property slots in it. */
internal const val OSMDROID_PREFS_NAME = "yahirandroidtaste_osmdroid"

/**
 * Boolean the hub owns: set once a placeholder scan has completed on this install. It does not
 * suppress the first scan of each process (a blocked fetch can re-cache a placeholder, so every
 * process looks once); it only stops later calls in the same process from retrying after a scan has
 * succeeded. A failed scan leaves it unset so the next call retries.
 */
internal const val PREF_PLACEHOLDER_PURGED = "placeholder_tiles_purged"

private const val TILE_CACHE_DB_NAME = "cache.db"

/** True once this process has done (or started) its first placeholder scan. */
private val placeholderScanStarted = AtomicBoolean(false)

/** True while a placeholder scan is running on the purge thread (at most one at a time). */
private val placeholderScanRunning = AtomicBoolean(false)

/** Runs each scan on its own short-lived daemon thread so the main thread never waits on SQLite. */
private val backgroundPurgeExecutor = Executor { task ->
    Thread(task, "yat-osm-placeholder-purge").apply { isDaemon = true }.start()
}

/** Where [configureOsmdroid] runs the placeholder scan; tests swap in a direct executor. */
@Volatile internal var placeholderPurgeExecutor: Executor = backgroundPurgeExecutor

/** Test hook: makes the next [configureOsmdroid] behave like the first call of a new process. */
internal fun resetPlaceholderScanForTests() {
    placeholderScanStarted.set(false)
}

/** Test hook: restores the production (background thread) scan executor. */
internal fun resetPlaceholderPurgeExecutorForTests() {
    placeholderPurgeExecutor = backgroundPurgeExecutor
}

/** True once this process has run [Configuration.load] (at most once per process, POL-03). */
private val osmdroidLoaded = AtomicBoolean(false)

/**
 * Guards the load-once decision and the request-property edits against concurrent hub callers only;
 * osmdroid's own tile threads never take it (see the precondition on [configureOsmdroid]).
 */
private val osmdroidLoadLock = Any()

/** Test hook: makes the next [configureOsmdroid] load the configuration again. */
internal fun resetOsmdroidLoadForTests() {
    osmdroidLoaded.set(false)
}

/**
 * Prefix of the application-id class the OSM tile server blocks outright (the Android template
 * placeholder ids). The single occurrence of this literal in main code: it only detects the
 * blocked class so a warning can be logged, it is never used as an agent.
 */
internal const val BLOCKED_PLACEHOLDER_PACKAGE_PREFIX = "com.example."

/**
 * HUBW-02 D-06 — the hub's osmdroid tile-policy configuration step.
 *
 * osmdroid's [Configuration] is process-global: every consumer app that uses the
 * PlaceMapPicker map surface must have this called (from
 * [org.osmdroid.config.Configuration.getInstance]) before any `MapView` is constructed
 * (Plan 02's PlaceMapPicker does so on first composition), so the hub configures it here from a
 * caller-supplied user agent rather than leaving each consumer to remember to.
 *
 * This is the hub's compliance point for the OSM tile usage policy
 * (https://operations.osmfoundation.org/policies/tiles/) — three rules this function exists to
 * serve:
 *  1. **Identifying User-Agent.** [configureOsmdroid] always sets a non-blank `userAgentValue`
 *     ([resolveOsmdroidUserAgent] replaces a blank agent, or a bare package name, with
 *     `<packageName>/<versionName> (Android <release>)`; a blank agent or a placeholder application
 *     id logs one warning, a bare non-placeholder package name is expanded silently), never
 *     osmdroid's library-default agent. The resolved agent is also injected as the `User-Agent` request
 *     property right after the single per-process `load()`, and re-applied without a reload on
 *     later calls when missing or different, because the MAPNIK tile source normalizes the agent
 *     to `<packageName>/<versionCode>` and ignores `userAgentValue`. `load()` is called at most
 *     once per process because it clears and refills a plain HashMap that tile threads iterate.
 *     **Precondition:** that guarantee covers hub callers only. The lock below serializes
 *     [configureOsmdroid] against itself, not against osmdroid's own tile download threads, which
 *     iterate the map without a lock. The first call per process is therefore safe only if no
 *     osmdroid `MapView` (or other osmdroid user, such as a consumer's own `Configuration.load`)
 *     is already running in the process; call [configureOsmdroid] before any osmdroid map exists.
 *     Callers should pass an identifying agent of the form `AppName/version (+contact)`.
 *  2. **Honor tile-server cache headers.** osmdroid's expiry-override handling is left at its
 *     default here — this function never calls an API that overrides cache expiry.
 *  3. **No bulk or offline tile pre-fetching.** This function calls no bulk-download or
 *     cache-manager API; it only configures where tiles are written once fetched. It also clears
 *     placeholder tiles that earlier blocked fetches cached ([purgePlaceholderTiles]): on the
 *     first call of each process (a full `tiles` scan, run on a background thread so the main
 *     thread never waits on it) and, on later calls, only while the install flag
 *     [PREF_PLACEHOLDER_PURGED] is still unset. That purge deletes only rows matching the
 *     placeholder fingerprint (size AND md5), is not keyed on a user-agent change, and uses no
 *     cache-manager or whole-cache wipe API.
 *
 * `osmdroidBasePath` and `osmdroidTileCache` are redirected to the host app's private `cacheDir`
 * (never shared external storage) after the one-time `load()`, so no storage permission is
 * required, tiles are written inside the app sandbox, and the OS is free to evict them under
 * storage pressure. The redirect is in memory: osmdroid's own first-run `load()` may still resolve
 * its default storage paths, create those directories and persist them to the hub's prefs before
 * the hub overrides them, so the persisted paths can differ from the in-memory ones.
 *
 * Imports only `android.`, `java.io.` and `org.osmdroid.` symbols (hub one-way-dependency
 * invariant).
 */
internal fun configureOsmdroid(context: Context, userAgent: String) {
    val appContext = context.applicationContext
    val prefs = appContext.getSharedPreferences(OSMDROID_PREFS_NAME, Context.MODE_PRIVATE)
    val resolvedAgent =
        resolveOsmdroidUserAgent(
            userAgent,
            appContext.packageName,
            hostVersionName(appContext),
            Build.VERSION.RELEASE,
        )
    synchronized(osmdroidLoadLock) {
        val configuration = Configuration.getInstance()
        if (!osmdroidLoaded.get()) {
            // load() runs at most once per process: it clears and refills the plain-HashMap request
            // properties that tile download threads iterate. It also resets userAgentValue, so the
            // agent and the header are set after it. A load that throws leaves the flag false.
            Configuration.getInstance().load(appContext, prefs)
            osmdroidLoaded.set(true)
        }
        if (configuration.userAgentValue != resolvedAgent) {
            configuration.userAgentValue = resolvedAgent
        }
        applyUserAgentHeader(configuration.additionalHttpRequestProperties, resolvedAgent)
    }
    if (sanitizeUserAgent(userAgent).isEmpty() || isBlockedClassUserAgent(resolvedAgent)) {
        Log.w(
            LOG_TAG,
            "OSM blocks blank and placeholder user agents; tiles may be refused. " +
                "Using \"$resolvedAgent\"; pass an agent of the form AppName/version (+contact). " +
                "Policy: $OSM_TILE_POLICY_URL",
        )
    }
    val basePath = File(appContext.cacheDir, "osmdroid")
    Configuration.getInstance().osmdroidBasePath = basePath
    val tileCache = File(basePath, "tiles")
    Configuration.getInstance().osmdroidTileCache = tileCache
    // Evaluated BEFORE the install-flag test: a second call in the same process must not rescan.
    val firstScanThisProcess = !placeholderScanStarted.getAndSet(true)
    if (firstScanThisProcess || !prefs.getBoolean(PREF_PLACEHOLDER_PURGED, false)) {
        schedulePlaceholderScan(File(tileCache, TILE_CACHE_DB_NAME), prefs)
    }
}

/**
 * Runs the placeholder scan off the calling thread (CR-01): [configureOsmdroid] is called from the
 * `AndroidView` factory on the main thread, and the scan opens SQLite, hashes candidate blobs and
 * deletes rows. At most one scan runs at a time; a call that arrives while one is running is
 * dropped (the next call or process rescans).
 *
 * The scan cannot be ordered strictly before the `MapView` opens `cache.db` without making the main
 * thread wait for it, so it does not try. It is safe to overlap because the scan uses its own SQLite
 * connection, and SQLite's file locking serializes it against the tile writer: a lock timeout
 * surfaces as a [SQLiteException], which [purgePlaceholderTiles] reports as `-1`, so the install
 * flag stays unset and the scan is retried later. The delete only ever removes a row whose bytes are
 * the placeholder, so the worst overlap outcome is that a placeholder cached after the scan read it
 * survives until the next scan. Nothing here throws into map creation.
 */
private fun schedulePlaceholderScan(dbFile: File, prefs: SharedPreferences) {
    if (!placeholderScanRunning.compareAndSet(false, true)) return
    try {
        placeholderPurgeExecutor.execute {
            try {
                if (purgePlaceholderTiles(dbFile) >= 0) {
                    prefs.edit().putBoolean(PREF_PLACEHOLDER_PURGED, true).apply()
                }
            } catch (_: RuntimeException) {
                // best effort: a failed scan is retried on the next call and never breaks map creation
            } finally {
                placeholderScanRunning.set(false)
            }
        }
    } catch (_: RuntimeException) {
        // the executor refused the task: nothing ran, so release the single-scan guard
        placeholderScanRunning.set(false)
    }
}

/**
 * Makes [requestProperties] carry exactly one `User-Agent` entry equal to [resolvedAgent], in
 * memory only (the consumer owns the persisted slot, the hub never writes it). The map is touched
 * only when needed: case variants are removed only when present, and the header is put only when
 * missing or different (a put on an existing key is not a structural modification, so a tile
 * thread iterating the map is not disturbed).
 */
private fun applyUserAgentHeader(requestProperties: MutableMap<String, String>, resolvedAgent: String) {
    val variants =
        requestProperties.keys.filter {
            it != USER_AGENT_HEADER && it.equals(USER_AGENT_HEADER, ignoreCase = true)
        }
    variants.forEach { requestProperties.remove(it) }
    if (requestProperties[USER_AGENT_HEADER] != resolvedAgent) {
        requestProperties[USER_AGENT_HEADER] = resolvedAgent
    }
}

private fun hostVersionName(context: Context): String? =
    try {
        context.packageManager
            .getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            .versionName
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

/** Removes ISO control characters (CR, LF, NUL, ...) then trims. */
private fun sanitizeUserAgent(userAgent: String): String =
    userAgent.filterNot { it.isISOControl() }.trim()

/**
 * Returns the sanitized [userAgent] (ISO control characters removed, then trimmed), or the honest
 * fallback `<packageName>/<versionName> (Android <release>)` when the result is blank or is the
 * host's own bare [packageName]. Used by [configureOsmdroid] to guarantee osmdroid's tile requests
 * never carry a blank or control-character User-Agent (OSM tile usage policy).
 */
internal fun resolveOsmdroidUserAgent(
    userAgent: String,
    packageName: String,
    versionName: String?,
    androidRelease: String,
): String {
    val sanitized = sanitizeUserAgent(userAgent)
    return if (sanitized.isEmpty() || sanitized == packageName) {
        buildFallbackUserAgent(packageName, versionName, androidRelease)
    } else {
        sanitized
    }
}

/** `<packageName>/<versionName> (Android <release>)`; a null or blank [versionName] becomes `0`. */
internal fun buildFallbackUserAgent(
    packageName: String,
    versionName: String?,
    androidRelease: String,
): String = "$packageName/${versionName?.trim().orEmpty().ifEmpty { "0" }} (Android $androidRelease)"

/**
 * Byte length of the "access blocked" placeholder tile the OSM server answers with. A content
 * fingerprint of a public placeholder image, not a security control.
 */
internal const val PLACEHOLDER_TILE_SIZE = 6987

/** md5 (lowercase hex) of the blocked placeholder tile; together with the size it is the fingerprint. */
internal const val PLACEHOLDER_TILE_MD5 = "c069a15b2cc2d6b6f527ad09eb93c61a"

/**
 * POL-03 — deletes the cached blocked placeholder tiles from an osmdroid `SqlTileWriter` database
 * ([dbFile], table `tiles`). A blocked fetch caches the placeholder and re-stamps a fresh expiry on
 * it, so without this an app that fixes its agent would keep showing blocked tiles for a week.
 *
 * Only rows whose tile has exactly [sizeBytes] bytes AND the md5 [md5Hex] are deleted; a size-only
 * match and every real tile survive. Best-effort and silent: returns the number of deleted rows,
 * `0` when there is nothing to do (including a missing file, which is never created) and `-1` when
 * the scan could not run (unreadable file, no `tiles` table, lock error). Never throws.
 *
 * Bound arguments only. Android binds every argument as text, so the length is compared through
 * `CAST(? AS INTEGER)`; an uncast text argument never equals an integer length.
 */
internal fun purgePlaceholderTiles(
    dbFile: File,
    sizeBytes: Int = PLACEHOLDER_TILE_SIZE,
    md5Hex: String = PLACEHOLDER_TILE_MD5,
): Int {
    if (!dbFile.isFile) return 0
    var db: SQLiteDatabase? = null
    return try {
        // A no-op error handler: the default one DELETES a database it finds corrupt, and this
        // purge must never remove the cache file itself.
        db =
            SQLiteDatabase.openDatabase(
                dbFile.path,
                null,
                SQLiteDatabase.OPEN_READWRITE,
                DatabaseErrorHandler { },
            )
        val matches = mutableListOf<Pair<Long, String>>()
        db.rawQuery(
            "SELECT key, provider, tile FROM tiles WHERE length(tile) = CAST(? AS INTEGER)",
            arrayOf(sizeBytes.toString()),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val provider = cursor.getString(1)
                if (provider != null && md5HexOf(cursor.getBlob(2)) == md5Hex) {
                    matches += cursor.getLong(0) to provider
                }
            }
        }
        // One transaction for all deletes: a journaled commit per row would be thousands of fsyncs.
        var deleted = 0
        db.beginTransaction()
        try {
            for ((key, provider) in matches) {
                deleted += db.delete("tiles", "key = ? AND provider = ?", arrayOf(key.toString(), provider))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        deleted
    } catch (_: SQLiteException) {
        -1
    } finally {
        try {
            db?.close()
        } catch (_: SQLiteException) {
            // best effort
        }
    }
}

private fun md5HexOf(bytes: ByteArray): String =
    MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }

/** True when the OSM tile server refuses this class of agent: blank, or a placeholder app id. */
internal fun isBlockedClassUserAgent(resolved: String): Boolean =
    resolved.isBlank() ||
        resolved.trim().startsWith(BLOCKED_PLACEHOLDER_PACKAGE_PREFIX, ignoreCase = true)
