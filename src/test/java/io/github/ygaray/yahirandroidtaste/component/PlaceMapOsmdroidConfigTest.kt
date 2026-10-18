package io.github.ygaray.yahirandroidtaste.component

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.config.Configuration
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * Tests for [configureOsmdroid] and [resolveOsmdroidUserAgent] (HUBW-02 D-06).
 *
 * osmdroid's [Configuration] is process-global mutable state (164-01 Round 1 LOW finding):
 * [setUp] captures `userAgentValue`, `osmdroidBasePath` and `osmdroidTileCache` before each test
 * and [tearDown] restores all three, so this class never leaks configuration across the suite or
 * into any other test class that runs after it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlaceMapOsmdroidConfigTest {

    private val application: Application = RuntimeEnvironment.getApplication()

    private lateinit var originalUserAgent: String
    private lateinit var originalBasePath: File
    private lateinit var originalTileCache: File
    private lateinit var originalRequestProperties: Map<String, String>

    private val osmPrefs
        get() = application.getSharedPreferences(OSMDROID_PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setUp() {
        originalUserAgent = Configuration.getInstance().userAgentValue
        originalBasePath = Configuration.getInstance().osmdroidBasePath
        originalTileCache = Configuration.getInstance().osmdroidTileCache
        originalRequestProperties = HashMap(Configuration.getInstance().additionalHttpRequestProperties)
        osmPrefs.edit().clear().commit()
        File(application.cacheDir, "osmdroid").deleteRecursively()
        resetPlaceholderScanForTests()
        resetOsmdroidLoadForTests()
        // Deterministic by default: the scan runs inline. The production executor is a background
        // thread (CR-01) and is exercised explicitly by the tests that restore it.
        placeholderPurgeExecutor = Executor { it.run() }
        ShadowLog.clear()
    }

    @After
    fun tearDown() {
        Configuration.getInstance().userAgentValue = originalUserAgent
        Configuration.getInstance().osmdroidBasePath = originalBasePath
        Configuration.getInstance().osmdroidTileCache = originalTileCache
        Configuration.getInstance().additionalHttpRequestProperties.apply {
            clear()
            putAll(originalRequestProperties)
        }
        osmPrefs.edit().clear().commit()
        File(application.cacheDir, "osmdroid").deleteRecursively()
        resetPlaceholderScanForTests()
        resetOsmdroidLoadForTests()
        resetPlaceholderPurgeExecutorForTests()
    }

    /**
     * osmdroid's very first load (no persisted base path) takes a "first run" branch that rewrites
     * the persisted request-property keys from memory. A real consumer has already passed that
     * branch, so seed an existing base path to reach the restore branch under test.
     */
    private fun seedPersistedPrefs(vararg entries: Pair<String, String>) {
        val dir = application.cacheDir.also { it.mkdirs() }
        osmPrefs.edit().apply {
            putString("osmdroid.basePath", dir.absolutePath)
            putString("osmdroid.cachePath", dir.absolutePath)
            entries.forEach { (key, value) -> putString(key, value) }
        }.commit()
    }

    private fun requestProperties(): MutableMap<String, String> =
        Configuration.getInstance().additionalHttpRequestProperties

    private fun userAgentEntryCount(): Int =
        requestProperties().keys.count { it.equals("User-Agent", ignoreCase = true) }

    @Test
    fun callerAgent_reachesTheUserAgentRequestProperty_afterTheFirstCall() {
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals("Probe/1.0 (+https://example.org)", requestProperties()["User-Agent"])
    }

    @Test
    fun secondCallWithAnotherAgent_replacesTheHeader_andKeepsExactlyOneUserAgentEntry() {
        configureOsmdroid(application, "First/1.0 (+https://example.org)")
        assertEquals("First/1.0 (+https://example.org)", requestProperties()["User-Agent"])
        configureOsmdroid(application, "Second/2.0 (+https://example.org)")
        assertEquals("Second/2.0 (+https://example.org)", requestProperties()["User-Agent"])
        assertEquals(1, userAgentEntryCount())
    }

    @Test
    fun persistedUserAgentValue_isOverwrittenInMemory_andTheHubNeverRewritesThePrefs() {
        val key = "osmdroid.additionalHttpRequestProperty.User-Agent"
        seedPersistedPrefs(key to "Persisted/0.1 (consumer owned)")
        configureOsmdroid(application, "Caller/3.0 (+https://example.org)")
        configureOsmdroid(application, "Caller/3.0 (+https://example.org)")
        assertEquals("Caller/3.0 (+https://example.org)", requestProperties()["User-Agent"])
        assertEquals(1, userAgentEntryCount())
        assertEquals("Persisted/0.1 (consumer owned)", osmPrefs.getString(key, null))
    }

    @Test
    fun differentlyCasedPersistedKey_leavesExactlyOneUserAgentEntry() {
        seedPersistedPrefs("osmdroid.additionalHttpRequestProperty.user-agent" to "lower/0.1")
        configureOsmdroid(application, "Caller/3.0 (+https://example.org)")
        configureOsmdroid(application, "Caller/3.0 (+https://example.org)")
        assertEquals(1, userAgentEntryCount())
        assertEquals("Caller/3.0 (+https://example.org)", requestProperties()["User-Agent"])
    }

    @Test
    fun otherPersistedRequestProperties_survive() {
        seedPersistedPrefs("osmdroid.additionalHttpRequestProperty.X-Other" to "kept")
        configureOsmdroid(application, "Caller/3.0 (+https://example.org)")
        assertEquals("kept", requestProperties()["X-Other"])
        assertEquals("Caller/3.0 (+https://example.org)", requestProperties()["User-Agent"])
    }

    @Test
    fun userAgentValue_stillCarriesTheResolvedAgent() {
        configureOsmdroid(application, "Caller/3.0 (+https://example.org)")
        assertEquals("Caller/3.0 (+https://example.org)", Configuration.getInstance().userAgentValue)
    }

    @Test
    fun headerKey_isTheLiteralUserAgent() {
        configureOsmdroid(application, "Caller/3.0 (+https://example.org)")
        assertTrue(requestProperties().containsKey("User-Agent"))
        assertNull(requestProperties()["user-agent"])
    }

    private fun resolve(
        agent: String,
        pkg: String = "com.host.app",
        versionName: String? = "2.1",
        release: String = "15",
    ) = resolveOsmdroidUserAgent(agent, pkg, versionName, release)

    @Test
    fun `resolveOsmdroidUserAgent returns the given agent when non-blank`() {
        assertEquals("Probe/1.0 (+https://example.org)", resolve("Probe/1.0 (+https://example.org)"))
    }

    @Test
    fun `resolveOsmdroidUserAgent trims the given agent`() {
        assertEquals("Probe/1.0 (+https://example.org)", resolve("  Probe/1.0 (+https://example.org)  "))
    }

    @Test
    fun `resolveOsmdroidUserAgent blank fallback is the honest package version android form`() {
        assertEquals("com.host.app/2.1 (Android 15)", resolve(""))
        assertEquals("com.host.app/2.1 (Android 15)", resolve("   "))
    }

    @Test
    fun `resolveOsmdroidUserAgent expands a bare package name the same way`() {
        assertEquals("com.host.app/2.1 (Android 15)", resolve("com.host.app"))
        assertEquals("com.host.app/2.1 (Android 15)", resolve("  com.host.app  "))
    }

    @Test
    fun `resolveOsmdroidUserAgent passes another package name through untouched`() {
        assertEquals("com.other.app", resolve("com.other.app"))
    }

    @Test
    fun `resolveOsmdroidUserAgent removes control characters so no header line can be injected`() {
        val resolved = resolve("Evil/1.0\r\nX-Injected: yes\u0000")
        assertEquals("Evil/1.0X-Injected: yes", resolved)
        assertTrue(resolved.none { it.isISOControl() })
    }

    @Test
    fun `resolveOsmdroidUserAgent falls back when the agent is only control characters`() {
        assertEquals("com.host.app/2.1 (Android 15)", resolve("\r\n\t\u0000"))
    }

    @Test
    fun `resolveOsmdroidUserAgent renders a null or blank versionName as 0`() {
        assertEquals("com.host.app/0 (Android 15)", resolve("", versionName = null))
        assertEquals("com.host.app/0 (Android 15)", resolve("", versionName = "  "))
    }

    @Test
    fun `resolveOsmdroidUserAgent produces the exact strings the host probes use`() {
        assertEquals(
            "io.github.ygaray.yahirandroidtasteharness/1.0 (Android 15)",
            resolveOsmdroidUserAgent("", "io.github.ygaray.yahirandroidtasteharness", "1.0", "15"),
        )
        assertEquals(
            "${BLOCKED_PLACEHOLDER_PACKAGE_PREFIX}probe/1.0 (Android 15)",
            resolveOsmdroidUserAgent("", "${BLOCKED_PLACEHOLDER_PACKAGE_PREFIX}probe", "1.0", "15"),
        )
    }

    @Test
    fun `isBlockedClassUserAgent is true for blank and placeholder agents in any letter case`() {
        assertTrue(isBlockedClassUserAgent(""))
        assertTrue(isBlockedClassUserAgent("   "))
        assertTrue(isBlockedClassUserAgent("${BLOCKED_PLACEHOLDER_PACKAGE_PREFIX}probe/1.0 (Android 15)"))
        assertTrue(isBlockedClassUserAgent("${BLOCKED_PLACEHOLDER_PACKAGE_PREFIX.uppercase()}Probe/1"))
    }

    @Test
    fun `isBlockedClassUserAgent is false for a real identifying agent`() {
        assertFalse(isBlockedClassUserAgent("io.github.ygaray.yahirandroidtasteharness/1.0 (Android 15)"))
        assertFalse(
            isBlockedClassUserAgent(
                "YahirAndroidTasteExplorer/2.8 (+https://github.com/Ygaray/yahirandroidtaste)",
            ),
        )
    }

    @Test
    fun `configureOsmdroid sets userAgentValue to the given agent`() {
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals("Probe/1.0 (+https://example.org)", Configuration.getInstance().userAgentValue)
    }

    @Test
    fun `configureOsmdroid sets osmdroidBasePath under the app private cache dir`() {
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals(
            File(application.cacheDir, "osmdroid"),
            Configuration.getInstance().osmdroidBasePath,
        )
    }

    @Test
    fun `configureOsmdroid sets osmdroidTileCache under the base path`() {
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        val expectedBase = File(application.cacheDir, "osmdroid")
        assertEquals(
            File(expectedBase, "tiles"),
            Configuration.getInstance().osmdroidTileCache,
        )
    }

    @Test
    fun `configureOsmdroid re-applies on every call and never leaves a stale agent`() {
        configureOsmdroid(application, "first.agent/1")
        assertEquals("first.agent/1", Configuration.getInstance().userAgentValue)
        configureOsmdroid(application, "second.agent/1")
        assertEquals("second.agent/1", Configuration.getInstance().userAgentValue)
    }

    @Test
    fun `configureOsmdroid with a blank agent resolves to the honest fallback form`() {
        configureOsmdroid(application, "")
        val resolved = Configuration.getInstance().userAgentValue
        assertTrue(resolved, resolved.startsWith("${application.packageName}/"))
        assertTrue(resolved, resolved.endsWith(" (Android ${Build.VERSION.RELEASE})"))
        assertEquals(resolved, requestProperties()["User-Agent"])
    }

    private fun warnings(): List<String> =
        ShadowLog.getLogsForTag("YatOsmdroid").filter { it.type == Log.WARN }.map { it.msg }

    @Test
    fun `a blank caller agent logs exactly one warning naming the policy url`() {
        configureOsmdroid(application, "")
        val lines = warnings()
        assertEquals(1, lines.size)
        assertTrue(lines[0], lines[0].contains("https://operations.osmfoundation.org/policies/tiles/"))
        assertTrue(lines[0], lines[0].contains(Configuration.getInstance().userAgentValue))
    }

    @Test
    fun `a placeholder prefixed caller agent logs exactly one warning`() {
        configureOsmdroid(application, "${BLOCKED_PLACEHOLDER_PACKAGE_PREFIX}probe/1.0 (Android 15)")
        assertEquals(1, warnings().size)
    }

    @Test
    fun `a real identifying agent logs no warning`() {
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals(0, warnings().size)
    }

    @Test
    fun `a placeholder package with a blank agent logs exactly one warning not two`() {
        // @Config(packageName) is gone in this Robolectric; a wrapper reports the placeholder
        // package. Its package is unknown to the PackageManager, which also pins the
        // NameNotFoundException path (versionName 0, no throw).
        val placeholderHost =
            object : ContextWrapper(application) {
                override fun getPackageName(): String = BLOCKED_PLACEHOLDER_PACKAGE_PREFIX + "warnapp"

                override fun getApplicationContext(): Context = this
            }
        configureOsmdroid(placeholderHost, "")
        assertEquals(1, warnings().size)
        assertEquals(
            "${BLOCKED_PLACEHOLDER_PACKAGE_PREFIX}warnapp/0 (Android ${Build.VERSION.RELEASE})",
            Configuration.getInstance().userAgentValue,
        )
    }

    @Test
    fun `two consecutive blocked class calls log two warnings`() {
        configureOsmdroid(application, "")
        configureOsmdroid(application, "")
        assertEquals(2, warnings().size)
    }

    @Test
    fun `a test that does not configure sees the restored original value, not a leaked agent`() {
        // Guards test-order isolation: if tearDown from a prior configuring test failed to
        // restore, this assertion would observe a leaked agent instead of the captured original.
        assertTrue(
            "expected the pre-suite original userAgentValue, not a leaked test value",
            Configuration.getInstance().userAgentValue == originalUserAgent,
        )
    }

    // ---- POL-03 placeholder purge trigger ------------------------------------------------------

    private val cacheDb: File
        get() = File(File(File(application.cacheDir, "osmdroid"), "tiles"), "cache.db")

    private val placeholderBytes: ByteArray by lazy {
        checkNotNull(javaClass.classLoader!!.getResourceAsStream("osm-blocked-placeholder.png")) {
            "fixture osm-blocked-placeholder.png missing"
        }.use { it.readBytes() }
    }

    private fun realTileBytes(): ByteArray = ByteArray(4472) { (it * 31 + 7).toByte() }

    private fun seedCache(vararg rows: Pair<Long, ByteArray>) {
        cacheDb.parentFile!!.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(cacheDb, null)
        try {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS tiles (key INTEGER , provider TEXT, tile BLOB, " +
                    "expires INTEGER, PRIMARY KEY (key, provider));",
            )
            rows.forEach { (key, tile) ->
                db.execSQL(
                    "INSERT INTO tiles (key, provider, tile, expires) VALUES (?, ?, ?, ?)",
                    arrayOf<Any>(key, "Mapnik", tile, 4102444800000L),
                )
            }
        } finally {
            db.close()
        }
    }

    private fun cachedKeys(): List<Long> {
        val db = SQLiteDatabase.openDatabase(cacheDb.path, null, SQLiteDatabase.OPEN_READONLY)
        try {
            db.rawQuery("SELECT key FROM tiles ORDER BY key", null).use { c ->
                val out = mutableListOf<Long>()
                while (c.moveToNext()) out += c.getLong(0)
                return out
            }
        } finally {
            db.close()
        }
    }

    private val purgedFlag: Boolean
        get() = osmPrefs.getBoolean(PREF_PLACEHOLDER_PURGED, false)

    @Test
    fun purgeRunsOnTheFirstCallOfAProcess_andSetsTheInstallFlag() {
        seedCache(1L to placeholderBytes, 2L to realTileBytes())
        assertFalse(purgedFlag)
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals(listOf(2L), cachedKeys())
        assertTrue(purgedFlag)
    }

    @Test
    fun secondCallInTheSameProcessDoesNotRescan() {
        seedCache(2L to realTileBytes())
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertTrue(purgedFlag)
        seedCache(1L to placeholderBytes)
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals(listOf(1L, 2L), cachedKeys())
    }

    @Test
    fun aNewProcessScansAgain_evenWhenTheInstallFlagIsSet() {
        seedCache(2L to realTileBytes())
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertTrue(purgedFlag)
        seedCache(1L to placeholderBytes)
        resetPlaceholderScanForTests()
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals(listOf(2L), cachedKeys())
        assertTrue(purgedFlag)
    }

    @Test
    fun failedScanLeavesTheFlagUnsetAndNeverThrows() {
        cacheDb.parentFile!!.mkdirs()
        cacheDb.writeBytes(ByteArray(4096) { (it % 251).toByte() })
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertFalse(purgedFlag)
        assertTrue(cacheDb.exists())
    }

    @Test
    fun failedScanIsRetriedOnTheNextCall() {
        cacheDb.parentFile!!.mkdirs()
        cacheDb.writeBytes(ByteArray(4096) { (it % 251).toByte() })
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertFalse(purgedFlag)
        cacheDb.delete()
        seedCache(1L to placeholderBytes, 2L to realTileBytes())
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals(listOf(2L), cachedKeys())
        assertTrue(purgedFlag)
    }

    @Test
    fun changingTheAgentNeverWipesRealTiles() {
        seedCache(1L to realTileBytes(), 2L to realTileBytes())
        configureOsmdroid(application, "AgentA/1.0 (+https://example.org)")
        configureOsmdroid(application, "AgentB/2.0 (+https://example.org)")
        assertEquals(listOf(1L, 2L), cachedKeys())
    }

    @Test
    fun missingCacheFile_setsTheFlag_andCreatesNothing() {
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertTrue(purgedFlag)
        assertFalse(cacheDb.exists())
    }

    @Test
    fun mapCreationPathStillSetsBaseAndTileCache() {
        seedCache(1L to placeholderBytes)
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        assertEquals(File(application.cacheDir, "osmdroid"), Configuration.getInstance().osmdroidBasePath)
        assertEquals(
            File(File(application.cacheDir, "osmdroid"), "tiles"),
            Configuration.getInstance().osmdroidTileCache,
        )
        assertEquals("Probe/1.0 (+https://example.org)", requestProperties()["User-Agent"])
    }

    @Test
    fun productionExecutor_runsTheScanOffTheCallingThread_andStillPurges() {
        resetPlaceholderPurgeExecutorForTests()
        seedCache(1L to placeholderBytes, 2L to realTileBytes())
        configureOsmdroid(application, "Probe/1.0 (+https://example.org)")
        // the flag is written last, by the purge thread; the shared prefs map updates in memory at once
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (!purgedFlag && System.nanoTime() < deadline) Thread.sleep(20)
        assertTrue("scan flag written by the purge thread", purgedFlag)
        assertEquals(listOf(2L), cachedKeys())
    }

    @Test
    fun scanThatOverlapsARunningScan_isDropped_notQueued() {
        val queued = mutableListOf<Runnable>()
        placeholderPurgeExecutor = Executor { queued += it }
        seedCache(1L to placeholderBytes)
        configureOsmdroid(application, agentA)
        resetPlaceholderScanForTests() // would otherwise rescan: only the in-flight guard may stop it
        configureOsmdroid(application, agentA)
        assertEquals(1, queued.size)
        queued.single().run()
        assertEquals(emptyList<Long>(), cachedKeys())
        assertTrue(purgedFlag)
    }

    @Test
    fun rejectedScanTask_neverThrows_andReleasesTheGuard() {
        placeholderPurgeExecutor = Executor { throw java.util.concurrent.RejectedExecutionException("full") }
        seedCache(1L to placeholderBytes)
        configureOsmdroid(application, agentA)
        assertFalse(purgedFlag)
        placeholderPurgeExecutor = Executor { it.run() }
        configureOsmdroid(application, agentA)
        assertTrue(purgedFlag)
        assertEquals(emptyList<Long>(), cachedKeys())
    }

    // ---- POL-03 load() once per process (gap plan 28-08) ---------------------------------------

    private val agentA = "AgentA/1.0 (+https://example.org)"
    private val agentB = "AgentB/2.0 (+https://example.org)"

    @Test
    fun secondCallDoesNotReload_andKeepsAnUnrelatedRequestProperty() {
        configureOsmdroid(application, agentA)
        val mapBefore = requestProperties()
        mapBefore["X-Probe"] = "kept"
        configureOsmdroid(application, agentA)
        // load() clears and refills the map from prefs: a surviving unrelated entry proves no reload.
        assertSame(mapBefore, requestProperties())
        assertEquals("kept", requestProperties()["X-Probe"])
        assertEquals(agentA, requestProperties()["User-Agent"])
    }

    @Test
    fun secondCallWithTheSameAgentKeepsTheHeader() {
        configureOsmdroid(application, agentA)
        assertEquals(agentA, requestProperties()["User-Agent"])
        configureOsmdroid(application, agentA)
        assertEquals(agentA, requestProperties()["User-Agent"])
        assertEquals(1, userAgentEntryCount())
    }

    @Test
    fun secondCallWithAnotherAgentReplacesTheValueWithoutReloading() {
        configureOsmdroid(application, agentA)
        requestProperties()["X-Probe"] = "kept"
        configureOsmdroid(application, agentB)
        assertEquals(agentB, requestProperties()["User-Agent"])
        assertEquals(agentB, Configuration.getInstance().userAgentValue)
        assertEquals("kept", requestProperties()["X-Probe"])
        assertEquals(1, userAgentEntryCount())
    }

    @Test
    fun secondCallWithAnotherAgentKeepsExactlyOneUserAgentEntryEvenWhenCasedDifferently() {
        configureOsmdroid(application, agentA)
        requestProperties()["user-agent"] = "stray/0.1"
        requestProperties()["X-Probe"] = "kept"
        configureOsmdroid(application, agentB)
        assertEquals(1, userAgentEntryCount())
        assertEquals(agentB, requestProperties()["User-Agent"])
        assertNull(requestProperties()["user-agent"])
        assertEquals("kept", requestProperties()["X-Probe"])
    }

    @Test
    fun aRemovedHeaderIsReappliedOnTheNextCall() {
        configureOsmdroid(application, agentA)
        requestProperties().remove("User-Agent")
        configureOsmdroid(application, agentA)
        assertEquals(agentA, requestProperties()["User-Agent"])
        assertEquals(1, userAgentEntryCount())
    }

    @Test
    fun resettingTheLoadGuardMakesTheNextCallLoadAgain() {
        val key = "osmdroid.additionalHttpRequestProperty.User-Agent"
        seedPersistedPrefs(key to "Persisted/0.1 (consumer owned)")
        configureOsmdroid(application, agentA)
        requestProperties()["X-Probe"] = "wiped-by-reload"
        requestProperties().remove("User-Agent")
        resetOsmdroidLoadForTests()
        configureOsmdroid(application, agentB)
        // the reload cleared the unrelated entry; the hub then overwrote the persisted value in memory
        assertNull(requestProperties()["X-Probe"])
        assertEquals(agentB, requestProperties()["User-Agent"])
        assertEquals(1, userAgentEntryCount())
        assertEquals("Persisted/0.1 (consumer owned)", osmPrefs.getString(key, null))
    }

    @Test
    fun warningCountPerCallIsUnchangedOnLaterCalls() {
        configureOsmdroid(application, "")
        configureOsmdroid(application, "")
        configureOsmdroid(application, "")
        assertEquals(3, warnings().size)
        ShadowLog.clear()
        configureOsmdroid(application, agentA)
        configureOsmdroid(application, agentA)
        assertEquals(0, warnings().size)
        configureOsmdroid(application, "${BLOCKED_PLACEHOLDER_PACKAGE_PREFIX}probe/1.0 (Android 15)")
        assertEquals(1, warnings().size)
    }
}
