package me.miki.shindo.utils.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import java.util.concurrent.TimeUnit

object CaffeineCacheFactory {

    @JvmStatic
    fun <K : Any, V : Any> newCache(spec: CaffeineCacheSpec = CaffeineCacheSpec()): Cache<K, V> {
        return configureBuilder<K, V>(spec).build()
    }

    @JvmStatic
    fun <K : Any, V : Any> newLoadingCache(
        spec: CaffeineCacheSpec = CaffeineCacheSpec(),
        loader: (K) -> V
    ): LoadingCache<K, V> {
        return configureBuilder<K, V>(spec).build(
            CacheLoader<K, V> { key -> loader(key) }
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K : Any, V : Any> configureBuilder(spec: CaffeineCacheSpec): Caffeine<K, V> {
        var builder = Caffeine.newBuilder()
            .maximumSize(spec.maximumSize)
            .expireAfterWrite(spec.expireAfterWriteDuration, spec.expireAfterWriteUnit)

        if (spec.expireAfterAccessDuration != null) {
            builder = builder.expireAfterAccess(spec.expireAfterAccessDuration, spec.expireAfterAccessUnit)
        }

        if (spec.recordStats) {
            builder = builder.recordStats()
        }

        return builder as Caffeine<K, V>
    }

    @JvmStatic
    fun minutes(value: Long): Pair<Long, TimeUnit> = Pair(value, TimeUnit.MINUTES)
}
