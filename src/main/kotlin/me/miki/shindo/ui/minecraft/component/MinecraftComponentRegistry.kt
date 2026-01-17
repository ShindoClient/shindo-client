package me.miki.shindo.ui.minecraft.component

import net.minecraft.client.gui.*
import net.minecraft.client.gui.inventory.GuiContainer
import java.util.concurrent.ConcurrentHashMap

/**
 * Registro centralizado de renderizadores de componentes do Minecraft.
 * Permite registrar renderizadores customizados para diferentes tipos de componentes.
 */
object MinecraftComponentRegistry {
    
    private val buttonRenderer: (GuiButton, Int, Int, Float) -> Unit = { button, mouseX, mouseY, partialTicks ->
        MinecraftButtonRenderer.renderButton(button, mouseX, mouseY, partialTicks)
    }
    
    private val textFieldRenderer: (GuiTextField, Int, Int, Float) -> Unit = { textField, mouseX, mouseY, partialTicks ->
        MinecraftTextFieldRenderer.renderTextField(textField, mouseX, mouseY, partialTicks)
    }
    
    private val sliderRenderer: (GuiSlider, Int, Int, Float) -> Unit = { slider, mouseX, mouseY, partialTicks ->
        MinecraftSliderRenderer.renderSlider(slider, mouseX, mouseY, partialTicks)
    }
    
    private val labelRenderer: (GuiLabel, Int, Int, Float) -> Unit = { label, mouseX, mouseY, partialTicks ->
        MinecraftLabelRenderer.renderLabel(label, mouseX, mouseY, partialTicks)
    }
    
    private val optionButtonRenderer: (GuiOptionButton, Int, Int, Float) -> Unit = { button, mouseX, mouseY, partialTicks ->
        MinecraftOptionButtonRenderer.renderOptionButton(button, mouseX, mouseY, partialTicks)
    }
    
    private val listRenderer: (GuiListExtended, Int, Int, Float) -> Unit = { list, mouseX, mouseY, partialTicks ->
        MinecraftListRenderer.renderList(list, mouseX, mouseY, partialTicks)
    }
    
    private val containerRenderer: (GuiContainer, Int, Int, Float) -> Unit = { container, mouseX, mouseY, partialTicks ->
        MinecraftContainerRenderer.renderContainerBackground(container, mouseX, mouseY, partialTicks)
    }
    
    private val chatRenderer: (GuiChat, Int, Int, Float) -> Unit = { chat, mouseX, mouseY, partialTicks ->
        MinecraftChatRenderer.renderChatBackground(chat, mouseX, mouseY, partialTicks)
    }
    
    /**
     * Renderiza um botão do Minecraft.
     */
    fun renderButton(button: GuiButton, mouseX: Int, mouseY: Int, partialTicks: Float) {
        when (button) {
            is GuiOptionButton -> optionButtonRenderer(button, mouseX, mouseY, partialTicks)
            else -> buttonRenderer(button, mouseX, mouseY, partialTicks)
        }
    }
    
    /**
     * Renderiza um campo de texto do Minecraft.
     */
    fun renderTextField(textField: GuiTextField, mouseX: Int, mouseY: Int, partialTicks: Float) {
        textFieldRenderer(textField, mouseX, mouseY, partialTicks)
    }
    
    /**
     * Renderiza um slider do Minecraft.
     */
    fun renderSlider(slider: GuiSlider, mouseX: Int, mouseY: Int, partialTicks: Float) {
        sliderRenderer(slider, mouseX, mouseY, partialTicks)
    }
    
    /**
     * Renderiza um label do Minecraft.
     */
    fun renderLabel(label: GuiLabel, mouseX: Int, mouseY: Int, partialTicks: Float) {
        labelRenderer(label, mouseX, mouseY, partialTicks)
    }
    
    /**
     * Renderiza uma lista do Minecraft.
     */
    fun renderList(list: GuiListExtended, mouseX: Int, mouseY: Int, partialTicks: Float) {
        when (list) {
            is net.minecraft.client.gui.GuiResourcePackAvailable -> 
                MinecraftResourcePackListRenderer.renderAvailableList(list, mouseX, mouseY, partialTicks)
            is net.minecraft.client.gui.GuiResourcePackSelected -> 
                MinecraftResourcePackListRenderer.renderSelectedList(list, mouseX, mouseY, partialTicks)
            is net.minecraft.client.gui.ServerSelectionList -> 
                MinecraftServerListRenderer.renderServerList(list, mouseX, mouseY, partialTicks)
            else -> listRenderer(list, mouseX, mouseY, partialTicks)
        }
    }
    
    /**
     * Renderiza o fundo de um container do Minecraft.
     */
    fun renderContainerBackground(container: GuiContainer, mouseX: Int, mouseY: Int, partialTicks: Float) {
        containerRenderer(container, mouseX, mouseY, partialTicks)
    }
    
    /**
     * Renderiza o fundo do chat do Minecraft.
     */
    fun renderChatBackground(chat: GuiChat, mouseX: Int, mouseY: Int, partialTicks: Float) {
        chatRenderer(chat, mouseX, mouseY, partialTicks)
    }
    
    /**
     * Renderiza um slot de inventário.
     */
    fun renderSlot(container: GuiContainer, slot: net.minecraft.inventory.Slot, mouseX: Int, mouseY: Int, partialTicks: Float) {
        MinecraftSlotRenderer.renderSlot(container, slot, mouseX, mouseY, partialTicks)
    }
    
    /**
     * Renderiza um painel com scroll.
     */
    fun renderScrollPanel(screen: GuiScreen, x: Int, y: Int, width: Int, height: Int, scrollAmount: Float, maxScroll: Float) {
        MinecraftScrollPanelRenderer.renderScrollPanel(screen, x, y, width, height, scrollAmount, maxScroll)
    }
}
