package me.miki.shindo.management.addons.resourcify.service

import me.miki.shindo.management.addons.resourcify.model.ResourcifyCategory
import me.miki.shindo.management.addons.resourcify.model.ResourcifyConfig
import me.miki.shindo.management.addons.resourcify.model.ResourcifyFilters
import me.miki.shindo.management.addons.resourcify.model.ResourcifyProject
import me.miki.shindo.management.addons.resourcify.model.ResourcifyResourceType
import me.miki.shindo.management.addons.resourcify.model.ResourcifySearchPage
import me.miki.shindo.management.addons.resourcify.model.ResourcifyServiceType
import me.miki.shindo.management.addons.resourcify.model.ResourcifyVersion
import me.miki.shindo.management.addons.resourcify.net.ResourcifyHttp

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class ModrinthService : ResourcifyService {

    override val serviceType = ResourcifyServiceType.MODRINTH

    override fun isEnabled(config: ResourcifyConfig): Boolean = true

    override fun search(
        config: ResourcifyConfig,
        query: String,
        type: ResourcifyResourceType,
        offset: Int,
        filters: ResourcifyFilters
    ): ResourcifySearchPage? {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        val facets = URLEncoder.encode(buildFacets(type, filters), StandardCharsets.UTF_8.name())
        val url = "$API/search?query=$encoded&limit=$PAGE_SIZE&offset=$offset&facets=$facets"
        val responseType = object : TypeToken<ModrinthSearchResponse>() {}.type
        val response = ResourcifyHttp.getJson<ModrinthSearchResponse>(url, responseType) ?: return null
        val results = response.hits.map {
            ResourcifyProject(
                serviceType,
                it.projectId,
                it.title ?: "Unknown",
                it.description ?: "",
                it.iconUrl,
                it.downloads ?: 0
            )
        }
        return ResourcifySearchPage(results, response.totalHits ?: results.size, offset)
    }

    override fun getLatestVersion(
        config: ResourcifyConfig,
        projectId: String,
        type: ResourcifyResourceType,
        version: String?
    ): ResourcifyVersion? {
        val loader = if (type == ResourcifyResourceType.SHADER_PACK) "optifine" else "minecraft"
        val targetVersion = version ?: DEFAULT_MC_VERSION
        val loadersParam = URLEncoder.encode("[\"$loader\"]", StandardCharsets.UTF_8.name())
        val versionsParam = URLEncoder.encode("[\"$targetVersion\"]", StandardCharsets.UTF_8.name())
        val url = "$API/project/$projectId/version?loaders=$loadersParam&game_versions=$versionsParam"
        val responseType = object : TypeToken<List<ModrinthVersion>>() {}.type
        val versions = ResourcifyHttp.getJson<List<ModrinthVersion>>(url, responseType) ?: return null
        val version = versions.find { it.files.isNotEmpty() } ?: return null
        val file = version.files.find { it.primary } ?: version.files.first()
        return ResourcifyVersion(
            serviceType,
            projectId,
            version.id,
            version.name ?: version.versionNumber ?: "Unknown",
            file.filename ?: "download.zip",
            file.url ?: return null
        )
    }

    override fun getCategories(config: ResourcifyConfig, type: ResourcifyResourceType): List<ResourcifyCategory> {
        val responseType = object : TypeToken<List<ModrinthCategory>>() {}.type
        val response = ResourcifyHttp.getJson<List<ModrinthCategory>>("$API/tag/category", responseType) ?: return emptyList()
        val targetType = if (type == ResourcifyResourceType.RESOURCE_PACK) "resourcepack" else "shader"
        return response.filter { it.projectType == targetType }
            .sortedBy { it.name }
            .map { ResourcifyCategory(it.name, it.name.capitalize()) }
    }

    override fun getMinecraftVersions(config: ResourcifyConfig): List<String> {
        val responseType = object : TypeToken<List<ModrinthGameVersion>>() {}.type
        val response = ResourcifyHttp.getJson<List<ModrinthGameVersion>>("$API/tag/game_version", responseType) ?: return emptyList()
        return response.filter { it.versionType == "release" }.map { it.version }
    }

    private fun buildFacets(type: ResourcifyResourceType, filters: ResourcifyFilters): String {
        val values = ArrayList<String>()
        when (type) {
            ResourcifyResourceType.RESOURCE_PACK -> values.add("[\"project_type:resourcepack\"]")
            ResourcifyResourceType.SHADER_PACK -> values.add("[\"project_type:shader\"]")
        }
        if (type == ResourcifyResourceType.SHADER_PACK) {
            values.add("[\"categories=optifine\"]")
        }
        val category = filters.categoryId
        if (!category.isNullOrBlank()) {
            values.add("[\"categories:$category\"]")
        }
        val version = filters.version ?: DEFAULT_MC_VERSION
        values.add("[\"versions:$version\"]")
        return "[" + values.joinToString(",") + "]"
    }

    private data class ModrinthSearchResponse(
        @SerializedName("hits") val hits: List<ModrinthSearchHit>,
        @SerializedName("total_hits") val totalHits: Int?
    )

    private data class ModrinthSearchHit(
        @SerializedName("project_id") val projectId: String,
        val title: String?,
        val description: String?,
        @SerializedName("icon_url") val iconUrl: String?,
        val downloads: Int?
    )

    private data class ModrinthCategory(
        val name: String,
        @SerializedName("project_type") val projectType: String
    )

    private data class ModrinthGameVersion(
        val version: String,
        @SerializedName("version_type") val versionType: String
    )

    private data class ModrinthVersion(
        val id: String,
        val name: String?,
        @SerializedName("version_number") val versionNumber: String?,
        val files: List<ModrinthFile>
    )

    private data class ModrinthFile(
        val url: String?,
        val filename: String?,
        val primary: Boolean
    )

    companion object {
        private const val API = "https://api.modrinth.com/v2"
        private const val PAGE_SIZE = 20
        private const val DEFAULT_MC_VERSION = "1.8.9"
    }
}
