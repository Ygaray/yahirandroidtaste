package io.github.ygaray.yahirandroidtaste.component

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import org.osmdroid.config.Configuration

private const val USER_AGENT_HEADER = "User-Agent"
private const val LOG_TAG = "YatOsmdroid"
private const val OSM_TILE_POLICY_URL = "https://operations.osmfoundation.org/policies/tiles/"

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
 *     `<packageName>/<versionName> (Android <release>)` and logs one warning), never osmdroid's
 *     library-default agent. The resolved agent is also injected as the `User-Agent` request
 *     property after every `load()`, because the MAPNIK tile source normalizes the agent to
 *     `<packageName>/<versionCode>` and ignores `userAgentValue`. Callers should pass an
 *     identifying agent of the form `AppName/version (+contact)`.
 *  2. **Honor tile-server cache headers.** osmdroid's expiry-override handling is left at its
 *     default here — this function never calls an API that overrides cache expiry.
 *  3. **No bulk or offline tile pre-fetching.** This function calls no bulk-download or
 *     cache-manager API; it only configures where tiles are written once fetched.
 *
 * `osmdroidBasePath` and `osmdroidTileCache` are pointed at the host app's private `cacheDir`
 * (never shared external storage) so no storage permission is required, tiles are confined to
 * the app sandbox, and the OS is free to evict them under storage pressure.
 *
 * Imports only `android.`, `java.io.` and `org.osmdroid.` symbols (hub one-way-dependency
 * invariant).
 */
internal fun configureOsmdroid(context: Context, userAgent: String) {
    val appContext = context.applicationContext
    val prefs = appContext.getSharedPreferences("yahirandroidtaste_osmdroid", Context.MODE_PRIVATE)
    // load() first: it resets Configuration's userAgentValue, so anything set before load() would
    // be discarded.
    Configuration.getInstance().load(appContext, prefs)
    val resolvedAgent =
        resolveOsmdroidUserAgent(
            userAgent,
            appContext.packageName,
            hostVersionName(appContext),
            Build.VERSION.RELEASE,
        )
    Configuration.getInstance().userAgentValue = resolvedAgent
    // load() re-fills the request properties from persisted prefs on every call, so the header is
    // injected in memory after EVERY load (never written back to prefs: the consumer owns that
    // slot). Case variants are removed first so exactly one User-Agent entry remains.
    val requestProperties = Configuration.getInstance().additionalHttpRequestProperties
    requestProperties.keys.removeAll { it.equals(USER_AGENT_HEADER, ignoreCase = true) }
    requestProperties[USER_AGENT_HEADER] = resolvedAgent
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
    Configuration.getInstance().osmdroidTileCache = File(basePath, "tiles")
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

/** True when the OSM tile server refuses this class of agent: blank, or a placeholder app id. */
internal fun isBlockedClassUserAgent(resolved: String): Boolean =
    resolved.isBlank() ||
        resolved.trim().startsWith(BLOCKED_PLACEHOLDER_PACKAGE_PREFIX, ignoreCase = true)
