package io.github.ygaray.yahirandroidtaste.testsupport

import androidx.compose.ui.semantics.AccessibilityAction
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.AnnotatedString
import kotlin.math.roundToInt

/**
 * Shared semantics-tree helpers for the Phase 23 card and screen proofs (no pixel capture: it times
 * out under Robolectric here, so every "renders exactly as v2.5.1" claim is a structural one).
 *
 * - [normalizedTree] is the golden form: depth-first, one line per node, only a whitelist of config
 *   keys, no node ids and no lambda identity hashes, so it is deterministic run to run.
 * - [rawTree] is the leak-scan form: every config entry of every node, no whitelist.
 * - [boundsLine] pins a tagged node's unclipped position and size in px.
 */
internal object SemanticsDump {

    /** Config keys that appear in a normalized golden, in the (alphabetical) order they are printed. */
    private val WHITELIST = listOf(
        "ContentDescription",
        "Disabled",
        "EditableText",
        "Error",
        "Heading",
        "LiveRegion",
        "Password",
        "Role",
        "Selected",
        "StateDescription",
        "TestTag",
        "Text"
    )

    /** All roots of the composition, popups included (a dropdown menu is its own root). */
    fun roots(rule: ComposeContentTestRule): List<SemanticsNode> =
        rule.onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes()

    fun normalizedTree(root: SemanticsNode, textNormalizer: (String) -> String = { it }): String {
        val sb = StringBuilder()
        appendNormalized(root, 0, textNormalizer, sb)
        return sb.toString().trimEnd('\n')
    }

    fun allRoots(rule: ComposeContentTestRule, textNormalizer: (String) -> String = { it }): String =
        roots(rule).joinToString(separator = "\n---- root ----\n") { normalizedTree(it, textNormalizer) }

    fun rawTree(root: SemanticsNode): String {
        val sb = StringBuilder()
        appendRaw(root, sb)
        return sb.toString()
    }

    fun allRawRoots(rule: ComposeContentTestRule): String =
        roots(rule).joinToString(separator = "\n---- root ----\n") { rawTree(it) }

    /** `tag x=.. y=.. w=.. h=..` from the unmerged node, unclipped, rounded to integer px. */
    fun boundsLine(rule: ComposeContentTestRule, tag: String): String {
        val node = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        return "$tag x=${node.positionInRoot.x.roundToInt()} y=${node.positionInRoot.y.roundToInt()} " +
            "w=${node.size.width} h=${node.size.height}"
    }

    /** Unclipped right edge in px of a node, for window-containment checks. */
    fun rightEdge(interaction: SemanticsNodeInteraction): Float {
        val node = interaction.fetchSemanticsNode()
        return node.positionInRoot.x + node.size.width
    }

    private fun appendNormalized(
        node: SemanticsNode,
        depth: Int,
        textNormalizer: (String) -> String,
        sb: StringBuilder
    ) {
        val parts = node.config
            .filter { it.key.name in WHITELIST }
            .sortedBy { it.key.name }
            .map { (key, value) -> "${key.name}=${render(value, textNormalizer)}" }
        sb.append("  ".repeat(depth)).append("node").append(if (parts.isEmpty()) "" else " ")
            .append(parts.joinToString(" ")).append('\n')
        node.children.forEach { appendNormalized(it, depth + 1, textNormalizer, sb) }
    }

    private fun appendRaw(node: SemanticsNode, sb: StringBuilder) {
        node.config.forEach { (key, value) ->
            val rendered = if (value is AccessibilityAction<*>) "action(${value.label})" else value.toString()
            sb.append(key.name).append('=').append(rendered).append('\n')
        }
        sb.append("--\n")
        node.children.forEach { appendRaw(it, sb) }
    }

    private fun render(value: Any?, textNormalizer: (String) -> String): String = when (value) {
        is AccessibilityAction<*> -> "action"
        is AnnotatedString -> "[" + textNormalizer(value.text) + "]"
        is List<*> -> value.joinToString(separator = "|", prefix = "[", postfix = "]") { item ->
            if (item is AnnotatedString) textNormalizer(item.text) else textNormalizer(item.toString())
        }
        else -> textNormalizer(value.toString())
    }
}
