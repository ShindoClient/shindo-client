package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.GlUtils;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;

public class PotionStatusMod extends HUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.COMPACT)
    private boolean compact = false;
    private int maxString, prevPotionCount;
    private Collection<PotionEffect> potions;

    public PotionStatusMod() {
        super(TranslateText.POTION_STATUS, TranslateText.POTION_STATUS_DESCRIPTION);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {

        if (this.isEditing() || mc.thePlayer == null) {
            potions = Arrays.asList(new PotionEffect(1, 0), new PotionEffect(10, 0));
        } else {
            potions = mc.thePlayer.getActivePotionEffects();
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG(nvg));

        if (!potions.isEmpty()) {

            int ySize = compact ? 22 : 23;
            int offsetY = 16;

            for (PotionEffect potioneffect : potions) {

                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                int index = potion.getStatusIconIndex();
                GlStateManager.enableBlend();

                GlUtils.startScale(this.getX(), this.getY(), this.getScale());

                if (compact) {
                    GlUtils.startScale((this.getX() + 21) - 20, (this.getY() + offsetY) - 11 - offsetY - 2F, 18, 18, 0.72F);
                    RenderUtils.drawTexturedModalRect((this.getX() + 21) - 20, (this.getY() + offsetY) - 11, index % 8 * 18, 198 + index / 8 * 18, 18, 18);
                    GlUtils.stopScale();
                } else {
                    RenderUtils.drawTexturedModalRect((this.getX() + 21) - 17, (this.getY() + offsetY) - 12, index % 8 * 18, 198 + index / 8 * 18, 18, 18);
                }

                GlUtils.stopScale();

                offsetY += ySize;
            }
        }
    }

    private void drawNanoVG(NanoVGManager nvg) {

        int ySize = compact ? 16 : 23;
        int offsetY = 16;

        if (potions.isEmpty()) {
            maxString = 0;
        }

        if (!potions.isEmpty()) {

            this.drawBackground(maxString + 29, (ySize * potions.size()) + 2);

            for (PotionEffect potioneffect : potions) {

                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];

                String name = I18n.format(potion.getName());

                if (potioneffect.getAmplifier() == 1) {
                    name = name + " " + I18n.format("enchantment.level.2");
                } else if (potioneffect.getAmplifier() == 2) {
                    name = name + " " + I18n.format("enchantment.level.3");
                } else if (potioneffect.getAmplifier() == 3) {
                    name = name + " " + I18n.format("enchantment.level.4");
                }

                String time = Potion.getDurationString(potioneffect);

                if (compact) {
                    this.drawText(name + " | " + time, 20, offsetY - 10.5F, 9, getHudFont(1));
                } else {
                    this.drawText(name, 25, offsetY - 12, 9, getHudFont(1));
                    this.drawText(time, 25, offsetY - 1, 8, getHudFont(1));
                }

                offsetY += ySize;

                if (compact) {

                    float totalWidth = nvg.getTextWidth(name + " | " + time, 9, getHudFont(1));

                    if (maxString < totalWidth || prevPotionCount != potions.size()) {
                        maxString = (int) totalWidth - 4;
                    }
                } else {
                    float levelWidth = nvg.getTextWidth(name, 9, getHudFont(1));
                    float timeWidth = nvg.getTextWidth(time, 9, getHudFont(1));

                    if (maxString < levelWidth || maxString < timeWidth || prevPotionCount != potions.size()) {

                        if (levelWidth > timeWidth) {
                            maxString = (int) (levelWidth);
                        } else {
                            maxString = (int) (timeWidth);
                        }

                        prevPotionCount = potions.size();
                    }
                }
            }
        }

        this.setWidth(maxString + 29);
        this.setHeight((ySize * 2) + 2);
    }
}
