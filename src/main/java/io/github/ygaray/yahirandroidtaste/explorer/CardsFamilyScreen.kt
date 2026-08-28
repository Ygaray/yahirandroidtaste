package io.github.ygaray.yahirandroidtaste.explorer

import android.content.Context
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ACCENT_COLORS
import io.github.ygaray.yahirandroidtaste.component.AdaptiveMediaPreview
import io.github.ygaray.yahirandroidtaste.component.AlbumCard
import io.github.ygaray.yahirandroidtaste.component.CardBase
import io.github.ygaray.yahirandroidtaste.component.CardQuickView
import io.github.ygaray.yahirandroidtaste.component.CardTagRow
import io.github.ygaray.yahirandroidtaste.component.CardTypeChip
import io.github.ygaray.yahirandroidtaste.component.CountBadge
import io.github.ygaray.yahirandroidtaste.component.ListCard
import io.github.ygaray.yahirandroidtaste.component.TagListItem
import io.github.ygaray.yahirandroidtaste.component.TextCard
import io.github.ygaray.yahirandroidtaste.component.VoiceCard
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import io.github.ygaray.yahirandroidtaste.model.TagManagementUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceClipUiModel
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.TactileType
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.File

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place Cards' `states`/`content` are
 * enriched without touching the shared registry file. Order preserved exactly from the
 * pre-Phase-62-Plan-02 inline registry list.
 *
 * D-03 states axis (fixed order: Default -> Pressed / Selected -> Disabled -> Focused): every
 * Cards component's `isPinned`/`isFavorite` toggle is the applicable "Pressed / Selected" axis
 * (a pinned/favorited card is the closest analog this API surface exposes to a highlighted/
 * selected state) — Default renders the neutral unpinned/unfavorited instance. None of the 7
 * Cards components expose an `enabled` flag or a focus-visual param, so Disabled and Focused are
 * "Not applicable" (`render = null`) on every entry — a real API-surface fact, not an omission.
 *
 * WR-02 fix: each entry's Playground `Control` is hoisted to a single shared `private val`
 * referenced by both `controls` and `preview` (mirroring `ChipsFamilyScreen`'s
 * `tagSortModeControl`), so the label string is a single source of truth and cannot drift between
 * the two call sites.
 */
private val cardBaseTactileDepthControl = Control.Toggle(label = "Tactile depth")
private val textCardPinnedControl = Control.Toggle(label = "Pinned")
private val listCardPinnedControl = Control.Toggle(label = "Pinned")
private val albumCardPinnedControl = Control.Toggle(label = "Pinned")
private val voiceCardPinnedControl = Control.Toggle(label = "Pinned")
private val cardQuickViewPinnedControl = Control.Toggle(label = "Pinned")
private val cardQuickViewFavoriteControl = Control.Toggle(label = "Favorite")

