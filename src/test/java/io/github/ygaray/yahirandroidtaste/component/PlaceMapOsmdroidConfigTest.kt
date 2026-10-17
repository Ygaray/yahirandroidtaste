package io.github.ygaray.yahirandroidtaste.component

import android.app.Application
import android.content.Context
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.config.Configuration
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

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
        get() = application.getSharedPreferences("yahirandroidtaste_osmdroid", Context.MODE_PRIVATE)

    @Before
    fun setUp() {
        originalUserAgent = Configuration.getInstance().userAgentValue
        originalBasePath = Configuration.getInstance().osmdroidBasePath
        originalTileCache = Configuration.getInstance().osmdroidTileCache
        originalRequestProperties = HashMap(Configuration.getInstance().additionalHttpRequestProperties)
        osmPrefs.edit().clear().commit()
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

    @Test
    fun `resolveOsmdroidUserAgent returns the given agent when non-blank`() {
        assertEquals(
            "com.example.secondbrain",
            resolveOsmdroidUserAgent("com.example.secondbrain", "com.host.app"),
        )
    }

    @Test
    fun `resolveOsmdroidUserAgent trims the given agent`() {
        assertEquals(
            "com.example.secondbrain",
            resolveOsmdroidUserAgent("  com.example.secondbrain  ", "com.host.app"),
        )
    }

    @Test
    fun `resolveOsmdroidUserAgent falls back to packageName when agent is empty`() {
        assertEquals("com.host.app", resolveOsmdroidUserAgent("", "com.host.app"))
    }

    @Test
    fun `resolveOsmdroidUserAgent falls back to packageName when agent is blank`() {
        assertEquals("com.host.app", resolveOsmdroidUserAgent("   ", "com.host.app"))
    }

    @Test
    fun `configureOsmdroid sets userAgentValue to the given agent`() {
        configureOsmdroid(application, "com.example.secondbrain")
        assertEquals("com.example.secondbrain", Configuration.getInstance().userAgentValue)
    }

    @Test
    fun `configureOsmdroid sets osmdroidBasePath under the app private cache dir`() {
        configureOsmdroid(application, "com.example.secondbrain")
        assertEquals(
            File(application.cacheDir, "osmdroid"),
            Configuration.getInstance().osmdroidBasePath,
        )
    }

    @Test
    fun `configureOsmdroid sets osmdroidTileCache under the base path`() {
        configureOsmdroid(application, "com.example.secondbrain")
        val expectedBase = File(application.cacheDir, "osmdroid")
        assertEquals(
            File(expectedBase, "tiles"),
            Configuration.getInstance().osmdroidTileCache,
        )
    }

    @Test
    fun `configureOsmdroid re-applies on every call and never leaves a stale agent`() {
        configureOsmdroid(application, "first.agent")
        assertEquals("first.agent", Configuration.getInstance().userAgentValue)
        configureOsmdroid(application, "second.agent")
        assertEquals("second.agent", Configuration.getInstance().userAgentValue)
    }

    @Test
    fun `configureOsmdroid with a blank agent falls back to the app package name`() {
        configureOsmdroid(application, "")
        assertEquals(application.packageName, Configuration.getInstance().userAgentValue)
    }

    @Test
    fun `a test that does not configure sees the restored original value, not a leaked agent`() {
        // Guards test-order isolation: if tearDown from a prior configuring test failed to
        // restore, this assertion would observe "second.agent" instead of the captured original.
        assertTrue(
            "expected the pre-suite original userAgentValue, not a leaked test value",
            Configuration.getInstance().userAgentValue == originalUserAgent,
        )
    }
}
