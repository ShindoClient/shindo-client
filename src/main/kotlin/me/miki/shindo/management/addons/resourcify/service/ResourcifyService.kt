package me.miki.shindo.management.addons.resourcify.service

import me.miki.shindo.management.addons.resourcify.model.ResourcifyCategory
import me.miki.shindo.management.addons.resourcify.model.ResourcifyConfig
import me.miki.shindo.management.addons.resourcify.model.ResourcifyFilters
import me.miki.shindo.management.addons.resourcify.model.ResourcifyResourceType
import me.miki.shindo.management.addons.resourcify.model.ResourcifySearchPage
import me.miki.shindo.management.addons.resourcify.model.ResourcifyServiceType
import me.miki.shindo.management.addons.resourcify.model.ResourcifyVersion

interface ResourcifyService {
    val serviceType: ResourcifyServiceType

    fun isEnabled(config: ResourcifyConfig): Boolean

    fun search(
        config: ResourcifyConfig,
        query: String,
        type: ResourcifyResourceType,
        offset: Int,
        filters: ResourcifyFilters
    ): ResourcifySearchPage?

    fun getLatestVersion(
        config: ResourcifyConfig,
        projectId: String,
        type: ResourcifyResourceType,
        version: String?
    ): ResourcifyVersion?

    fun getCategories(config: ResourcifyConfig, type: ResourcifyResourceType): List<ResourcifyCategory>

    fun getMinecraftVersions(config: ResourcifyConfig): List<String>
}
