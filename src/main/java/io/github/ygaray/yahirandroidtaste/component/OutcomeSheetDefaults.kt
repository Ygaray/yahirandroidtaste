package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.ToneDefaults

/**
 * The rendering style of [OutcomeSheet] (VRCPT-01, D-03).
 *
 * The enum is opt-in: the default is [Classic], which is the v2.5.1 rendering unchanged, so a call
 * that does not pass a style renders exactly as before.
 */
enum class OutcomeSheetStyle {
    /** The v2.5.1 rendering, unchanged. The default. */
    Classic,

    /**
     * The receipt layout: status badge, title row, transcript line, tone card and a pinned action
     * bar. Opt-in.
     */
    Receipt
}

/**
 * The two tone token pairs [OutcomeSheet] paints with (D-06), resolved by
 * [OutcomeSheetDefaults.colors]. A plain class with value equality (a data class would publish
 * `copy` and `componentN`), mirroring [ToneColors], so two holders built from the same tokens are
 * interchangeable and stable for Compose skipping.
 *
 * @property warnTone Paints the Classic Warn failure card, the Receipt Warn `ToneCard`, the Warn
 *   badge and the on-device pill dot. Pass your own tokens when dynamic color (Material You) makes
 *   `tertiary` non-amber.
 * @property errorTone Paints the Receipt Error `ToneCard` and the Error badge.
 */
@Immutable
class OutcomeSheetColors(val warnTone: ToneColors, val errorTone: ToneColors) {

    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is OutcomeSheetColors && warnTone == other.warnTone && errorTone == other.errorTone)

    override fun hashCode(): Int = 31 * warnTone.hashCode() + errorTone.hashCode()

    override fun toString(): String = "OutcomeSheetColors(warnTone=$warnTone, errorTone=$errorTone)"
}

/** Defaults for [OutcomeSheet]'s Receipt style (VRCPT-01, D-05). */
object OutcomeSheetDefaults {

    /**
     * Builds the [OutcomeSheetColors] for [OutcomeSheet], resolved from the ACTIVE
     * [MaterialTheme.colorScheme] at call time.
     *
     * @param warnTone The warn tone; defaults to [ToneDefaults.warn].
     * @param errorTone The error tone; defaults to [ToneDefaults.error].
     */
    @Composable
    fun colors(
        warnTone: ToneColors = ToneDefaults.warn(),
        errorTone: ToneColors = ToneDefaults.error()
    ): OutcomeSheetColors = OutcomeSheetColors(warnTone = warnTone, errorTone = errorTone)

    /**
     * The Receipt title style: `titleLarge` at 18sp / 24sp [FontWeight.SemiBold] (D-05), resolved
     * from the active theme typography.
     */
    val ReceiptTitleStyle: TextStyle
        @Composable
        get() = MaterialTheme.typography.titleLarge.copy(
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
}
