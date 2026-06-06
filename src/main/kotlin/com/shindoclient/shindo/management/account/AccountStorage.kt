package com.shindoclient.shindo.management.account

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.shindoclient.shindo.api.websocket.AccountType
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.account.data.SavedAccount
import com.shindoclient.shindo.management.file.FileManager
import java.io.File

class AccountStorage(
    fileManager: FileManager,
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file: File = File(fileManager.shindoDir, "accounts.json")

    fun load(): MutableList<SavedAccount> {
        if (!file.exists()) return mutableListOf()

        return try {
            val root = JsonParser.parseString(file.readText()).asJsonObject
            val arr = root.getAsJsonArray("accounts") ?: return mutableListOf()
            val result = mutableListOf<SavedAccount>()
            for (elem in arr) {
                val obj = elem.asJsonObject
                result.add(
                    SavedAccount(
                        id = obj.get("id").asString,
                        type = AccountType.valueOf(obj.get("type").asString),
                        username = obj.get("username").asString,
                        uuid = obj.get("uuid").asString,
                        accessToken = obj.get("accessToken").asString,
                        tokenExpiresAt = obj.get("tokenExpiresAt").asLong,
                        sessionJson = obj.get("sessionJson")?.asString ?: "",
                        active = obj.get("active").asBoolean,
                    ),
                )
            }
            result
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load accounts.json", e)
            mutableListOf()
        }
    }

    fun save(accounts: List<SavedAccount>) {
        try {
            if (!file.exists()) file.createNewFile()
            val root = JsonObject()
            val arr = JsonArray()
            for (acc in accounts) {
                val obj = JsonObject()
                obj.addProperty("id", acc.id)
                obj.addProperty("type", acc.type.name)
                obj.addProperty("username", acc.username)
                obj.addProperty("uuid", acc.uuid)
                obj.addProperty("accessToken", acc.accessToken)
                obj.addProperty("tokenExpiresAt", acc.tokenExpiresAt)
                obj.addProperty("sessionJson", acc.sessionJson)
                obj.addProperty("active", acc.active)
                arr.add(obj)
            }
            root.add("accounts", arr)
            file.writeText(gson.toJson(root))
        } catch (e: Exception) {
            ShindoLogger.error("Failed to save accounts.json", e)
        }
    }
}
