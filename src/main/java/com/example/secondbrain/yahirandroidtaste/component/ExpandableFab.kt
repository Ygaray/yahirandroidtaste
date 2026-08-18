package com.example.secondbrain.yahirandroidtaste.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState

/**
 * Google Keep-style expandable FAB for creating new cards.
 *
 * Main FloatingActionButton rotates 45° when expanded to become an X.
 * Mini-FABs slide in with AnimatedVisibility when expanded.
 * A scrim Box collapses the FAB when tapped outside.
 *
 * Phase 3: one mini-FAB labelled "Text note" with an Edit icon.
 * Phase 4: adds "List" mini-FAB above "Text note" with FormatListBulleted icon.
 * Phase 5: adds "Voice" mini-FAB above "List" with Mic icon.
 * Phase 6: adds "Album" mini-FAB above "Voice" with PhotoAlbum icon.
 * Phase 42: "Album" mini-FAB opens a nested tier-2 sub-fan (Camera / Gallery) in place instead
 * of navigating directly — replaces the AlbumSourcePickerSheet bottom sheet on the
 * card-creation path (D-01).
 * Phase 64 (CAP-05, D-07): the tier-2 sub-fan restructured from a vertical stack to a horizontal
 * side-fan — Camera/Gallery fan out to the left of the Album pill (perpendicular to the tier-1
 * vertical fan), so the nested tier reads as visually distinct from the tier-1 rows.
 *
 * @param onCreateTextCard Called when the "Text note" mini-FAB is tapped.
 * @param onCreateListCard Called when the "List" mini-FAB is tapped.
 * @param onCreateVoiceCard Called when the "Voice" mini-FAB is tapped.
 * @param onAlbumCamera Called when the tier-2 "Camera" sub-FAB is tapped (after the Album mini
 *   is tapped to expand the nested album tier). Collapses the whole fan before invoking.
 * @param onAlbumGallery Called when the tier-2 "Gallery" sub-FAB is tapped. Collapses the whole
 *   fan before invoking.
 * @param onQuickTake Called when the "Quick Take" mini-FAB is tapped.
 * @param onNewTag Dashboard-only optional entry. When non-null, a topmost "New tag" fan row
 *   renders above every other row (organizational, not a card-type creator) and invokes this
 *   lambda after collapsing the fan. Defaults to null so shared call sites (e.g. Browse) render
 *   no such row and stay byte-identical.
 * @param isAlbumEnabled When false the "Album" and "Quick Take" mini-FABs are fully absent from composition.
 * @param isVoiceEnabled When false the "Voice" mini-FAB is fully absent from composition.
 * @param isListEnabled When false the "List" mini-FAB is fully absent from composition.
 * @param isTextEnabled When false the "Text note" mini-FAB is fully absent from composition.
 * @param configLoaded When false the FAB cannot expand, so no mini-FABs render. WR-01/WR-03
 *   (19 review): the per-type `is<Type>Enabled` flags default to `true` while the module is still
 *   loading (`module == null`), so without this gate the FAB would briefly show ALL create
 *   mini-FABs during the load window — including types the module disallows. Gating expansion on
 *   `configLoaded` keeps the FAB collapsed until the module's allowedCardTypes have resolved,
 *   closing that window without flipping the enabled defaults to `false` (which would hide
 *   legitimately-enabled FABs). Defaults to `true` so the component is unchanged for callers that
 *   have no async config.
 * @param modifier Modifier applied to the outer Box container.
 */