internal val cardsFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "CardBase",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { CardBasePreview(tactileDepth = true) },
            // Pressed / Selected: CardBase exposes no selection flag — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: CardBase exposes no `enabled` flag — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed on CardBase — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { CardBaseVariants() },
        controls = listOf(cardBaseTactileDepthControl),
        preview = { state ->
            CardBasePreview(tactileDepth = state.boolean(cardBaseTactileDepthControl))
        }
    ),
    ComponentRegistry.Entry(
        name = "CardTypeChip",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") {
                CardTypeChipPreview(accent = Color(ACCENT_COLORS[7].light))
            },
            // Pressed / Selected: CardTypeChip exposes no selection flag — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: CardTypeChip exposes no `enabled` flag — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed on CardTypeChip — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { CardTypeChipVariants() }
    ),
    ComponentRegistry.Entry(
        name = "TextCard",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { TextCardPreview(isPinned = false) },
            ComponentRegistry.StateCell("Pressed / Selected") { TextCardPreview(isPinned = true) },
            // Disabled: TextCard exposes no `enabled` flag — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed on TextCard — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { TextCardVariants() },
        controls = listOf(textCardPinnedControl),
        preview = { state ->
            TextCardPreview(isPinned = state.boolean(textCardPinnedControl))
        }
    ),
    ComponentRegistry.Entry(
        name = "ListCard",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { ListCardPreview(isPinned = false) },
            ComponentRegistry.StateCell("Pressed / Selected") { ListCardPreview(isPinned = true) },
            // Disabled: ListCard exposes no `enabled` flag — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed on ListCard — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ListCardVariants() },
        controls = listOf(listCardPinnedControl),
        preview = { state ->
            ListCardPreview(isPinned = state.boolean(listCardPinnedControl))
        }
    ),
    ComponentRegistry.Entry(
        name = "AlbumCard",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { AlbumCardPreview(isPinned = false) },
            ComponentRegistry.StateCell("Pressed / Selected") { AlbumCardPreview(isPinned = true) },
            // Disabled: AlbumCard exposes no `enabled` flag — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed on AlbumCard — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { AlbumCardVariants() },
        controls = listOf(albumCardPinnedControl),
        preview = { state ->
            AlbumCardPreview(isPinned = state.boolean(albumCardPinnedControl))
        }
    ),
    ComponentRegistry.Entry(
        name = "VoiceCard",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { VoiceCardPreview(isPinned = false) },
            ComponentRegistry.StateCell("Pressed / Selected") { VoiceCardPreview(isPinned = true) },
            // Disabled: VoiceCard exposes no `enabled` flag — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed on VoiceCard — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { VoiceCardVariants() },
        controls = listOf(voiceCardPinnedControl),
        preview = { state ->
            VoiceCardPreview(isPinned = state.boolean(voiceCardPinnedControl))
        }
    ),
    ComponentRegistry.Entry(
        name = "AdaptiveMediaPreview",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { AdaptiveMediaPreviewContent() },
            // Pressed / Selected: cell taps are display-only navigation callbacks (onCellTap/
            // onOverflowTap) — no selected-visual param exposed — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: no `enabled` param exposed — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { AdaptiveMediaPreviewVariants() }
    ),
    ComponentRegistry.Entry(
        name = "CardTagRow",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { CardTagRowContent() },
            // Pressed / Selected: every chip this row builds is hardcoded `isSelected = false` —
            // no caller-supplied selection param exists to exercise — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: no `enabled` param exposed — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { CardTagRowVariants() }
    ),
    ComponentRegistry.Entry(
        name = "CardQuickView",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") {
                CardQuickViewContent(isPinned = false, isFavorite = false)
            },
            ComponentRegistry.StateCell("Pressed / Selected") {
                CardQuickViewContent(isPinned = true, isFavorite = true)
            },
            // Disabled: CardQuickView exposes no `enabled` flag — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed on CardQuickView — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { CardQuickViewVariants() },
        controls = listOf(
            cardQuickViewPinnedControl,
            cardQuickViewFavoriteControl
        ),
        preview = { state ->
            CardQuickViewContent(
                isPinned = state.boolean(cardQuickViewPinnedControl),
                isFavorite = state.boolean(cardQuickViewFavoriteControl)
            )
        }
    ),
    ComponentRegistry.Entry(
        name = "CountBadge",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { CountBadgeDefaultPreview() },
            // Pressed / Selected: CountBadge is a static badge — no interaction-state prop
            // exists to exercise — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: no `enabled` param exposed — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { CountBadgeVariants() }
    ),
    ComponentRegistry.Entry(
        name = "TagListItem",
        family = ExplorerFamilies.CARDS,
        states = listOf(
            ComponentRegistry.StateCell("Default") { TagListItemDefaultPreview() },
            // Pressed / Selected: TagListItem exposes no selection prop — its tap is a plain
            // navigation callback (onClick), not a caller-supplied selected-visual — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: no `enabled` param exposed — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { TagListItemVariants() }
    )
)

