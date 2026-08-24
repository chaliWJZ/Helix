package com.helix.kuikly.components.richtext

/**
 * Markdown 解析器（纯 Kotlin，commonMain 可用）
 *
 * 将 Markdown 文本解析为元素列表，供 MarkdownRenderer 渲染
 * 支持：标题/段落/代码块/列表/引用/分割线
 */
object MarkdownParser {

    /** Markdown 元素模型 */
    sealed class MdElement {
        data class Heading(val level: Int, val text: String) : MdElement()
        data class Paragraph(val text: String) : MdElement()
        data class CodeBlock(val language: String, val code: String) : MdElement()
        data class ListItem(val ordered: Boolean, val index: Int, val text: String) : MdElement()
        data class Quote(val text: String) : MdElement()
        object Divider : MdElement()
    }

    /**
     * 解析 Markdown 文本
     */
    fun parse(markdown: String): List<MdElement> {
        val elements = mutableListOf<MdElement>()
        val lines = markdown.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            when {
                // 空行跳过
                line.isBlank() -> i++

                // 分割线
                line.trim() in setOf("---", "***", "___") -> {
                    elements.add(MdElement.Divider)
                    i++
                }

                // 代码块开始
                line.trimStart().startsWith("```") -> {
                    val language = line.trim().removePrefix("```").trim()
                    val code = StringBuilder()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                        code.appendLine(lines[i])
                        i++
                    }
                    i++ // 跳过结束的 ```
                    elements.add(MdElement.CodeBlock(language, code.toString().trimEnd()))
                }

                // 标题
                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length.coerceAtMost(6)
                    val text = line.dropWhile { it == '#' }.trim()
                    elements.add(MdElement.Heading(level, text))
                    i++
                }

                // 引用
                line.startsWith(">") -> {
                    val quoteText = StringBuilder(line.removePrefix(">").trim())
                    i++
                    // 连续引用行合并
                    while (i < lines.size && lines[i].startsWith(">")) {
                        quoteText.append("\n").append(lines[i].removePrefix(">").trim())
                        i++
                    }
                    elements.add(MdElement.Quote(quoteText.toString()))
                }

                // 无序列表
                line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                    val text = line.trimStart().drop(2).trim()
                    elements.add(MdElement.ListItem(ordered = false, index = 0, text = text))
                    i++
                }

                // 有序列表
                Regex("""^\d+\.\s""").containsMatchIn(line) -> {
                    val match = Regex("""^(\d+)\.\s""").find(line)!!
                    val index = match.groupValues[1].toIntOrNull() ?: 0
                    val text = line.drop(match.value.length).trim()
                    elements.add(MdElement.ListItem(ordered = true, index = index, text = text))
                    i++
                }

                // 普通段落（连续非空行合并）
                else -> {
                    val paragraph = StringBuilder(line)
                    i++
                    while (i < lines.size && lines[i].isNotBlank() &&
                        !lines[i].startsWith("#") && !lines[i].startsWith(">") &&
                        !lines[i].trimStart().startsWith("- ") &&
                        !lines[i].trimStart().startsWith("```") &&
                        !lines[i].trimStart().startsWith("* ") &&
                        !Regex("""^\d+\.\s""").containsMatchIn(lines[i])
                    ) {
                        paragraph.append(" ").append(lines[i].trim())
                        i++
                    }
                    elements.add(MdElement.Paragraph(stripInlineMarkdown(paragraph.toString())))
                }
            }
        }
        return elements
    }

    /**
     * 去除行内 Markdown 标记（粗体/斜体/行内代码/链接）
     * 简化处理：保留纯文本
     */
    private fun stripInlineMarkdown(text: String): String {
        return text
            .replace(Regex("""\*\*(.+?)\*\*"""), "$1")   // 粗体
            .replace(Regex("""\*(.+?)\*"""), "$1")         // 斜体
            .replace(Regex("""`(.+?)`"""), "$1")           // 行内代码
            .replace(Regex("""\[(.+?)]\((.+?)\)"""), "$1") // 链接 [text](url) → text
            .replace(Regex("""~~(.+?)~~"""), "$1")         // 删除线
    }
}
