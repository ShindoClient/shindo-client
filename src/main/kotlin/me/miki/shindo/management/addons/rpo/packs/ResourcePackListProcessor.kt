package me.miki.shindo.management.addons.rpo.packs

import net.minecraft.client.resources.ResourcePackListEntry
import net.minecraft.client.resources.ResourcePackListEntryFound
import java.util.*
import java.util.regex.Pattern

class ResourcePackListProcessor(
    private val sourceList: List<ResourcePackListEntry>,
    private val targetList: MutableList<ResourcePackListEntry>
) {

    private var sorter: Comparator<ResourcePackListEntry>? = null
    private var textFilter: Pattern? = null

    fun setSorter(comparator: Comparator<ResourcePackListEntry>?) {
        sorter = comparator
        refresh()
    }

    fun setFilter(text: String?) {
        textFilter = if (text.isNullOrEmpty()) {
            null
        } else {
            Pattern.compile("\\Q" + text.replace("*", "\\E.*\\Q") + "\\E", Pattern.CASE_INSENSITIVE)
        }
        refresh()
    }

    fun refresh() {
        targetList.clear()

        for (entry in sourceList) {
            if (checkFilter(name(entry)) || checkFilter(description(entry))) {
                targetList.add(entry)
            }
        }

        sorter?.let { Collections.sort(targetList, it) }
    }

    private fun checkFilter(entryText: String): Boolean {
        return textFilter == null || textFilter!!.matcher(entryText.lowercase(Locale.ENGLISH)).find()
    }

    companion object {
        @JvmField
        val sortAZ: Comparator<ResourcePackListEntry> =
            Comparator { entry1, entry2 ->
                String.CASE_INSENSITIVE_ORDER.compare(
                    nameSort(entry1, reverse = false),
                    nameSort(entry2, reverse = false)
                )
            }

        @JvmField
        val sortZA: Comparator<ResourcePackListEntry> =
            Comparator { entry1, entry2 ->
                -String.CASE_INSENSITIVE_ORDER.compare(
                    nameSort(entry1, reverse = true),
                    nameSort(entry2, reverse = true)
                )
            }

        private fun name(entry: ResourcePackListEntry): String {
            return when (entry) {
                is ResourcePackListEntryCustom -> entry.func_148312_b()
                is ResourcePackListEntryFound -> entry.func_148318_i().resourcePackName
                else -> "<INVALID>"
            }
        }

        private fun nameSort(entry: ResourcePackListEntry, reverse: Boolean): String {
            val pfx1 = if (!reverse) "a" else "z"
            val pfx2 = if (!reverse) "b" else "z"
            val pfx3 = if (!reverse) "z" else "a"

            if (entry is ResourcePackListEntryFolder) {
                return if (entry.isUp) "$pfx1${entry.folderName}" else "$pfx2${entry.folderName}"
            }

            return when (entry) {
                is ResourcePackListEntryCustom -> pfx3 + entry.func_148312_b()
                is ResourcePackListEntryFound -> pfx3 + entry.func_148318_i().resourcePackName
                else -> "$pfx3<INVALID>"
            }
        }

        private fun description(entry: ResourcePackListEntry): String {
            return when (entry) {
                is ResourcePackListEntryCustom -> entry.func_148311_a()
                is ResourcePackListEntryFound -> entry.func_148318_i().texturePackDescription
                else -> "<INVALID>"
            }
        }
    }
}


