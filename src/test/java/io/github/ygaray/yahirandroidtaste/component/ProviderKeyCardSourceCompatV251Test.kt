package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
import org.junit.Test

/**
 * Compile-only canaries for the [ProviderKeyCard] call shapes consumers compiled against v2.5.1 and
 * earlier (Phase 23, VKEY-01): the lifecycle parameters are appended after `providerLabel`, so each
 * shape must keep binding the same values.
 *
 * Mirrors [VoiceI18nSourceCompatTest]: the composable calls live inside non-invoked
 * `@Composable () -> Unit` lambdas and only have to COMPILE.
 */
class ProviderKeyCardSourceCompatV251Test {

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun v251NamedCallShape_compiles() {
        // SecondBrain / Explorer style: all ten v2.5.1 parameters, named.
        val call: @androidx.compose.runtime.Composable () -> Unit = {
            ProviderKeyCard(
                providers = listOf(ProviderOptionUiModel(id = "p", label = "P")),
                selectedProviderId = "p",
                onProviderSelected = { _: String -> },
                keyValue = "",
                onKeyChange = { _: String -> },
                keyState = KeyFieldState.Empty,
                keyLabel = "API key",
                modifier = Modifier,
                emptyProvidersReason = "No providers",
                providerLabel = "Provider"
            )
        }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun v24PositionalThroughEmptyProvidersReason_compiles() {
        val call: @androidx.compose.runtime.Composable () -> Unit = {
            ProviderKeyCard(
                emptyList(),
                null,
                { _: String -> },
                "",
                { _: String -> },
                KeyFieldState.Empty,
                "API key",
                Modifier,
                "No providers"
            )
        }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun v25PositionalThroughProviderLabel_compiles() {
        val call: @androidx.compose.runtime.Composable () -> Unit = {
            ProviderKeyCard(
                emptyList(),
                null,
                { _: String -> },
                "",
                { _: String -> },
                KeyFieldState.Empty,
                "API key",
                Modifier,
                "No providers",
                "Provider"
            )
        }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun v260NamedLifecycleCallShape_compiles() {
        val call: @androidx.compose.runtime.Composable () -> Unit = {
            ProviderKeyCard(
                providers = emptyList(),
                selectedProviderId = null,
                onProviderSelected = { _: String -> },
                keyValue = "",
                onKeyChange = { _: String -> },
                keyState = KeyFieldState.Empty,
                keyLabel = "API key",
                modifier = Modifier,
                emptyProvidersReason = "No providers",
                providerLabel = "Provider",
                savedKeys = mapOf("p" to SavedKeyUiModel("fp")),
                editingSavedKey = false,
                actions = ProviderKeyActions(onSave = {}),
                labels = ProviderKeyDefaults.labels()
            )
        }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun v260LabelsDefaultsNamedSubset_compiles() {
        val labels = ProviderKeyDefaults.labels(save = "Guardar", savedMarker = "guardada")
        val actions = ProviderKeyActions(onSave = {}, onClear = {})
    }
}
