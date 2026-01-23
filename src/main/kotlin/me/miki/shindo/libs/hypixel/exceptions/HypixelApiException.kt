package me.miki.shindo.libs.hypixel.exceptions

/**
 * Exceção customizada para erros da API do Hypixel
 */
class HypixelApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    companion object {
        const val PLAYER_NEVER_JOINED = "This player never joined Hypixel, it might be a nick."
        const val INVALID_API_KEY = "Invalid API key"
        const val RATE_LIMITED = "Rate limited"
        const val PLAYER_NOT_FOUND = "Player not found"
    }
}