/**
 * Cards family (SHOW-01, D-01): a registry-filtered row-list picker — tapping a row opens that
 * component's detail page (`detail/{component}`), which renders its States matrix + the Variants
 * section lifted in from this file's per-component `XxxVariants()` composables below.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { CardsFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.CARDS },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

// --- Per-component Variants wrappers (D-02) -----------------------------------------------
// Each groups its component's preserved Phase-61 curated fixtures, unchanged, into the detail
// page's Variants section body. No fixture is dropped, duplicated across components, or
// rewritten — the private XxxSection composables below still render exactly as they did in the
// pre-Phase-62 gallery.

@Composable
private fun TextCardVariants() {
    TextCardSection(label = "TextCard — pinned", title = ExplorerFakeData.SHORT_TITLE, isPinned = true)
    DividerRow()
    TextCardSection(label = "TextCard — unpinned, long title", title = ExplorerFakeData.LONG_TITLE, isPinned = false)
    DividerRow()
    TextCardSection(
        label = "TextCard — 4 tags (3 chips + \"+1\", siblings band active)",
        title = ExplorerFakeData.SHORT_TITLE,
        isPinned = false,
        tags = ExplorerFakeData.tagChips
    )
}

@Composable
private fun ListCardVariants() {
    ListCardSection(label = "ListCard — pinned checklist", title = ExplorerFakeData.SHORT_TITLE, isPinned = true)
    DividerRow()
    ListCardSection(label = "ListCard — unpinned, long title", title = ExplorerFakeData.LONG_TITLE, isPinned = false)
    DividerRow()
    ListCardSection(
        label = "ListCard — 3 tags (exactly 3 chips, no overflow, siblings band active)",
        title = ExplorerFakeData.SHORT_TITLE,
        isPinned = false,
        tags = ExplorerFakeData.tagChips.take(3)
    )
}

@Composable
private fun AlbumCardVariants() {
    AlbumCardSection(label = "AlbumCard — pinned", title = ExplorerFakeData.SHORT_TITLE, isPinned = true)
    DividerRow()
    AlbumCardSection(label = "AlbumCard — unpinned, long title, no photos", title = ExplorerFakeData.LONG_TITLE, isPinned = false)
    DividerRow()
    AlbumCardSection(
        label = "AlbumCard — 1 tag (chip present, siblings band inactive per D-08)",
        title = ExplorerFakeData.SHORT_TITLE,
        isPinned = false,
        tags = ExplorerFakeData.tagChips.take(1)
    )
}

/**
 * Writes a generated `.bin` amplitude-samples demo file into [context]'s cacheDir (Phase 129
 * DS-03 D-02, 129-REVIEWS.md cycle-1 MEDIUM) — a deterministic decaying-sine synthetic envelope,
 * so the Explorer gallery's clip-list states show one genuinely painted waveform (via the shared
 * `WaveformCanvas`, through the exact same [io.github.ygaray.yahirandroidtaste.component.VoiceClipRow]
 * decode path a real caller would use) alongside the deliberate blank-track null-`samplesPath`
 * state. Same layout `readAmplitudeBars` consumes: a 4-byte count followed by that many 4-byte
 * floats. Written only when absent — no binary asset is committed; generating the bytes exercises
 * the identical production decode path and keeps the repo binary-free.
 */
private fun writeVoiceClipDemoSamplesFile(context: Context): String {
    val file = File(context.cacheDir, "voice_card_explorer_demo_samples.bin")
    if (!file.exists()) {
        val sampleCount = 120
        val values = FloatArray(sampleCount) { i ->
            val decay = 1f - (i / sampleCount.toFloat())
            val envelope = (kotlin.math.sin(i * 0.35) + 1.0).toFloat() / 2f
            (decay * envelope).coerceIn(0f, 1f)
        }
        DataOutputStream(file.outputStream().buffered()).use { dos ->
            dos.writeInt(sampleCount)
            values.forEach { dos.writeFloat(it) }
        }
    }
    return file.absolutePath
}

@Composable
private fun VoiceCardVariants() {
    VoiceCardSection(label = "VoiceCard — pinned", title = ExplorerFakeData.SHORT_TITLE, isPinned = true)
    DividerRow()
    VoiceCardSection(label = "VoiceCard — unpinned, long title, no waveform", title = ExplorerFakeData.LONG_TITLE, isPinned = false)
    DividerRow()
    VoiceCardSection(
        label = "VoiceCard — 0 tags (no tag row rendered, per D-04)",
        title = ExplorerFakeData.SHORT_TITLE,
        isPinned = false,
        tags = emptyList()
    )
    DividerRow()

    // Clip-list states (Phase 129 DS-03 D-02) — pre-satisfies Phase 130 success criterion 3
    // (the on-device gallery must show these states rendering correctly). The demo samples file
    // is generated off the main thread and only when absent; each row renders its documented
    // blank track until its own decode resolves (129-REVIEWS.md cycle-1 MEDIUM).
    val context = LocalContext.current
    var demoSamplesPath by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val path = withContext(Dispatchers.IO) { writeVoiceClipDemoSamplesFile(context) }
        demoSamplesPath = path
    }

    SectionLabel("VoiceCard — 1 clip (singular pill copy, single row, no overflow)")
    VoiceCardContent(
        title = ExplorerFakeData.SHORT_TITLE,
        isPinned = false,
        clips = listOf(
            VoiceClipUiModel(id = "clip-0", sortOrder = 0, durationMs = 42_000L, samplesPath = demoSamplesPath)
        )
    )
    DividerRow()

    SectionLabel("VoiceCard — 3 clips (plural pill copy, 2 rows + \"+1 more clip\" overflow)")
    VoiceCardContent(
        title = ExplorerFakeData.SHORT_TITLE,
        isPinned = false,
        clips = listOf(
            VoiceClipUiModel(id = "clip-0", sortOrder = 0, durationMs = 42_000L, samplesPath = demoSamplesPath),
            VoiceClipUiModel(id = "clip-1", sortOrder = 1, durationMs = 18_000L, samplesPath = null),
            VoiceClipUiModel(id = "clip-2", sortOrder = 2, durationMs = 65_000L, samplesPath = null)
        )
    )
    DividerRow()

    SectionLabel("VoiceCard — 9 clips (cap holds at 2 rows, plural overflow remainder)")
    VoiceCardContent(
        title = ExplorerFakeData.SHORT_TITLE,
        isPinned = false,
        clips = List(9) { i ->
            VoiceClipUiModel(
                id = "clip-$i",
                sortOrder = i,
                durationMs = 20_000L + i * 5_000L,
                samplesPath = if (i == 0) demoSamplesPath else null
            )
        }
    )
}

