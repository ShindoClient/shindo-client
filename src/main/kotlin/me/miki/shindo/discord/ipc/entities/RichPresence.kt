package me.miki.shindo.discord.ipc.entities

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.time.OffsetDateTime

class RichPresence(
    private val state: String?,
    private val details: String?,
    private val startTimestamp: OffsetDateTime?,
    private val endTimestamp: OffsetDateTime?,
    private val largeImageKey: String?,
    private val largeImageText: String?,
    private val smallImageKey: String?,
    private val smallImageText: String?,
    private val partyId: String?,
    private val partySize: Int,
    private val partyMax: Int,
    private val matchSecret: String?,
    private val joinSecret: String?,
    private val spectateSecret: String?,
    private val instance: Boolean
) {

    fun toJson(): JsonObject {
        val timestampsObject = JsonObject().apply {
            startTimestamp?.let { addProperty("start", it.toEpochSecond()) }
            endTimestamp?.let { addProperty("end", it.toEpochSecond()) }
        }

        val assetsObject = JsonObject().apply {
            largeImageKey?.let { addProperty("large_image", it) }
            largeImageText?.let { addProperty("large_text", it) }
            smallImageKey?.let { addProperty("small_image", it) }
            smallImageText?.let { addProperty("small_text", it) }
        }

        val partyObject = if (partyId != null) {
            JsonObject().apply {
                addProperty("id", partyId)
                val partySizeArray = JsonArray()
                partySizeArray.add(JsonPrimitive(partySize))
                partySizeArray.add(JsonPrimitive(partyMax))
                add("size", partySizeArray)
            }
        } else null

        val secretsObject = JsonObject().apply {
            joinSecret?.let { addProperty("join", it) }
            spectateSecret?.let { addProperty("spectate", it) }
            matchSecret?.let { addProperty("match", it) }
        }

        return JsonObject().apply {
            state?.let { addProperty("state", it) }
            details?.let { addProperty("details", it) }
            add("timestamps", timestampsObject)
            add("assets", assetsObject)
            partyObject?.let { add("party", it) }
            add("secrets", secretsObject)
            addProperty("instance", instance)
        }
    }

    class Builder {
        private var state: String? = null
        private var details: String? = null
        private var startTimestamp: OffsetDateTime? = null
        private var endTimestamp: OffsetDateTime? = null
        private var largeImageKey: String? = null
        private var largeImageText: String? = null
        private var smallImageKey: String? = null
        private var smallImageText: String? = null
        private var partyId: String? = null
        private var partySize: Int = 0
        private var partyMax: Int = 0
        private var matchSecret: String? = null
        private var joinSecret: String? = null
        private var spectateSecret: String? = null
        private var instance: Boolean = false

        fun build(): RichPresence = RichPresence(
            state,
            details,
            startTimestamp,
            endTimestamp,
            largeImageKey,
            largeImageText,
            smallImageKey,
            smallImageText,
            partyId,
            partySize,
            partyMax,
            matchSecret,
            joinSecret,
            spectateSecret,
            instance
        )

        fun setState(state: String) = apply { this.state = state }
        fun setDetails(details: String) = apply { this.details = details }
        fun setStartTimestamp(startTimestamp: OffsetDateTime) = apply { this.startTimestamp = startTimestamp }
        fun setEndTimestamp(endTimestamp: OffsetDateTime) = apply { this.endTimestamp = endTimestamp }
        fun setLargeImage(largeImageKey: String, largeImageText: String? = null) = apply {
            this.largeImageKey = largeImageKey
            this.largeImageText = largeImageText
        }

        fun setSmallImage(smallImageKey: String, smallImageText: String? = null) = apply {
            this.smallImageKey = smallImageKey
            this.smallImageText = smallImageText
        }

        fun setParty(partyId: String, partySize: Int, partyMax: Int) = apply {
            this.partyId = partyId
            this.partySize = partySize
            this.partyMax = partyMax
        }

        fun setMatchSecret(matchSecret: String) = apply { this.matchSecret = matchSecret }
        fun setJoinSecret(joinSecret: String) = apply { this.joinSecret = joinSecret }
        fun setSpectateSecret(spectateSecret: String) = apply { this.spectateSecret = spectateSecret }
        fun setInstance(instance: Boolean) = apply { this.instance = instance }
    }
}
