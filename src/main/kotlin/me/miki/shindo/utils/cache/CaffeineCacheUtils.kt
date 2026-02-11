package me.miki.shindo.utils.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.LoadingCache
import org.checkerframework.checker.nullness.qual.Nullable
import java.util.concurrent.ConcurrentMap

object CaffeineCacheUtils {

    @JvmStatic
    fun <K : Any, V : Any> getIfPresent(cache: Cache<K, V>, key: K): V? {
        return cache.getIfPresent(key)
    }

    @JvmStatic
    fun <K : Any, V : Any> getOrLoad(cache: Cache<K, V>, key: K, provider: () -> V): V {
        val current = cache.getIfPresent(key)
        if (current != null) {
            return current
        }
        val computed = provider()
        cache.put(key, computed)
        return computed
    }

    @JvmStatic
    fun <K : Any, V : Any> getOrLoad(cache: LoadingCache<K, V>, key: K): @Nullable V? {
        return cache.get(key)
    }

    @JvmStatic
    fun <K : Any, V : Any> put(cache: Cache<K, V>, key: K, value: V) {
        cache.put(key, value)
    }

    @JvmStatic
    fun <K : Any, V : Any> invalidate(cache: Cache<K, V>, key: K) {
        cache.invalidate(key)
    }

    @JvmStatic
    fun <K : Any, V : Any> clear(cache: Cache<K, V>) {
        cache.invalidateAll()
    }

    @JvmStatic
    fun <K : Any, V : Any> asMap(cache: Cache<K, V>): ConcurrentMap<K, V> {
        return cache.asMap()
    }
}