@Composable
private fun AdaptiveMediaPreviewVariants() {
    AdaptiveMediaPreviewSection()
}

@Composable
private fun CardTagRowVariants() {
    CardTagRowSection()
}

@Composable
private fun CardQuickViewVariants() {
    CardQuickViewSection()
}

@Composable
private fun CountBadgeVariants() {
    SectionLabel("CountBadge — low count")
    CountBadgeDefaultPreview()
    DividerRow()
    SectionLabel("CountBadge — 999+ overflow")
    CountBadge(count = 1200, tileAccentColor = CountBadgeFixtureAccentColor)
}

@Composable
private fun TagListItemVariants() {
    SectionLabel("TagListItem — plain tag")
    TagListItemDefaultPreview()
    DividerRow()
    SectionLabel("TagListItem — home tag with custom color")
    TagListItem(tag = TagListItemFixtureHomeTag, onClick = {})
}

// --- States-matrix preview helpers (D-03) -------------------------------------------------
// Lean, label-free previews consumed only by cardsFamilyEntries' StateCell renders above — the
// visible cell label ("Default"/"Pressed / Selected") is already supplied by StatesMatrixSection,
// so these intentionally skip the SectionLabel the XxxSection/Content composables below add for
// the Variants section.

@Composable
private fun TextCardPreview(isPinned: Boolean) {
    TextCardContent(title = ExplorerFakeData.SHORT_TITLE, isPinned = isPinned)
}

@Composable
private fun ListCardPreview(isPinned: Boolean) {
    ListCardContent(title = ExplorerFakeData.SHORT_TITLE, isPinned = isPinned)
}

@Composable
private fun AlbumCardPreview(isPinned: Boolean) {
    AlbumCardContent(title = ExplorerFakeData.SHORT_TITLE, isPinned = isPinned)
}

@Composable
private fun VoiceCardPreview(isPinned: Boolean) {
    VoiceCardContent(title = ExplorerFakeData.SHORT_TITLE, isPinned = isPinned)
}

