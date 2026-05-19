package me.miki.shindo.management.remote.news

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.network.HttpUtils
import java.util.concurrent.CopyOnWriteArrayList

class NewsManager {
    private val news = CopyOnWriteArrayList<News>()

    init {
        TaskExecutor.runAsync(ThreadPoolType.NETWORK) { loadNews() }
    }

    private fun loadNews() {
        val jsonObject = HttpUtils.readJson("https://cdn.shindoclient.com/data/news/news.json", null) ?: return
        val jsonArray = JsonUtils.getArrayProperty(jsonObject, "news")
        val gson = Gson()
        for (jsonElement in jsonArray) {
            val changelogJsonObject = gson.fromJson(jsonElement, JsonObject::class.java)
            news.add(
                News(
                    JsonUtils.getStringProperty(changelogJsonObject, "title", "null").toString(),
                    JsonUtils.getStringProperty(changelogJsonObject, "subtitle", "null").toString(),
                    JsonUtils.getStringProperty(changelogJsonObject, "body", "null").toString(),
                ),
            )
        }
    }

    fun getNews(): CopyOnWriteArrayList<News> = news
}
