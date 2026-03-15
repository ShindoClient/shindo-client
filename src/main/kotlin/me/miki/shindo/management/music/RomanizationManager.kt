package me.miki.shindo.management.music

import com.github.jikyo.romaji.Transliterator
import me.miki.shindo.logger.ShindoLogger.error
import me.miki.shindo.management.music.model.CachedRomanization
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

open class RomanizationManager {

    private val cache = ConcurrentHashMap<String, CachedRomanization>()
    private val executorService = Executors.newSingleThreadExecutor { r ->
        Thread(r).apply {
            name = "Romanization-Service"
            isDaemon = true
        }
    }

    fun containsJapaneseCharacters(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        for (c in text) {
            if ((c in '\u3040'..'\u309F') ||
                (c in '\u30A0'..'\u30FF') ||
                (c in '\u4E00'..'\u9FAF')
            ) {
                return true
            }
        }
        return false
    }


    open fun romanizeText(text: String?): CompletableFuture<String?>? {
        if (text == null || text.isEmpty() || !containsJapaneseCharacters(text)) {
            return CompletableFuture.completedFuture(text)
        }

        val cached = cache[text]
        return if (cached != null && !cached.isExpired()) {
            CompletableFuture.completedFuture<String>(cached.romanized)
        } else CompletableFuture.supplyAsync(Supplier<String> supplyAsync@{
            try {
                val results = Transliterator.transliterate(text)
                if (results != null && results.isNotEmpty()) {
                    val romanized = results[0]
                    cache[text] = CachedRomanization(romanized)
                    return@supplyAsync romanized
                }
            } catch (e: Exception) {
                error("Error romanizing text: " + e.message)
            }
            text
        }, executorService)
    }

    fun shutdown() {
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executorService.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}