@Composable
private fun AdaptiveMediaPreviewContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
    ) {
        AdaptiveMediaPreview(
            cells = ExplorerFakeData.mediaThumbnails,
            onCellTap = {},
            onOverflowTap = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun AdaptiveMediaPreviewSection() {
    SectionLabel("AdaptiveMediaPreview — 4+ photos with count badge")
    AdaptiveMediaPreviewContent()
}

@Composable
private fun CardTagRowContent() {
    CardTagRow(
        tags = ExplorerFakeData.tagChips,
        onTagClick = {},
        onSiblingsClick = {},
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun CardTagRowSection() {
    SectionLabel("CardTagRow — 4 tags (2 visible + \"+2\" overflow)")
    CardTagRowContent()
}

@Composable
private fun CardQuickViewContent(isPinned: Boolean, isFavorite: Boolean) {
    CardQuickView(
        title = ExplorerFakeData.SHORT_TITLE,
        createdAt = ExplorerFakeData.CREATED_AT,
        updatedAt = ExplorerFakeData.UPDATED_AT,
        isPinned = isPinned,
        isFavorite = isFavorite,
        tagContent = {
            CardTagRow(
                tags = ExplorerFakeData.tagChips.take(2),
                onTagClick = {},
                onSiblingsClick = {}
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(ExplorerFakeData.NOTE_BODY, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CardQuickViewSection() {
    SectionLabel("CardQuickView — display archetype")
    CardQuickViewContent(isPinned = true, isFavorite = true)
}

/** Fixture accent color shared by [CountBadgeDefaultPreview]/[CountBadgeVariants] (Phase 86). */
private val CountBadgeFixtureAccentColor = Color(0xFF6750A4)

@Composable
private fun CountBadgeDefaultPreview() {
    CountBadge(count = 42, tileAccentColor = CountBadgeFixtureAccentColor)
}

/** Fixture tag shared by [TagListItemDefaultPreview]/[TagListItemVariants] (Phase 86). */
private val TagListItemFixturePlainTag = TagManagementUiModel(
    id = "tag-plain",
    name = "Groceries",
    cardCount = 3,
    isHome = false,
    iconName = null,
    color = null
)

/** Fixture home tag with a non-null color, exercising TagListItem's custom-tint branch. */
private val TagListItemFixtureHomeTag = TagManagementUiModel(
    id = "tag-home",
    name = "Home",
    cardCount = 12,
    isHome = true,
    iconName = "star",
    color = 0xFF6750A4L
)

@Composable
private fun TagListItemDefaultPreview() {
    TagListItem(tag = TagListItemFixturePlainTag, onClick = {})
}

// --- CardBase depth-card fixture (Phase 129 DS-02, Tasks 1-2) -----------------------------
// Proves the new CardBase(accent, tactileDepth) params and the CardTypeChip + TactileType.
// CardTitle pairing render end to end. Not yet registered in cardsFamilyEntries — task 3 wires
// these into CardBaseVariants()/CardTypeChipVariants() and the registry.

@Composable
private fun CardBaseContent(
    tactileDepth: Boolean = false,
    accent: Color? = null,
    showChipHeader: Boolean = false
) {
    val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
    CardBase(
        openRowState = openRowState,
        tactileDepth = tactileDepth,
        accent = accent,
        headerContent = {
            if (showChipHeader) {
                CardTypeChip(
                    accent = accent,
                    modifier = Modifier.padding(start = Dimens.HorizontalPadding, top = Dimens.TopPadding)
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice")
                }
                Text(
                    text = ExplorerFakeData.SHORT_TITLE,
                    style = TactileType.CardTitle,
                    modifier = Modifier.padding(
                        start = Dimens.ContentSpacing,
                        top = Dimens.TopPadding,
                        bottom = Dimens.ContentSpacing
                    )
                )
            } else {
                Text(
                    text = ExplorerFakeData.SHORT_TITLE,
                    modifier = Modifier.padding(
                        start = Dimens.HorizontalPadding,
                        top = Dimens.TopPadding,
                        bottom = Dimens.ContentSpacing
                    )
                )
            }
        },
        bodyContent = {
            Text(
                text = ExplorerFakeData.NOTE_BODY,
                modifier = Modifier.padding(horizontal = Dimens.HorizontalPadding)
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun CardBaseSection(
    label: String,
    tactileDepth: Boolean = false,
    accent: Color? = null,
    showChipHeader: Boolean = false
) {
    SectionLabel(label)
    CardBaseContent(tactileDepth = tactileDepth, accent = accent, showChipHeader = showChipHeader)
}

@Composable
private fun CardBasePreview(tactileDepth: Boolean) {
    CardBaseContent(tactileDepth = tactileDepth, accent = Color(ACCENT_COLORS[7].light))
}

@Composable
private fun CardBaseVariants() {
    CardBaseSection(label = "CardBase — plain (both defaults)")
    DividerRow()
    CardBaseSection(
        label = "CardBase — depth, neutral spine (null accent)",
        tactileDepth = true,
        accent = null
    )
    DividerRow()
    CardBaseSection(
        label = "CardBase — depth, accent spine",
        tactileDepth = true,
        accent = Color(ACCENT_COLORS[7].light)
    )
    DividerRow()
    CardBaseSection(
        label = "CardBase — depth, accent + CardTypeChip + TactileType.CardTitle title",
        tactileDepth = true,
        accent = Color(ACCENT_COLORS[7].light),
        showChipHeader = true
    )
}

// --- CardTypeChip fixture (Phase 129 DS-02 Task 2) -----------------------------------------

@Composable
private fun CardTypeChipPreview(accent: Color?) {
    CardTypeChip(accent = accent) {
        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice")
    }
}

@Composable
private fun CardTypeChipVariants() {
    SectionLabel("CardTypeChip — accent")
    CardTypeChipPreview(accent = Color(ACCENT_COLORS[7].light))
    DividerRow()
    SectionLabel("CardTypeChip — neutral (null accent)")
    CardTypeChipPreview(accent = null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardsFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Cards") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}

@Composable
private fun TextCardContent(
    title: String,
    isPinned: Boolean,
    tags: List<TagChipUiModel> = emptyList()
) {
    val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
    TextCard(
        id = title,
        title = title,
        content = ExplorerFakeData.NOTE_BODY,
        categoryPath = "Notes / Groceries",
        createdAt = ExplorerFakeData.CREATED_AT,
        updatedAt = ExplorerFakeData.UPDATED_AT,
        isPinned = isPinned,
        isFavorite = false,
        onEdit = {},
        onDelete = {},
        onTogglePin = {},
        onToggleFavorite = {},
        onConfirmRename = {},
        openRowState = openRowState,
        tags = tags,
        onTagClick = {},
        onSiblingsClick = {},
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun TextCardSection(
    label: String,
    title: String,
    isPinned: Boolean,
    tags: List<TagChipUiModel> = emptyList()
) {
    SectionLabel(label)
    TextCardContent(title = title, isPinned = isPinned, tags = tags)
}

@Composable
private fun ListCardContent(
    title: String,
    isPinned: Boolean,
    tags: List<TagChipUiModel> = emptyList()
) {
    val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
    ListCard(
        id = title,
        title = title,
        subType = "CHECKBOX",
        items = ExplorerFakeData.checklistItems,
        categoryPath = null,
        createdAt = ExplorerFakeData.CREATED_AT,
        updatedAt = ExplorerFakeData.UPDATED_AT,
        isPinned = isPinned,
        isFavorite = false,
        onEdit = {},
        onDelete = {},
        onTogglePin = {},
        onToggleFavorite = {},
        onConfirmRename = {},
        onToggleItem = {},
        openRowState = openRowState,
        tags = tags,
        onTagClick = {},
        onSiblingsClick = {},
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ListCardSection(
    label: String,
    title: String,
    isPinned: Boolean,
    tags: List<TagChipUiModel> = emptyList()
) {
    SectionLabel(label)
    ListCardContent(title = title, isPinned = isPinned, tags = tags)
}

@Composable
private fun AlbumCardContent(
    title: String,
    isPinned: Boolean,
    tags: List<TagChipUiModel> = emptyList()
) {
    val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
    val thumbnails = if (isPinned) ExplorerFakeData.mediaThumbnails else ExplorerFakeData.emptyMediaThumbnails
    AlbumCard(
        id = title,
        title = title,
        isPinned = isPinned,
        isFavorite = false,
        thumbnailItems = thumbnails,
        onTap = {},
        onDelete = {},
        onRename = {},
        onTogglePin = {},
        onToggleFavorite = {},
        openRowState = openRowState,
        tags = tags,
        onTagClick = {},
        onSiblingsClick = {},
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun AlbumCardSection(
    label: String,
    title: String,
    isPinned: Boolean,
    tags: List<TagChipUiModel> = emptyList()
) {
    SectionLabel(label)
    AlbumCardContent(title = title, isPinned = isPinned, tags = tags)
}

@Composable
private fun VoiceCardContent(
    title: String,
    isPinned: Boolean,
    tags: List<TagChipUiModel> = emptyList(),
    clips: List<VoiceClipUiModel> = emptyList()
) {
    val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
    VoiceCard(
        id = title,
        title = title,
        durationMs = 65_000L,
        samplesPath = null,
        categoryPath = null,
        isPinned = isPinned,
        isFavorite = false,
        onTap = {},
        onDelete = {},
        onTogglePin = {},
        onToggleFavorite = {},
        onRenameOrTagsRequest = {},
        openRowState = openRowState,
        tags = tags,
        onTagClick = {},
        onSiblingsClick = {},
        modifier = Modifier.padding(horizontal = 16.dp),
        clips = clips
    )
}

@Composable
private fun VoiceCardSection(
    label: String,
    title: String,
    isPinned: Boolean,
    tags: List<TagChipUiModel> = emptyList(),
    clips: List<VoiceClipUiModel> = emptyList()
) {
    SectionLabel(label)
    VoiceCardContent(title = title, isPinned = isPinned, tags = tags, clips = clips)
}
