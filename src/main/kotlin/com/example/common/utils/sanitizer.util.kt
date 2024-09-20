package com.example.common.utils

import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import java.util.function.Predicate
import java.util.regex.Pattern

object HtmlSanitizer {
    private val COLOR_NAME: Pattern = Pattern.compile(
        "(?:aqua|black|blue|fuchsia|gray|grey|green|lime|maroon|navy|olive|purple"
                + "|red|silver|teal|white|yellow)"
    )

    private val COLOR_CODE: Pattern = Pattern.compile("(?:#(?:[0-9a-fA-F]{3}(?:[0-9a-fA-F]{3})?))")

    private val NUMBER_OR_PERCENT: Pattern = Pattern.compile("[0-9]+%?")

    private val PARAGRAPH: Pattern = Pattern.compile("(?:[\\p{L}\\p{N},'\\.\\s\\-_\\(\\)]|&[0-9]{2};)*")

    private val HTML_ID: Pattern = Pattern.compile("[a-zA-Z0-9\\:\\-_\\.]+")

    private val HTML_TITLE: Pattern = Pattern.compile("[\\p{L}\\p{N}\\s\\-_',:\\[\\]!\\./\\\\\\(\\)&]*")

    private val HTML_CLASS: Pattern = Pattern.compile("[a-zA-Z0-9\\s,\\-_]+")

    private val ONSITE_URL: Pattern = Pattern.compile("(?:[\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]+|\\#(\\w)+)")

    private val OFFSITE_URL: Pattern = Pattern.compile(
        ("\\s*(?:(?:ht|f)tps?://|mailto:)[\\p{L}\\p{N}]"
                + "[\\p{L}\\p{N}\\p{Zs}\\.\\#@\\$%\\+&;:\\-_~,\\?=/!\\(\\)]*+\\s*")
    )

    private val NUMBER: Pattern = Pattern.compile("[+-]?(?:(?:[0-9]+(?:\\.[0-9]*)?)|\\.[0-9]+)")

    private val NAME: Pattern = Pattern.compile("[a-zA-Z0-9\\-_\\$]+")

    private val ALIGN: Pattern = Pattern.compile("(?i)center|left|right|justify|char")

    private val VALIGN: Pattern = Pattern.compile("(?i)baseline|bottom|middle|top")

    private val COLOR_NAME_OR_COLOR_CODE: Predicate<String> = matchesEither(COLOR_NAME, COLOR_CODE)

    private val ONSITE_OR_OFFSITE_URL: Predicate<String> = matchesEither(ONSITE_URL, OFFSITE_URL)

    private val HISTORY_BACK: Pattern = Pattern.compile("(?:javascript:)?\\Qhistory.go(-1)\\E")

    private val ONE_CHAR: Pattern = Pattern.compile(".?", Pattern.DOTALL)


    private fun matchesEither(a: Pattern, b: Pattern): Predicate<String> {
        return Predicate<String> { s -> a.matcher(s).matches() || b.matcher(s).matches() }
    }

