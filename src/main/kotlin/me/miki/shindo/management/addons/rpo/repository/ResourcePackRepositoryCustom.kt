package me.miki.shindo.management.addons.rpo.repository

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import me.miki.shindo.injection.interfaces.IMixinMinecraft
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.IResourcePack
import net.minecraft.client.resources.ResourcePackRepository
import net.minecraft.client.resources.data.IMetadataSerializer
import net.minecraft.client.settings.GameSettings
import java.io.File
import java.lang.reflect.Constructor

class ResourcePackRepositoryCustom(
    dirResourcepacks: File,
    dirServerResourcepacks: File,
    rprDefaultResourcePack: IResourcePack,
    rprMetadataSerializer: IMetadataSerializer,
    settings: GameSettings,
    enabledPacks: List<String>
) : ResourcePackRepository(
    dirResourcepacks,
    dirServerResourcepacks,
    rprDefaultResourcePack,
    rprMetadataSerializer,
    settings
) {

    private val repositoryEntries: MutableList<Entry> = Lists.newArrayList()
    private var isReady: Boolean = true
    private var repositoryEntriesAll: MutableList<Entry> = Lists.newArrayList()

    init {
        updateRepositoryEntriesAll()

        repositoryEntries.clear()
        for (pack in enabledPacks) {
            for (entry in repositoryEntriesAll) {
                if (entry.resourcePackName == pack && !repositoryEntries.contains(entry)) {
                    repositoryEntries.add(entry)
                }
            }
        }
    }

    override fun updateRepositoryEntriesAll() {
        if (!isReady) return

        val list: MutableList<Entry> = Lists.newArrayList()

        for (file in getResourcePackFiles(dirResourcepacks)) {
            val entry = createEntryInstance(this, file) ?: continue

            if (!repositoryEntriesAll.contains(entry)) {
                try {
                    entry.updateResourcePack()
                    list.add(entry)
                } catch (_: Exception) {

                }
            } else {
                val index = repositoryEntriesAll.indexOf(entry)
                if (index in 0 until repositoryEntriesAll.size) {
                    list.add(repositoryEntriesAll[index])
                }
            }
        }

        for (entry in repositoryEntriesAll) {
            if (!list.contains(entry)) {
                entry.closeResourcePack()
            }
        }

        repositoryEntriesAll = list
    }

    override fun getRepositoryEntriesAll(): List<Entry> {
        return ImmutableList.copyOf(repositoryEntriesAll)
    }

    override fun getRepositoryEntries(): List<Entry> {
        return ImmutableList.copyOf(repositoryEntries)
    }

    override fun setRepositories(repositories: List<Entry>) {
        repositoryEntries.clear()
        for (entry in repositories) {
            if (!repositoryEntries.contains(entry)) {
                repositoryEntries.add(entry)
            }
        }
    }

    private fun getResourcePackFiles(root: File): List<File> {
        return if (root.isDirectory) {
            val packFiles: MutableList<File> = Lists.newArrayList()
            for (file in root.listFiles() ?: emptyArray()) {
                if (file.isDirectory && !File(file, "pack.mcmeta").isFile) {
                    packFiles.addAll(getResourcePackFiles(file))
                } else {
                    packFiles.add(file)
                }
            }
            packFiles
        } else {
            emptyList()
        }
    }

    companion object {
        private var entryConstructor: Constructor<Entry>? = null

        @JvmStatic
        fun overrideRepository(enabledPacks: List<String>) {
            val mc = Minecraft.getMinecraft()

            try {
                val fileResourcepacks = (mc as IMixinMinecraft).getFileResourcepacks()
                val originalRepo = (mc as IMixinMinecraft).getMcResourcePackRepository() as ResourcePackRepository

                val customRepo = ResourcePackRepositoryCustom(
                    fileResourcepacks,
                    File(mc.mcDataDir, "server-resource-packs"),
                    (mc as IMixinMinecraft).getMcDefaultResourcePack() as net.minecraft.client.resources.DefaultResourcePack,
                    originalRepo.rprMetadataSerializer,
                    mc.gameSettings,
                    enabledPacks
                )

                (mc as IMixinMinecraft).setMcResourcePackRepository(customRepo)
            } catch (t: Throwable) {
                throw RuntimeException("Failed to override resource pack repository", t)
            }
        }

        @JvmStatic
        fun createEntryInstance(repository: ResourcePackRepository, file: File): Entry? {
            return try {
                if (entryConstructor == null) {
                    entryConstructor = Entry::class.java.getDeclaredConstructor(
                        ResourcePackRepository::class.java,
                        File::class.java
                    ).apply { isAccessible = true }
                }

                entryConstructor!!.newInstance(repository, file)
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
    }
}
