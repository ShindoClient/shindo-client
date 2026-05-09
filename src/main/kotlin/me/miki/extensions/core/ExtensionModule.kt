package me.miki.extensions.core

import me.miki.extensions.ExtensionLibrary

data class ExtensionModule(
    val id: String,
    val name: String,
    val namespace: String,
    val version: String,
    val description: String,
    val tags: Set<String>,
    val metadata: Map<String, String>
)

class ExtensionModuleBuilder {
    var id: String? = null
    var name: String? = null
    var namespace: String = ExtensionLibrary.BASE_PACKAGE
    var version: String? = null
    var description: String = ""
    private val tags: MutableSet<String> = linkedSetOf()
    private val metadata: MutableMap<String, String> = linkedMapOf()

    fun tag(tag: String) {
        if (tag.isNotBlank()) tags += tag.trim()
    }

    fun tags(vararg values: String) {
        values.forEach { tag(it) }
    }

    fun metadata(key: String, value: String) {
        if (key.isNotBlank()) metadata[key] = value
    }

    fun metadata(other: Map<String, String>) {
        other.forEach { (key, value) -> metadata(key, value) }
    }

    internal fun build(): ExtensionModule {
        val resolvedId = id?.trim()?.takeIf(String::isNotEmpty)
            ?: throw IllegalStateException("ExtensionModule id is required")
        val resolvedName = name?.trim()?.takeIf(String::isNotEmpty)
            ?: throw IllegalStateException("ExtensionModule name is required")
        val resolvedVersion = version?.trim()?.takeIf(String::isNotEmpty)
            ?: throw IllegalStateException("ExtensionModule version is required")

        return ExtensionModule(
            id = resolvedId,
            name = resolvedName,
            namespace = namespace.trim().ifEmpty { ExtensionLibrary.BASE_PACKAGE },
            version = resolvedVersion,
            description = description.trim(),
            tags = tags.toSet(),
            metadata = metadata.toMap()
        )
    }
}
