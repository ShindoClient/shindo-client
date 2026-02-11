package me.miki.shindo.libs.hypixel.parser

import com.google.gson.JsonObject
import me.miki.shindo.libs.hypixel.data.HypixelPlayerData
import java.text.SimpleDateFormat
import java.util.*
class LoginData(private val playerData: HypixelPlayerData) {

    private val data: JsonObject = playerData.getPlayerData()
    fun getFormattedName(): String {
        val rank = getRank()
        val plusColor = getPlusColor()
        val displayName = playerData.getDisplayName() ?: "Unknown"

        return when {
            rank == "MVP_PLUS" && plusColor != null -> {
                val colorCode = when (plusColor.toLowerCase(Locale.ROOT)) {
                    "red" -> "§c"
                    "gold" -> "§6"
                    "green" -> "§a"
                    "yellow" -> "§e"
                    "light_purple" -> "§d"
                    "white" -> "§f"
                    "blue" -> "§9"
                    "dark_blue" -> "§1"
                    "dark_green" -> "§2"
                    "dark_aqua" -> "§3"
                    "dark_red" -> "§4"
                    "dark_purple" -> "§5"
                    "dark_gray" -> "§8"
                    "gray" -> "§7"
                    "black" -> "§0"
                    else -> "§b"
                }
                "§b[MVP$colorCode+§b] $displayName"
            }

            rank == "MVP_PLUS" -> "§b[MVP+] $displayName"
            rank == "MVP" -> "§b[MVP] $displayName"
            rank == "VIP_PLUS" -> "§a[VIP+] $displayName"
            rank == "VIP" -> "§a[VIP] $displayName"
            rank == "YOUTUBER" -> "§c[§fYOUTUBE§c] $displayName"
            rank == "HELPER" -> "§9[HELPER] $displayName"
            rank == "MODERATOR" -> "§2[MOD] $displayName"
            rank == "ADMIN" -> "§c[ADMIN] $displayName"
            rank == "OWNER" -> "§c[OWNER] $displayName"
            else -> displayName
        }
    }
    fun getDisplayName(): String {
        return playerData.getDisplayName() ?: "Unknown"
    }
    fun getRank(): String {
        val rank = data.get("rank")?.asString
        if (rank != null && rank != "NORMAL") return rank

        val packageRank = data.get("packageRank")?.asString
        if (packageRank != null && packageRank != "NONE") return packageRank

        val newPackageRank = data.get("newPackageRank")?.asString
        if (newPackageRank != null && newPackageRank != "NONE") return newPackageRank

        return "NORMAL"
    }
    fun getPlusColor(): String? {
        return data.get("rankPlusColor")?.asString
    }
    fun isOnline(): Boolean = playerData.isOnline()
    fun isHidingFromAPI(): Boolean = playerData.isHidingFromAPI()
    fun getLastLogout(): Long = playerData.getLastLogout()
    fun getLatestActivity(): String {
        val lastLogin = playerData.getLastLogin()
        val lastLogout = playerData.getLastLogout()

        return if (lastLogin > lastLogout) {
            "Last login"
        } else {
            "Last logout"
        }
    }
    fun getLatestActivityTime(): Long {
        val lastLogin = playerData.getLastLogin()
        val lastLogout = playerData.getLastLogout()

        return if (lastLogin > lastLogout) {
            lastLogin
        } else {
            lastLogout
        }
    }
    fun formatTimeSince(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val days = diff / (24 * 3600 * 1000)
        val hours = (diff % (24 * 3600 * 1000)) / (3600 * 1000)
        val minutes = (diff % (3600 * 1000)) / (60 * 1000)

        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
    fun formatLocalTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MM/dd/yyyy HH:mm")
        return format.format(date)
    }
}


