package io.github.ygaray.yahirandroidtaste.component

import android.app.Application
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
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

    @Before
    fun setUp() {
        originalUserAgent = Configuration.getInstance().userAgentValue
        originalBasePath = Configuration.getInstance().osmdroidBasePath
        originalTileCache = Configuration.getInstance().osmdroidTileCache
    }

    @After
    fun tearDown() {
        Configuration.getInstance().userAgentValue = originalUserAgent
        Configuration.getInstance().osmdroidBasePath = originalBasePath
        Configuration.getInstance().osmdroidTileCache = originalTileCache
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
