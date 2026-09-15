package io.github.ygaray.yahirandroidtaste.component

import android.content.Context
import java.io.File
import org.osmdroid.config.Configuration

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
 *     ([resolveOsmdroidUserAgent] falls back to the host's package name when the caller passes a
 *     blank/whitespace agent), never osmdroid's library-default agent.
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
    Configuration.getInstance().userAgentValue =
        resolveOsmdroidUserAgent(userAgent, appContext.packageName)
    val basePath = File(appContext.cacheDir, "osmdroid")
    Configuration.getInstance().osmdroidBasePath = basePath
    Configuration.getInstance().osmdroidTileCache = File(basePath, "tiles")
}

/**
 * Returns the trimmed [userAgent], or [packageName] when the trimmed value is blank. Used by
 * [configureOsmdroid] to guarantee osmdroid's tile requests never carry a blank User-Agent
 * (OSM tile usage policy).
 */
internal fun resolveOsmdroidUserAgent(userAgent: String, packageName: String): String {
    val trimmed = userAgent.trim()
    return trimmed.ifEmpty { packageName }
}
