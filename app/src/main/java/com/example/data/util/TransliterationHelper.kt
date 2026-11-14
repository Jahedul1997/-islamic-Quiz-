package com.example.data.util

/**
 * Converts standard Quranic Latin transliteration (from AlQuran Cloud `en.transliteration`)
 * into clean, readable Bengali phonetic pronunciation (বাংলা উচ্চারণ) when offline Bengali
 * transliteration is unavailable for downloaded Surahs.
 */
object TransliterationHelper {

    fun toBengaliPronunciation(latin: String): String {
        if (latin.isBlank()) return ""
        var s = latin.lowercase()
            .replace("'", "’")
            .replace("`", "’")
            .replace("sh", "শ")
            .replace("th", "ছ")
            .replace("kh", "খ")
            .replace("dh", "য")
            .replace("gh", "গ")
            .replace("zh", "ঝ")
            .replace("ch", "চ")
            .replace("aa", "আ")
            .replace("ee", "ঈ")
            .replace("oo", "ঊ")
            .replace("ai", "আই")
            .replace("au", "আউ")
            .replace("ay", "আই")
            .replace("aw", "আও")

        val words = s.split(Regex("\\s+")).filter { it.isNotBlank() }
        return words.joinToString(" ") { transliterateWord(it) }
    }

    private fun transliterateWord(w: String): String {
        val sb = StringBuilder()
        var i = 0
        var atStart = true
        while (i < w.length) {
            val c = w[i]
            when {
                c in listOf('শ', 'ছ', 'খ', 'য', 'গ', 'ঝ', 'চ') -> {
                    sb.append(c)
                    atStart = false
                }
                c == 'আ' -> {
                    sb.append(if (atStart) 'আ' else 'া')
                    atStart = false
                }
                c == 'ঈ' -> {
                    sb.append(if (atStart) 'ঈ' else 'ী')
                    atStart = false
                }
                c == 'ঊ' -> {
                    sb.append(if (atStart) 'ঊ' else 'ূ')
                    atStart = false
                }
                c == 'a' -> {
                    sb.append(if (atStart) 'আ' else 'া')
                    atStart = false
                }
                c == 'i' -> {
                    sb.append(if (atStart) 'ই' else 'ি')
                    atStart = false
                }
                c == 'u' -> {
                    sb.append(if (atStart) 'উ' else 'ু')
                    atStart = false
                }
                c == 'e' -> {
                    sb.append(if (atStart) 'এ' else 'ে')
                    atStart = false
                }
                c == 'o' -> {
                    sb.append(if (atStart) 'ও' else 'ো')
                    atStart = false
                }
                c == 'b' -> { sb.append('ব'); atStart = false }
                c == 't' -> { sb.append('ত'); atStart = false }
                c == 'j' -> { sb.append('জ'); atStart = false }
                c == 'h' -> { sb.append('হ'); atStart = false }
                c == 'd' -> { sb.append('দ'); atStart = false }
                c == 'r' -> { sb.append('র'); atStart = false }
                c == 'z' -> { sb.append('য'); atStart = false }
                c == 's' -> { sb.append('স'); atStart = false }
                c == 'f' -> { sb.append('ফ'); atStart = false }
                c == 'q' -> { sb.append('ক'); atStart = false }
                c == 'k' -> { sb.append('ক'); atStart = false }
                c == 'l' -> { sb.append('ল'); atStart = false }
                c == 'm' -> { sb.append('ম'); atStart = false }
                c == 'n' -> { sb.append('ন'); atStart = false }
                c == 'w' -> { sb.append('ও'); atStart = false }
                c == 'y' -> { sb.append('য়'); atStart = false }
                c == '-' -> { sb.append('-'); atStart = true }
                c == '’' -> { /* silent glottal / ayn indicator */ }
                else -> { sb.append(c); atStart = false }
            }
            i++
        }
        return sb.toString()
    }
}
