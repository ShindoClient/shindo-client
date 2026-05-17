package me.miki.shindo.api.websocket

import java.util.*

enum class AccountType {
    LOCAL,
    MICROSOFT,
    OFFLINE;

    fun getWireValue(): String = name

    companion object {
        @JvmStatic
        fun from(raw: String?): AccountType {
            if (raw == null) return LOCAL
            val normalized = raw.trim().uppercase(Locale.ROOT)
            for (type in entries) {
                if (type.name == normalized) return type
            }
            return LOCAL
        }
    }
}