@Composable
fun ExpandableFab(
    onCreateTextCard: () -> Unit,
    onCreateListCard: () -> Unit,
    modifier: Modifier = Modifier,
    onCreateVoiceCard: () -> Unit = {},
    onAlbumCamera: () -> Unit = {},
    onAlbumGallery: () -> Unit = {},
    onQuickTake: () -> Unit = {},
    onNewTag: (() -> Unit)? = null,
    isAlbumEnabled: Boolean = true,
    isVoiceEnabled: Boolean = true,
    isListEnabled: Boolean = true,
    isTextEnabled: Boolean = true,
    configLoaded: Boolean = true
) {
    var expandedRaw by remember { mutableStateOf(false) }
    // Effective expansion: never expand (and thus never render mini-FABs) until the module
    // config has loaded. While loading, the per-type enabled flags are permissive defaults, so
    // showing the expanded content would surface disallowed create options (WR-03).
    val expanded = expandedRaw && configLoaded
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation"
    )

    // Tier-2 nested album sub-fan (Camera / Gallery) — gated on both albumSubExpanded and the
    // parent tier-1 expansion (D-01). Reset whenever the whole fan collapses so re-opening the
    // fan never leaves tier-2 stuck open (D-01a/D-01b invariant).
    var albumSubExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(expandedRaw) {
        if (!expandedRaw) albumSubExpanded = false
    }

    // Back gesture: only intercepted while the fan is expanded (D-01a) — collapsed-FAB screen
    // back navigation must never be swallowed. While tier-2 is open, back collapses tier-2 only;
    // otherwise it collapses tier-1.
    BackHandler(enabled = expanded) {
        if (albumSubExpanded) albumSubExpanded = false else expandedRaw = false
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim — collapses FAB on tap-outside
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        expandedRaw = false
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(16.dp)
        ) {
            // Mini-FAB: "New tag" — dashboard-only optional topmost row (Phase 42-08 gap
            // closure). Organizational action, independent of card types — gated ONLY on
            // onNewTag != null so shared call sites (Browse) render nothing when absent.
            val newTagAction = onNewTag
            if (newTagAction != null) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    FabActionRow(
                        label = "New tag",
                        icon = Icons.Default.Label,
                        contentDescription = "New tag",
                        onClick = {
                            expandedRaw = false
                            newTagAction()
                        }
                    )
                }
            }

            // Mini-FAB: "Quick Take" — topmost option (above "Album"), added Phase 15
            // Gated to album-enabled modules only (isAlbumEnabled = allowedCardTypes.contains("ALBUM"))
            if (isAlbumEnabled) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    FabActionRow(
                        label = "Quick Take",
                        icon = Icons.Default.Bolt,
                        contentDescription = "Quick Take",
                        onClick = {
                            expandedRaw = false
                            onQuickTake()
                        }
                    )
                }
            }

            // Mini-FAB: "Album" (below "Quick Take") plus its tier-2 nested sub-fan (Camera /
            // Gallery), added Phase 6 / restructured Phase 64 (CAP-05, D-07). The tier-2 sub-fan
            // now fans out HORIZONTALLY to the left of the Album pill (a perpendicular side-fan)
            // instead of stacking vertically above it, so it reads as visually distinct from the
            // tier-1 vertical fan. A single horizontal Row lays out, left to right:
            // [Camera, Gallery] [Album], anchored to the row's end (Arrangement.End) so Compose's
            // own layout pass positions the sub-fan automatically regardless of pill width — no
            // manual Modifier.offset magic numbers.
            if (isAlbumEnabled) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tier-2 nested album sub-fan: Camera + Gallery, fanning leftward from the
                    // Album pill (D-07). Single AnimatedVisibility wrapping both rows — avoids
                    // independent-timing flicker between the two sub-rows.
                    AnimatedVisibility(
                        visible = albumSubExpanded && expanded,
                        enter = slideInHorizontally { it } + fadeIn(),
                        exit = slideOutHorizontally { it } + fadeOut()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Sub-FAB: "Camera" — renders to the left of "Gallery"
                            FabActionRow(
                                label = "Camera",
                                icon = Icons.Default.CameraAlt,
                                contentDescription = "Take photo",
                                onClick = {
                                    expandedRaw = false
                                    onAlbumCamera()
                                }
                            )

                            // Sub-FAB: "Gallery"
                            FabActionRow(
                                label = "Gallery",
                                icon = Icons.Default.PhotoLibrary,
                                contentDescription = "Choose from gallery",
                                onClick = {
                                    expandedRaw = false
                                    onAlbumGallery()
                                }
                            )
                        }
                    }

                    // Mini-FAB: "Album" — above "Voice" (below "Quick Take")
                    AnimatedVisibility(
                        visible = expanded,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        FabActionRow(
                            label = "Album",
                            icon = Icons.Default.PhotoAlbum,
                            contentDescription = "Album",
                            onClick = {
                                albumSubExpanded = !albumSubExpanded
                            }
                        )
                    }
                }
            }

            // Mini-FAB: "Voice" — above "List"
            if (isVoiceEnabled) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    FabActionRow(
                        label = "Voice",
                        icon = Icons.Default.Mic,
                        contentDescription = "Voice",
                        onClick = {
                            expandedRaw = false
                            onCreateVoiceCard()
                        }
                    )
                }
            }

            // Mini-FAB: "List" — ABOVE "Text note" (visually closer to main FAB when expanded)
            if (isListEnabled) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    FabActionRow(
                        label = "List",
                        icon = Icons.Default.FormatListBulleted,
                        contentDescription = "List",
                        onClick = {
                            expandedRaw = false
                            onCreateListCard()
                        }
                    )
                }
            }

            // Mini-FAB: "Text note"
            if (isTextEnabled) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    FabActionRow(
                        label = "Text note",
                        icon = Icons.Default.Edit,
                        contentDescription = "Text note",
                        onClick = {
                            expandedRaw = false
                            onCreateTextCard()
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main FAB — rotates to X when expanded
            FloatingActionButton(
                onClick = { expandedRaw = !expandedRaw }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (expanded) "Close" else "Create",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

/**
 * Shared labeled mini-FAB row — a text label in a [Surface] card followed by a
 * [SmallFloatingActionButton]. Extracted from [ExpandableFab] (pure structural extraction,
 * no behavior/visual change) since every fan row (New tag / Quick Take / Camera / Gallery /
 * Album / Voice / List / Text note) shares this exact layout, differing only in label, icon,
 * and click action.
 */
@Composable
private fun FabActionRow(
    label: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        SmallFloatingActionButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}
