package me.miki.shindo.management.account

import com.google.gson.JsonParser
import me.miki.shindo.api.websocket.AccountType
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.account.data.SavedAccount
import me.miki.shindo.management.file.FileManager
import net.minecraft.client.Minecraft
import net.minecraft.util.Session
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.java.JavaAuthManager
import net.raphimc.minecraftauth.msa.model.MsaDeviceCode
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService
import net.raphimc.minecraftauth.msa.service.util.ParamMsaAuthServiceSupplier
import java.lang.reflect.Field
import java.util.UUID
import java.util.function.Consumer

class AccountManager(
    fileManager: FileManager,
) {
    private val storage = AccountStorage(fileManager)
    private val accounts: MutableList<SavedAccount> = storage.load().toMutableList()

    fun getAccounts(): List<SavedAccount> = accounts.toList()

    fun getActiveAccount(): SavedAccount? = accounts.firstOrNull { it.active }

    fun switchAccount(id: String) {
        val target = accounts.firstOrNull { it.id == id } ?: return
        val updated = accounts.map { it.copy(active = it.id == id) }
        accounts.clear()
        accounts.addAll(updated)
        save()
        injectSession(target)
    }

    fun addMicrosoftAccount(
        onCode: (DeviceCodeInfo) -> Unit,
        onSuccess: (SavedAccount) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        try {
            val httpClient = MinecraftAuth.createHttpClient()

            val authManager =
                JavaAuthManager.create(httpClient).login(
                    ParamMsaAuthServiceSupplier { client, appConfig, param ->
                        DeviceCodeMsaAuthService(client, appConfig, param)
                    },
                    Consumer<MsaDeviceCode> { code ->
                        onCode(
                            DeviceCodeInfo(
                                userCode = code.userCode,
                                verificationUrl = code.verificationUri,
                                directUrl = code.directVerificationUri,
                                expiresIn = code.expireTimeMs,
                            ),
                        )
                    },
                )

            // Eagerly request tokens so they are all included in the serialized chain
            val token = authManager.minecraftToken.upToDate
            val profile = authManager.minecraftProfile.upToDate

            val acc =
                SavedAccount(
                    id = UUID.randomUUID().toString(),
                    type = AccountType.MICROSOFT,
                    username = profile.name,
                    uuid = profile.id.toString().replace("-", ""),
                    accessToken = token.token,
                    tokenExpiresAt = token.expireTimeMs,
                    sessionJson = JavaAuthManager.toJson(authManager).toString(),
                    active = false,
                )

            // Persist whenever the library refreshes tokens in the background
            authManager.changeListeners.add {
                updateSessionJson(acc.id, JavaAuthManager.toJson(authManager).toString())
            }

            addOrReplace(acc)
            onSuccess(acc)
        } catch (e: Exception) {
            ShindoLogger.error("Microsoft auth failed", e)
            onFailure(e)
        }
    }

    fun addOfflineAccount(username: String): SavedAccount {
        val uuid =
            UUID
                .nameUUIDFromBytes(
                    "OfflinePlayer:$username".toByteArray(Charsets.UTF_8),
                ).toString()
                .replace("-", "")

        val acc =
            SavedAccount(
                id = UUID.randomUUID().toString(),
                type = AccountType.OFFLINE,
                username = username,
                uuid = uuid,
                accessToken = "-",
                tokenExpiresAt = 0L,
                sessionJson = "",
                active = false,
            )
        addOrReplace(acc)
        return acc
    }

    fun refreshIfNeeded(id: String): SavedAccount {
        val acc =
            accounts.firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("Account $id not found")

        if (acc.type == AccountType.OFFLINE || acc.sessionJson.isEmpty()) return acc

        val fiveMinutes = 5L * 60L * 1000L
        if (System.currentTimeMillis() < acc.tokenExpiresAt - fiveMinutes) return acc

        return try {
            val httpClient = MinecraftAuth.createHttpClient()
            val authManager =
                JavaAuthManager.fromJson(
                    httpClient,
                    JsonParser.parseString(acc.sessionJson).asJsonObject,
                )

            val token = authManager.minecraftToken.upToDate
            val profile = authManager.minecraftProfile.upToDate

            val updated =
                acc.copy(
                    username = profile.name,
                    accessToken = token.token,
                    tokenExpiresAt = token.expireTimeMs,
                    sessionJson = JavaAuthManager.toJson(authManager).toString(),
                )
            replaceById(updated)
            save()
            updated
        } catch (e: Exception) {
            ShindoLogger.error("Token refresh failed for ${acc.username} — user may need to re-login", e)
            acc
        }
    }

    fun removeAccount(id: String) {
        val wasActive = accounts.firstOrNull { it.id == id }?.active == true
        accounts.removeAll { it.id == id }
        if (wasActive && accounts.isNotEmpty()) {
            val first = accounts[0].copy(active = true)
            accounts[0] = first
            injectSession(first)
        }
        save()
    }

    fun injectSession(account: SavedAccount) {
        try {
            val mc = Minecraft.getMinecraft()
            val token = if (account.type == AccountType.OFFLINE) "-" else account.accessToken
            val session = Session(account.username, account.uuid, token, "mojang")
            val field: Field = Minecraft::class.java.getDeclaredField("session")
            field.isAccessible = true
            field.set(mc, session)
            ShindoLogger.info("Session injected for ${account.username} (${account.type})")
        } catch (e: Exception) {
            ShindoLogger.error("Failed to inject session for ${account.username}", e)
        }
    }

    private fun addOrReplace(acc: SavedAccount) {
        // If the same MS/offline account is added again, replace instead of duplicating
        val idx = accounts.indexOfFirst { it.uuid == acc.uuid && it.type == acc.type }
        if (idx >= 0) accounts[idx] = acc else accounts.add(acc)
        save()
    }

    private fun replaceById(updated: SavedAccount) {
        val idx = accounts.indexOfFirst { it.id == updated.id }
        if (idx >= 0) accounts[idx] = updated
    }

    private fun updateSessionJson(
        id: String,
        json: String,
    ) {
        val idx = accounts.indexOfFirst { it.id == id }
        if (idx >= 0) {
            accounts[idx] = accounts[idx].copy(sessionJson = json)
            save()
        }
    }

    private fun save() = storage.save(accounts)

    data class DeviceCodeInfo(
        val userCode: String,
        val verificationUrl: String,
        val directUrl: String,
        val expiresIn: Long,
    )
}
