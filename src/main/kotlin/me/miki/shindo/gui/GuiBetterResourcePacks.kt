package me.miki.shindo.gui

import com.google.common.collect.Lists
import me.miki.shindo.management.addons.rpo.RPOAddon
import me.miki.shindo.management.addons.rpo.packs.ResourcePackListEntryFolder
import me.miki.shindo.management.addons.rpo.packs.ResourcePackListProcessor
import me.miki.shindo.management.addons.rpo.repository.ResourcePackRepositoryCustom
import me.miki.shindo.utils.file.FileUtils
import net.minecraft.client.gui.*
import net.minecraft.client.resources.*
import net.minecraft.client.resources.ResourcePackRepository.Entry
import org.lwjgl.input.Keyboard
import java.io.File
import java.util.Locale

class GuiBetterResourcePacks(private val parentScreen: GuiScreen) : GuiScreenResourcePacks(parentScreen) {
    private var searchField: GuiTextField? = null
    private lateinit var guiPacksAvailable: GuiResourcePackAvailable
    private lateinit var guiPacksSelected: GuiResourcePackSelected
    private lateinit var listPacksAvailable: MutableList<ResourcePackListEntry>
    private lateinit var listPacksAvailableProcessed: MutableList<ResourcePackListEntry>
    private lateinit var listPacksDummy: MutableList<ResourcePackListEntry>
    private lateinit var listPacksSelected: MutableList<ResourcePackListEntry>
    private lateinit var listProcessor: ResourcePackListProcessor

    private lateinit var currentFolder: File
    private var selectedButton: GuiButton? = null
    private var hasUpdated = false
    private var requiresReload = false

