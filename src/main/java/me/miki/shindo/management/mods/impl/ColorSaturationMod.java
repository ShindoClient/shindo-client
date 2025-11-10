package me.miki.shindo.management.mods.impl;

import com.google.gson.JsonSyntaxException;
import me.miki.shindo.injection.interfaces.IMixinShaderGroup;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventShader;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class ColorSaturationMod extends Mod {

    private final ResourceLocation colorsaturation = new ResourceLocation("minecraft:shaders/post/colorsaturation.json");
    @Property(type = PropertyType.NUMBER, translate = TranslateText.HUE, category = "Color Grading", min = 0, max = 1, current = 0)
    private double hueSetting;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.BRIGHTNESS, category = "Color Grading", min = 0, max = 1, current = 0.5)
    private double brightnessSetting = 0.5;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.CONTRAST, category = "Color Grading", min = 0, max = 1, current = 0.5)
    private double contrastSetting = 0.5;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.SATURATION, category = "Color Grading", min = 0, max = 1, current = 0.5)
    private double saturationSetting = 0.5;
    private ShaderGroup group;
    private float prevHue;
    private float prevSaturation;
    private float prevBrightness;
    private float prevContrast;
    private int prevWidth, prevHeight;

    public ColorSaturationMod() {
        super(TranslateText.COLOR_SATURATION, TranslateText.COLOR_SATURATION_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onShader(EventShader event) {

        ScaledResolution sr = new ScaledResolution(mc);

        float hue = (float) hueSetting;
        float saturation = (float) saturationSetting;
        float brightness = (float) brightnessSetting;
        float contrast = (float) contrastSetting;

        if (group == null || prevWidth != sr.getScaledWidth() || prevHeight != sr.getScaledHeight()) {

            prevWidth = sr.getScaledWidth();
            prevHeight = sr.getScaledHeight();

            prevHue = hue;
            prevSaturation = saturation;
            prevBrightness = brightness;
            prevContrast = contrast;

            try {
                group = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), colorsaturation);
                group.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            } catch (JsonSyntaxException | IOException error) {
                error.printStackTrace();
            }
        }

        if (prevHue != hue || prevSaturation != saturation || prevBrightness != brightness || prevContrast != contrast) {
            ((IMixinShaderGroup) group).getListShaders().forEach((shader) -> {

                ShaderUniform hueUniform = shader.getShaderManager().getShaderUniform("hue");
                ShaderUniform contrastUniform = shader.getShaderManager().getShaderUniform("Contrast");
                ShaderUniform brightnessUniform = shader.getShaderManager().getShaderUniform("Brightness");
                ShaderUniform saturationUniform = shader.getShaderManager().getShaderUniform("Saturation");

                if (hueUniform != null) {
                    hueUniform.set(hue);
                }

                if (contrastUniform != null) {
                    contrastUniform.set(contrast);
                }

                if (brightnessUniform != null) {
                    brightnessUniform.set(brightness);
                }

                if (saturationUniform != null) {
                    saturationUniform.set(saturation);
                }
            });

            prevHue = hue;
            prevSaturation = saturation;
            prevBrightness = brightness;
            prevContrast = contrast;
        }

        event.getGroups().add(group);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        group = null;
    }
}
