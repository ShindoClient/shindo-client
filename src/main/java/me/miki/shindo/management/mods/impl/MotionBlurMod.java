package me.miki.shindo.management.mods.impl;

import me.miki.shindo.injection.interfaces.IMixinShaderGroup;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventShader;
import me.miki.shindo.management.event.impl.EventUpdateDisplay;
import me.miki.shindo.management.annotation.Range;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.utils.Sound;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
public class MotionBlurMod extends Mod {

    private final ResourceLocation motion_blur = new ResourceLocation("minecraft:shaders/post/motion_blur.json");

    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private Mode mode = Mode.SHADER;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.AMOUNT, min = 0.1, max = 0.9, current = 0.5)
    private double amountSetting = 0.5;

    private long lastCheck = 0L;
    private ShaderGroup group;
    private float groupBlur;
    private boolean loaded;
    private int prevWidth, prevHeight;

    public MotionBlurMod() {
        super(TranslateText.MOTION_BLUR, TranslateText.MOTION_BLUR_DESCRIPTION, ModCategory.RENDER);
    }

    @Override
    public void setup() {
        loaded = false;
    }

    @EventTarget
    public void onShader(EventShader event) {

        ScaledResolution sr = new ScaledResolution(mc);

        if (mode == Mode.SHADER) {

            if (group == null || prevWidth != sr.getScaledWidth() || prevHeight != sr.getScaledHeight()) {

                prevWidth = sr.getScaledWidth();
                prevHeight = sr.getScaledHeight();

                groupBlur = (float) amountSetting;

                try {
                    group = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), motion_blur);
                    group.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (groupBlur != (float) amountSetting || !loaded) {
                loaded = true;
                ((IMixinShaderGroup) group).getListShaders().forEach((shader) -> {
                    ShaderUniform factor = shader.getShaderManager().getShaderUniform("BlurFactor");
                    if (factor != null) {
                        factor.set((float) amountSetting);
                    }
                });
                groupBlur = (float) amountSetting;
            }

            event.getGroups().add(group);
        }
    }

    @EventTarget
    public void onUpdateDisplay(EventUpdateDisplay event) {

        if (mode == Mode.ACCUM) {

            if (group != null) {
                group = null;
                loaded = false;
            }

            if (mc.thePlayer != null) {

                GL11.glAccum(259, (float) amountSetting);
                GL11.glAccum(256, 1.0f - (float) amountSetting);
                GL11.glAccum(258, 1.0f);

                if (lastCheck + 1000L < System.currentTimeMillis()) {
                    lastCheck = System.currentTimeMillis();

                    int error = GL11.glGetError();

                    if (error == 1282) {
                        this.setToggled(false);
                        try {
                            Sound.play("shindo/audio/error.wav", false);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        group = null;
        super.onEnable();
    }

    private enum Mode implements PropertyEnum {
        ACCUM(TranslateText.ACCUM),
        SHADER(TranslateText.SHADER);

        private final TranslateText translate;

        Mode(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