    private var currentSorter: Comparator<ResourcePackListEntry>? = null

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)

        buttonList.add(GuiOptionButton(1, width / 2 + 100 - 75, height - 26, I18n.format("gui.done")))
        buttonList.add(GuiOptionButton(2, width / 2 + 100 - 75, height - 48, I18n.format("resourcePack.openFolder")))

        buttonList.add(GuiOptionButton(10, width / 2 - 204, height - 26, 40, 20, "A-Z"))
        buttonList.add(GuiOptionButton(11, width / 2 - 204 + 44, height - 26, 40, 20, "Z-A"))
        buttonList.add(GuiOptionButton(20, width / 2 - 74, height - 26, 70, 20, "Refresh"))

        val prevText = searchField?.text ?: ""
        searchField = GuiTextField(30, fontRendererObj, width / 2 - 203, height - 46, 198, 16).apply {
            text = prevText
        }

        if (!requiresReload) {
            listPacksAvailable = Lists.newArrayListWithCapacity(8)
            listPacksAvailableProcessed = Lists.newArrayListWithCapacity(8)
            listPacksDummy = Lists.newArrayListWithCapacity(1)
            listPacksSelected = Lists.newArrayListWithCapacity(8)

            val repository = mc.resourcePackRepository
            repository.updateRepositoryEntriesAll()

            currentFolder = repository.dirResourcepacks
            listPacksAvailable.addAll(createAvailablePackList(repository))

            for (entry in Lists.reverse(repository.repositoryEntries)) {
                listPacksSelected.add(ResourcePackListEntryFound(this, entry))
            }

            listPacksSelected.add(ResourcePackListEntryDefault(this))
        }

        guiPacksAvailable = GuiResourcePackAvailable(mc, 200, height, listPacksAvailableProcessed)
        guiPacksAvailable.setSlotXBoundsFromLeft(width / 2 - 204)
        guiPacksAvailable.registerScrollButtons(7, 8)

        guiPacksSelected = GuiResourcePackSelected(mc, 200, height, listPacksSelected)
        guiPacksSelected.setSlotXBoundsFromLeft(width / 2 + 4)
        guiPacksSelected.registerScrollButtons(7, 8)

        listProcessor = ResourcePackListProcessor(listPacksAvailable, listPacksAvailableProcessed)
        val sorter = currentSorter ?: ResourcePackListProcessor.sortAZ.also { currentSorter = it }
        listProcessor.setSorter(sorter)
        listProcessor.setFilter(searchField?.text?.trim() ?: "")
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            20 -> refreshAvailablePacks()
            11 -> listProcessor.setSorter(ResourcePackListProcessor.sortZA.also { currentSorter = it })
            10 -> listProcessor.setSorter(ResourcePackListProcessor.sortAZ.also { currentSorter = it })
            2 -> FileUtils.openFolderAtPath(mc.resourcePackRepository.dirResourcepacks)
            1 -> {
                if (requiresReload) {
                    val selected = refreshSelectedPacks()
                    mc.gameSettings.resourcePacks.clear()
                    for (entry in selected) {
                        mc.gameSettings.resourcePacks.add(entry.resourcePackName)
                    }
                    RPOAddon.instance.get().options.updateEnabledPacks()
                    mc.gameSettings.saveOptions()
                    mc.refreshResources()
                }
                mc.displayGuiScreen(parentScreen)
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, buttonId: Int) {
        if (buttonId == 0) {
            for (button in buttonList) {
                if (button.mousePressed(mc, mouseX, mouseY)) {
                    selectedButton = button
                    button.playPressSound(mc.soundHandler)
                    actionPerformed(button)
                }
            }
        }

        guiPacksAvailable.mouseClicked(mouseX, mouseY, buttonId)
        guiPacksSelected.mouseClicked(mouseX, mouseY, buttonId)
        searchField?.mouseClicked(mouseX, mouseY, buttonId)

        listProcessor.refresh()
    }

    override fun handleMouseInput() {
        try {
            super.handleMouseInput()
        } catch (_: NullPointerException) {
        }

        guiPacksAvailable.handleMouseInput()
        guiPacksSelected.handleMouseInput()
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, eventType: Int) {
        if (eventType == 0 && selectedButton != null) {
            selectedButton?.mouseReleased(mouseX, mouseY)
            selectedButton = null
        }
    }

    override fun keyTyped(keyChar: Char, keyCode: Int) {
        super.keyTyped(keyChar, keyCode)

        val field = searchField
        if (field != null && field.isFocused) {
            field.textboxKeyTyped(keyChar, keyCode)
            listProcessor.setFilter(field.text.trim())
        }
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
    }

    override fun updateScreen() {
        searchField?.updateCursorCounter()

        if (hasUpdated) {
            hasUpdated = false
            refreshSelectedPacks()
            refreshAvailablePacks()
        }
    }

    fun moveToFolder(folder: File) {
        currentFolder = folder
        refreshSelectedPacks()
        refreshAvailablePacks()
    }

    private fun refreshAvailablePacks() {
        listPacksAvailable.clear()
        listPacksAvailable.addAll(createAvailablePackList(mc.resourcePackRepository))
        listProcessor.refresh()
    }

    private fun refreshSelectedPacks(): List<Entry> {
        val selected = Lists.newArrayListWithCapacity<Entry>(listPacksSelected.size)

        for (entry in listPacksSelected) {
            if (entry !is ResourcePackListEntryFound) {
                continue
            }

            val packEntry = entry.func_148318_i()
            if (packEntry != null) {
                selected.add(packEntry)
            }
        }

        selected.reverse()

        mc.resourcePackRepository.setRepositories(selected)
        return selected
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTickTime: Float) {
        drawBackground(0)
        guiPacksAvailable.drawScreen(mouseX, mouseY, partialTickTime)
        guiPacksSelected.drawScreen(mouseX, mouseY, partialTickTime)
        searchField?.drawTextBox()

        for (button in buttonList) {
            button.drawButton(mc, mouseX, mouseY)
        }
    }

    private fun createAvailablePackList(repository: ResourcePackRepository): MutableList<ResourcePackListEntryFound> {
        val list = Lists.newArrayList<ResourcePackListEntryFound>()

        val parent = currentFolder.parentFile
        if (repository.dirResourcepacks != currentFolder && parent != null) {
            list.add(ResourcePackListEntryFolder(this, parent, true))
        }

        val files = currentFolder.listFiles() ?: return list

        for (file in files) {
            if (file.isDirectory) {
                val mcmeta = File(file, "pack.mcmeta")

                if (mcmeta.isFile) {
                    val entry = ResourcePackRepositoryCustom.createEntryInstance(repository, file)
                    if (entry != null) {
                        try {
                            entry.updateResourcePack()
                            list.add(ResourcePackListEntryFound(this, entry))
                        } catch (_: Exception) {
                        }
                    }
                } else {
                    list.add(ResourcePackListEntryFolder(this, file))
                }
            } else if (file.name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
                val entry = ResourcePackRepositoryCustom.createEntryInstance(repository, file)
                if (entry != null) {
                    try {
                        entry.updateResourcePack()
                        list.add(ResourcePackListEntryFound(this, entry))
                    } catch (_: Exception) {
                    }
                }
            }
        }

        val selectedEntries = repository.repositoryEntries
        list.removeIf { entry ->
            val packEntry = entry.func_148318_i()
            packEntry != null && selectedEntries.contains(packEntry)
        }

        return list
    }

    override fun hasResourcePackEntry(entry: ResourcePackListEntry): Boolean {
        return listPacksSelected.contains(entry)
    }

    override fun getListContaining(entry: ResourcePackListEntry): MutableList<ResourcePackListEntry> {
        return if (hasResourcePackEntry(entry)) listPacksSelected else listPacksAvailable
    }

    override fun getAvailableResourcePacks(): MutableList<ResourcePackListEntry> {
        hasUpdated = true
        listPacksDummy.clear()
        return listPacksDummy
    }

    override fun getSelectedResourcePacks(): MutableList<ResourcePackListEntry> {
        hasUpdated = true
        return listPacksSelected
    }

    override fun markChanged() {
        requiresReload = true
    }
}


