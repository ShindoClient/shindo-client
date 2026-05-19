package me.miki.shindo.management.remote.changelog

import me.miki.shindo.management.nanovg.font.Lucide
import java.awt.Color

enum class ChangelogType(
    val id: Int,
    val text: String,
    val color: Color,
) {
    ADDED(0, Lucide.PLUS, Color(0, 142, 65)),
    FIXED(1, Lucide.REFRESH_CCW, Color(207, 112, 3)),
    REMOVED(2, Lucide.MINUS, Color(209, 34, 34)),
    ERROR(999, Lucide.BAN, Color(143, 0, 0)),
    ;

    companion object {
        @JvmStatic
        fun getTypeById(id: Int): ChangelogType = entries.find { it.id == id } ?: ERROR
    }
}
