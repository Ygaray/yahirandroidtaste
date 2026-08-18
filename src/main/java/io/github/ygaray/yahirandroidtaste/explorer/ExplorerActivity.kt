package io.github.ygaray.yahirandroidtaste.explorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme

/**
 * CATALOG-06: :yahirandroidtaste's own launch surface for [ExplorerEntry] — lets any consuming
 * app open the full component gallery via a plain, explicit, same-package Intent with zero
 * nav-graph/Settings glue. Hosts ExplorerEntry UNCHANGED (D-03) — this class supplies only the
 * Activity-level plumbing ExplorerEntry itself has never needed (a NavController-free
 * onNavigateBack, edge-to-edge insets, and an outer theme wrap for defense-in-depth even though
 * every ExplorerEntry leaf screen already self-wraps).
 */
class ExplorerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YahirAndroidTasteTheme {
                ExplorerEntry(onNavigateBack = { finish() })
            }
        }
    }
}
