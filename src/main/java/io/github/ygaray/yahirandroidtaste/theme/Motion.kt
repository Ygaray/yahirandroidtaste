package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

/**
 * Plain-Kotlin motion-token set (DS-02, D-01). This public type intentionally uses only stable,
 * non-experimental `androidx.compose.animation`/`androidx.compose.animation.core` types
 * ([FiniteAnimationSpec], [EnterTransition], [ExitTransition], [Int]) — it never references M3's
 * experimental expressive-motion API surface anywhere, satisfying D-01's opt-in containment as a
 * hard module-wide invariant (not merely a single-file containment) — no consumer (SecondBrain,
 * CalTracker, or any future one) is ever forced to opt in to consume these tokens.
 *
 * A plain `object` (not a `CompositionLocal`-scoped `data class`) — motion tokens here are
 * theme-invariant constants, not derived from a resolved `ColorScheme`/dark-theme flag, so no
 * `CompositionLocal` indirection is needed.
 */
object ExpressiveMotion {

    /** Standard transition duration in milliseconds. */
    val standardMillis: Int = 300

    /** Emphasized spatial motion — pronounced overshoot, for primary/hero transitions. */
    val emphasizedSpatialSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.8f, stiffness = 380f)

    /** Fast spatial motion — quick, lightly-damped, for small/frequent transitions. */
    val fastSpatialSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.6f, stiffness = 800f)

    /** Emphasized effects motion — critically damped, for opacity/color-only transitions. */
    val emphasizedEffectsSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 1f, stiffness = 1600f)

    /** Standard fade-in, timed to [standardMillis]. */
    val enterFade: EnterTransition = fadeIn(tween(standardMillis))

    /** Standard fade-out, timed to [standardMillis]. */
    val exitFade: ExitTransition = fadeOut(tween(standardMillis))
}
