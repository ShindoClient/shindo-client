package me.miki.shindo.utils

import me.miki.shindo.api.websocket.AccountType
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object AccountUtil {
    data class MojangProfile(
        val name: String,
        val id: String,
    )

    @JvmStatic
    fun detectAccountTypeFromUuid(uuid: UUID?): AccountType {
        if (uuid == null) return AccountType.LOCAL
        return when (uuid.version()) {
            4 -> AccountType.MICROSOFT
            3 -> AccountType.OFFLINE
            else -> AccountType.OFFLINE
        }
    }

    @JvmStatic
    fun hasValidAuthToken(
        token: String?,
        username: String?,
    ): Boolean {
        if (token.isNullOrBlank()) return false
        if (token == "0" || token == "-") return false
        if (!username.isNullOrBlank() && token.equals(username, ignoreCase = true)) return false
        return token.length >= 20 // real tokens are long hex/JWT strings
    }

    @JvmStatic
    fun fetchMojangProfile(username: String): MojangProfile? {
        return try {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$username")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode != 200) return null
            val json = conn.inputStream.bufferedReader().readText()
            val name = Regex("\"name\"\\s*:\\s*\"(.*?)\"").find(json)?.groupValues?.get(1)
            val id = Regex("\"id\"\\s*:\\s*\"(.*?)\"").find(json)?.groupValues?.get(1)
            if (name != null && id != null) MojangProfile(name, id) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun detectAccountTypeFromNetwork(username: String): AccountType =
        if (fetchMojangProfile(username) == null) {
            AccountType.OFFLINE
        } else {
            AccountType.MICROSOFT
        }

    @JvmStatic
    fun downloadPlayerHeadToCache(
        id: String,
        cacheDir: File,
        size: Int = 64,
    ): File? {
        return try {
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, "$id-$size.png")
            if (file.exists() && file.length() > 0) return file
            val url = URL("https://minotar.net/avatar/$id/$size.png")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.inputStream.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun getCachedPlayerHead(
        id: String,
        cacheDir: File,
        size: Int = 64,
    ): File? {
        val file = File(cacheDir, "$id-$size.png")
        return if (file.exists() && file.length() > 0) file else null
    }
}