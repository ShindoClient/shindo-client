package me.miki.shindo.management.addons.rpo.packs;

import me.miki.shindo.gui.GuiBetterResourcePacks;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.List;

public class ResourcePackListEntryFolder extends ResourcePackListEntryCustom {
    private static final ResourceLocation folderResource = new ResourceLocation("shindo/rpo/folder.png"); // http://www.iconspedia.com/icon/folion-icon-27237.html
    public final File folder;
    public final String folderName;
    public final boolean isUp;
    private final GuiBetterResourcePacks ownerScreen;

    public ResourcePackListEntryFolder(GuiBetterResourcePacks ownerScreen, File folder) {
        super(ownerScreen);
        this.ownerScreen = ownerScreen;
        this.folder = folder;
        this.folderName = folder.getName();
        this.isUp = false;
    }

    public ResourcePackListEntryFolder(GuiBetterResourcePacks ownerScreen, File folder, boolean isUp) {
        super(ownerScreen);
        this.ownerScreen = ownerScreen;
        this.folder = folder;
        this.folderName = "..";
        this.isUp = isUp;
    }

    @Override
    public void func_148313_c() {
        mc.getTextureManager().bindTexture(folderResource);
    }

    @Override
    public String func_148312_b() {
        return folderName;
    }

    @Override
    public String func_148311_a() {
        return isUp ? "(Back)" : "(Folder)";
    }

    @Override
    public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_) {
        ownerScreen.moveToFolder(folder);
        return true;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        this.func_148313_c();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0F, 0F, 32, 32, 32F, 32F);
        GlStateManager.disableBlend();

        int i2;

        if ((mc.gameSettings.touchscreen || isSelected) && this.func_148310_d()) {
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1F, 1F, 1F, 1F);
        }

        String s = this.func_148312_b();
        i2 = mc.fontRendererObj.getStringWidth(s);

        if (i2 > 157) {
            s = mc.fontRendererObj.trimStringToWidth(s, 157 - mc.fontRendererObj.getStringWidth("...")) + "...";
        }

        mc.fontRendererObj.drawStringWithShadow(s, x + 32 + 2, y + 1, 16777215);
        List list = mc.fontRendererObj.listFormattedStringToWidth(this.func_148311_a(), 157);

        for (int j2 = 0; j2 < 2 && j2 < list.size(); ++j2) {
            mc.fontRendererObj.drawStringWithShadow((String) list.get(j2), x + 32 + 2, y + 12 + 10 * j2, 8421504);
        }
    }
}