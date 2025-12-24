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

class CurseForgeService : ResourcifyService {

    override val serviceType = ResourcifyServiceType.CURSEFORGE

    override fun isEnabled(config: ResourcifyConfig): Boolean {
        return !config.curseForgeApiKey.isNullOrEmpty()
    }

    override fun search(
        config: ResourcifyConfig,
        query: String,
        type: ResourcifyResourceType,
        offset: Int,
        filters: ResourcifyFilters
    ): ResourcifySearchPage? {
        val key = config.curseForgeApiKey ?: return null
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        val classId = getClassId(type) ?: return null
        val version = filters.version ?: DEFAULT_MC_VERSION
        val categoryParam = filters.categoryId?.let { "&categoryIds=[$it]" } ?: ""
        val url = "$API/mods/search?gameId=432&classId=$classId&searchFilter=$encoded&index=$offset&pageSize=$PAGE_SIZE&sortField=2&sortOrder=desc&gameVersion=$version$categoryParam"
        val responseType = object : TypeToken<CurseForgeSearchResponse>() {}.type
        val response = ResourcifyHttp.getJson<CurseForgeSearchResponse>(url, responseType, mapOf("x-api-key" to key))
            ?: return null
        val results = response.data.map {
            ResourcifyProject(
                serviceType,
                it.id.toString(),
                it.name ?: "Unknown",
                it.summary ?: "",
                it.logo?.url,
                it.downloadCount ?: 0
            )
        }
        return ResourcifySearchPage(results, response.pagination?.totalCount ?: results.size, offset)
    }

    override fun getLatestVersion(
        config: ResourcifyConfig,
        projectId: String,
        type: ResourcifyResourceType,
        version: String?
    ): ResourcifyVersion? {
        val key = config.curseForgeApiKey ?: return null
        val targetVersion = version ?: DEFAULT_MC_VERSION
        val url = "$API/mods/$projectId/files?gameVersion=$targetVersion&pageSize=1&index=0"
        val responseType = object : TypeToken<CurseForgeFilesResponse>() {}.type
        val response = ResourcifyHttp.getJson<CurseForgeFilesResponse>(url, responseType, mapOf("x-api-key" to key))
            ?: return null
        val file = response.data.find { !it.downloadUrl.isNullOrEmpty() } ?: return null
        val fileName = file.fileName ?: file.displayName ?: "download.zip"
        return ResourcifyVersion(
            serviceType,
            projectId,
            file.id.toString(),
            file.displayName ?: fileName,
            fileName,
            file.downloadUrl ?: return null
        )
    }

    override fun getCategories(config: ResourcifyConfig, type: ResourcifyResourceType): List<ResourcifyCategory> {
        val key = config.curseForgeApiKey ?: return emptyList()
        val classId = getClassId(type) ?: return emptyList()
        val responseType = object : TypeToken<CurseForgeCategoryResponse>() {}.type
        val response = ResourcifyHttp.getJson<CurseForgeCategoryResponse>(
            "$API/categories?gameId=432",
            responseType,
            mapOf("x-api-key" to key)
        ) ?: return emptyList()
        return response.data.filter { it.classId == classId }
            .sortedBy { it.name }
            .map { ResourcifyCategory(it.id.toString(), it.name ?: it.id.toString()) }
    }

    override fun getMinecraftVersions(config: ResourcifyConfig): List<String> {
        val key = config.curseForgeApiKey ?: return emptyList()
        val responseType = object : TypeToken<CurseForgeVersionResponse>() {}.type
        val response = ResourcifyHttp.getJson<CurseForgeVersionResponse>(
            "$API/minecraft/version",
            responseType,
            mapOf("x-api-key" to key)
        ) ?: return emptyList()
        return response.data.filter { it.type == 1 }.mapNotNull { it.name }
    }

    private fun getClassId(type: ResourcifyResourceType): Int? {
        return when (type) {
            ResourcifyResourceType.RESOURCE_PACK -> 12
            ResourcifyResourceType.SHADER_PACK -> 6552
        }
    }

    private data class CurseForgeSearchResponse(
        val data: List<CurseForgeProject>,
        val pagination: CurseForgePagination?
    )

    private data class CurseForgeProject(
        val id: Int,
        val name: String?,
        val summary: String?,
        @SerializedName("downloadCount") val downloadCount: Int?,
        val logo: CurseForgeLogo?
    )

    private data class CurseForgeLogo(
        val url: String?
    )

    private data class CurseForgePagination(
        @SerializedName("totalCount") val totalCount: Int?
    )

    private data class CurseForgeCategoryResponse(
        val data: List<CurseForgeCategory>
    )

    private data class CurseForgeCategory(
        val id: Int,
        val name: String?,
        val classId: Int
    )

    private data class CurseForgeVersionResponse(
        val data: List<CurseForgeGameVersion>
    )

    private data class CurseForgeGameVersion(
        val id: Int,
        val name: String?,
        val type: Int
    )

    private data class CurseForgeFilesResponse(
        val data: List<CurseForgeFile>
    )

    private data class CurseForgeFile(
        val id: Int,
        val displayName: String?,
        val fileName: String?,
        val downloadUrl: String?
    )

    companion object {
        private const val API = "https://api.curseforge.com/v1"
        private const val PAGE_SIZE = 20
        private const val DEFAULT_MC_VERSION = "1.8.9"
    }
}
