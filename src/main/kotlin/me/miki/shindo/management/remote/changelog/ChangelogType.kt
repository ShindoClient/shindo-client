package me.miki.shindo.management.remote.changelog

import me.miki.shindo.management.nanovg.font.LegacyIcon
import java.awt.Color

enum class ChangelogType(
    val id: Int,
    val text: String,
    val color: Color,
) {
    ADDED(0, LegacyIcon.PLUS, Color(0, 142, 65)),
    FIXED(1, LegacyIcon.REFRESH, Color(207, 112, 3)),
    REMOVED(2, LegacyIcon.MINUS, Color(209, 34, 34)),
    ERROR(999, LegacyIcon.PROHIBITED, Color(143, 0, 0)),
    ;

    companion object {
        @JvmStatic
        fun getTypeById(id: Int): ChangelogType = values().find { it.id == id } ?: ERROR
    }
}
