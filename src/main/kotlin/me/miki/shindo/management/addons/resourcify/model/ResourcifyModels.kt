package me.miki.shindo.management.addons.resourcify.model

import java.io.File

data class ResourcifyProject(
    val service: ResourcifyServiceType,
    val projectId: String,
    val title: String,
    val description: String,
    val iconUrl: String?,
    val downloads: Int
)

data class ResourcifyVersion(
    val service: ResourcifyServiceType,
    val projectId: String,
    val versionId: String,
    val name: String,
    val fileName: String,
    val downloadUrl: String
)

data class ResourcifySearchPage(
    val results: List<ResourcifyProject>,
    val total: Int,
    val offset: Int
)

data class ResourcifyCategory(
    val id: String,
    val name: String
)

data class ResourcifyFilters(
    val categoryId: String? = null,
    val version: String? = null
)

data class ResourcifyUpdate(
    val entry: ResourcifyEntry,
    val version: ResourcifyVersion?
)

data class ResourcifyDownloadResult(
    val file: File?,
    val error: String?
)
