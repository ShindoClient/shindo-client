package me.miki.shindo.injection.mixin.mixins.gui;

import me.miki.shindo.management.event.impl.EventLoadWorld;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiDownloadTerrain.class)
public class MixinGuiDownloadTerrain extends GuiScreen {

    /**
     * @author EldoDebug
     * @reason Clear the Button List + Implement the EventLoadWorld
     */
    @Overwrite
    public void initGui() {
        this.buttonList.clear();
        new EventLoadWorld().call();
    }
}
