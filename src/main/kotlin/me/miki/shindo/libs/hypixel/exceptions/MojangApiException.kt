package me.miki.shindo.libs.hypixel.exceptions

class MojangApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    companion object {
        const val INVALID_NAME = "Invalid Minecraft name"
        const val PLAYER_NOT_FOUND = "Player not found"
        const val INVALID_UUID = "Invalid UUID"
    }
}