    fun sanitizeHTML(untrustedHTML: String?): String {
        val policy: PolicyFactory = HtmlPolicyBuilder()
            .allowAttributes("href").onElements("a")
            .allowStandardUrlProtocols()
            .allowElements(
                "table", "tr", "td", "thead", "tbody", "th", "hr", "font", "button",
                "input", "select", "option", "video", "audio"
            )
            .allowAttributes("b", "i", "strong", "code", "em", "pre").globally()
            .allowAttributes("class").globally()
            .allowAttributes("color").globally()
            .allowAttributes("bgcolor").globally()
            .allowAttributes("align").globally()
            .allowAttributes("target").globally()
            .allowAttributes("value").globally()
            .allowAttributes("name").globally()
            .allowAttributes("controls").globally()
            .allowAttributes("src").globally()
            .allowAttributes("autoplay").globally()
            .allowAttributes("muted").globally()
            .allowAttributes("loop").globally()
            .allowAttributes("poster").globally()
            .allowUrlProtocols("http", "https", "mailto", "chat")
            .allowAttributes("id").matching(HTML_ID).globally()
            .allowAttributes("class").matching(HTML_CLASS).globally()
            .allowAttributes("lang").matching(Pattern.compile("[a-zA-Z]{2,20}"))
            .globally()
            .allowAttributes("title").matching(HTML_TITLE).globally()
            .allowStyling()
            .allowAttributes("align").matching(ALIGN).onElements("p")
            .allowAttributes("for").matching(HTML_ID).onElements("label")
            .allowAttributes("color").matching(COLOR_NAME_OR_COLOR_CODE)
            .onElements("font")
            .allowAttributes("face")
            .matching(Pattern.compile("[\\w;, \\-]+"))
            .onElements("font")
            .allowAttributes("size").matching(NUMBER).onElements("font")
            .allowAttributes("href").matching(ONSITE_OR_OFFSITE_URL)
            .onElements("a")
            .allowStandardUrlProtocols()
            .allowAttributes("name").matching(NAME).onElements("a")
            .allowAttributes(
                "onfocus", "onblur", "onclick", "onmousedown", "onmouseup"
            )
            .matching(HISTORY_BACK).onElements("a")
            .allowAttributes("src").matching(ONSITE_OR_OFFSITE_URL)
            .onElements("img")
            .allowAttributes("name").matching(NAME)
            .onElements("img")
            .allowAttributes("alt").matching(PARAGRAPH)
            .onElements("img")
            .allowAttributes("border", "hspace", "vspace").matching(NUMBER)
            .onElements("img")
            .allowAttributes("border", "cellpadding", "cellspacing")
            .matching(NUMBER).onElements("table")
            .allowAttributes("bgcolor").matching(COLOR_NAME_OR_COLOR_CODE)
            .onElements("table")
            .allowAttributes("background").matching(ONSITE_URL)
            .onElements("table")
            .allowAttributes("align").matching(ALIGN)
            .onElements("table")
            .allowAttributes("noresize").matching(Pattern.compile("(?i)noresize"))
            .onElements("table")
            .allowAttributes("background").matching(ONSITE_URL)
            .onElements("td", "th", "tr")
            .allowAttributes("bgcolor").matching(COLOR_NAME_OR_COLOR_CODE)
            .onElements("td", "th")
            .allowAttributes("abbr").matching(PARAGRAPH)
            .onElements("td", "th")
            .allowAttributes("axis", "headers").matching(NAME)
            .onElements("td", "th")
            .allowAttributes("scope")
            .matching(Pattern.compile("(?i)(?:row|col)(?:group)?"))
            .onElements("td", "th")
            .allowAttributes("nowrap")
            .onElements("td", "th")
            .allowAttributes("height", "width").matching(NUMBER_OR_PERCENT)
            .onElements("table", "td", "th", "tr", "img")
            .allowAttributes("align").matching(ALIGN)
            .onElements(
                "thead", "tbody", "tfoot", "img",
                "td", "th", "tr", "colgroup", "col"
            )
            .allowAttributes("valign").matching(VALIGN)
            .onElements(
                "thead", "tbody", "tfoot",
                "td", "th", "tr", "colgroup", "col"
            )
            .allowAttributes("charoff").matching(NUMBER_OR_PERCENT)
            .onElements(
                "td", "th", "tr", "colgroup", "col",
                "thead", "tbody", "tfoot"
            )
            .allowAttributes("char").matching(ONE_CHAR)
            .onElements(
                "td", "th", "tr", "colgroup", "col",
                "thead", "tbody", "tfoot"
            )
            .allowAttributes("colspan", "rowspan").matching(NUMBER)
            .onElements("td", "th")
            .allowAttributes("span", "width").matching(NUMBER_OR_PERCENT)
            .onElements("colgroup", "col")
            .allowElements(
                "a", "label", "noscript", "h1", "h2", "h3", "h4", "h5", "h6",
                "p", "i", "b", "u", "strong", "em", "small", "big", "pre", "code",
                "cite", "samp", "sub", "sup", "strike", "center", "blockquote",
                "hr", "br", "col", "font", "map", "span", "div", "img",
                "ul", "ol", "li", "dd", "dt", "dl", "tbody", "thead", "tfoot",
                "table", "td", "th", "tr", "colgroup", "fieldset", "legend",
                "header-", "img"
            )
            .toFactory()

        return policy.sanitize(untrustedHTML)
    }
}
