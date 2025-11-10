package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import net.minecraft.client.network.NetworkPlayerInfo;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class PlayerListMod extends HUDMod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.MAX, min = 1, max = 100, current = 16, step = 1)
    private int maxSetting = 16;

    private int index;
    private float maxName;

    public PlayerListMod() {
        super(TranslateText.PLAYER_LIST, TranslateText.PLAYER_LIST_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG());
    }

    private void drawNanoVG() {

        int prevIndex = 0;
        int offsetY = 23;

        this.drawBackground(maxName, (index * 15) + 24.5F);
        this.drawText("Player List", 5.5F, 6F, 10.5F, getHudFont(1));
        this.drawRect(0, 18, maxName, 1);

        for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {

            if (playerInfo != null && playerInfo.getGameProfile() != null) {

                String name = playerInfo.getGameProfile().getName();

                if (this.getTextWidth(name, 9, getHudFont(2)) + 26 > maxName) {
                    maxName = this.getTextWidth(name, 9, getHudFont(2)) + 26;
                }

                this.drawPlayerHead(playerInfo.getLocationSkin(), 5.5F, offsetY, 12, 12, 2.5F);
                this.drawText(name, 20, offsetY + 2.5F, 9, getHudFont(1));

                if (prevIndex > maxSetting) {
                    prevIndex++;
                    index = prevIndex;
                    break;
                }

                prevIndex++;
                offsetY += 15;
            }
        }

        index = prevIndex;

        this.setWidth((int) maxName);
        this.setHeight((index * 15) + 26);
    }
}
