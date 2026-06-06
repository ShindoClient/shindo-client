package com.shindoclient.shindo.management.addons.builtin.rpo.packs

import com.shindoclient.shindo.gui.GuiBetterResourcePacks
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.io.File

class ResourcePackListEntryFolder(
    private val ownerScreen: GuiBetterResourcePacks,
    val folder: File,
    val isUp: Boolean = false,
) : ResourcePackListEntryCustom(ownerScreen) {
    companion object {
        private val folderResource = ResourceLocation("shindo/rpo/folder.png")
    }

    val folderName: String = if (isUp) ".." else folder.name

    override fun func_148313_c() {
        mc.textureManager.bindTexture(folderResource)
    }

    override fun func_148312_b(): String = folderName

    override fun func_148311_a(): String = if (isUp) "(Back)" else "(Folder)"

    override fun mousePressed(
        p_148278_1_: Int,
        p_148278_2_: Int,
        p_148278_3_: Int,
        p_148278_4_: Int,
        p_148278_5_: Int,
        p_148278_6_: Int,
    ): Boolean {
        ownerScreen.moveToFolder(folder)
        return true
    }

    override fun drawEntry(
        slotIndex: Int,
        x: Int,
        y: Int,
        listWidth: Int,
        slotHeight: Int,
        mouseX: Int,
        mouseY: Int,
        isSelected: Boolean,
    ) {
        func_148313_c()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 32, 32, 32f, 32f)
        GlStateManager.disableBlend()

        if ((mc.gameSettings.touchscreen || isSelected) && func_148310_d()) {
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544)
            GlStateManager.color(1f, 1f, 1f, 1f)
        }

        var s = func_148312_b()
        var width = mc.fontRendererObj.getStringWidth(s)

        if (width > 157) {
            s = mc.fontRendererObj.trimStringToWidth(
                s,
                157 - mc.fontRendererObj.getStringWidth("..."),
            ) + "..."
            width = mc.fontRendererObj.getStringWidth(s)
        }

        mc.fontRendererObj.drawStringWithShadow(s, (x + 32 + 2).toFloat(), (y + 1).toFloat(), 0xFFFFFF)

        val lines = mc.fontRendererObj.listFormattedStringToWidth(func_148311_a(), 157)
        for (i in 0 until 2.coerceAtMost(lines.size)) {
            mc.fontRendererObj.drawStringWithShadow(
                lines[i],
                (x + 32 + 2).toFloat(),
                (y + 12 + 10 * i).toFloat(),
                0x808080,
            )
        }
    }
}
