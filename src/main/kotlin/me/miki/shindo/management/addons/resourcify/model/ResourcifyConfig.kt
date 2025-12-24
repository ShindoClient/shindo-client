package me.miki.shindo.management.addons.resourcify.model

data class ResourcifyConfig(
    var entries: MutableList<ResourcifyEntry> = mutableListOf(),
    var curseForgeApiKey: String? = null
)

data class ResourcifyEntry(
    val service: ResourcifyServiceType,
    val projectId: String,
    val versionId: String,
    val fileName: String,
    val filePath: String,
    val type: ResourcifyResourceType,
    val installedAt: Long
)
