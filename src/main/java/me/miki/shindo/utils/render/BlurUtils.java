package me.miki.shindo.utils.render;

import me.miki.shindo.injection.interfaces.IMixinMinecraft;
import me.miki.shindo.injection.interfaces.IMixinShaderGroup;
import me.miki.shindo.logger.ShindoLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;

public class BlurUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static ShaderGroup blurShader;

    private static float lastScale = 0;
    private static float lastScaleWidth = 0;
    private static float lastScaleHeight = 0;

    private static void reinitShader() {
        try {
            Framebuffer buffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
            buffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            blurShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new ResourceLocation("shaders/post/blurArea.json"));
            blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
        } catch (Exception e) {
            ShindoLogger.error("Failed to load blur shader", e);
        }
    }

    public static void drawBlurScreen(float radius) {

        ScaledResolution sr = new ScaledResolution(mc);

        int factor = sr.getScaleFactor();
        int factor2 = sr.getScaledWidth();
        int factor3 = sr.getScaledHeight();
        int x = 0;
        int y = 0;
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();

        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            reinitShader();
        }

        lastScale = factor;
        lastScaleWidth = factor2;
        lastScaleHeight = factor3;

        ((IMixinShaderGroup) blurShader).getListShaders().get(0).getShaderManager().getShaderUniform("BlurXY").set(x * (sr.getScaleFactor() / 2.0F), (factor3 - height) * (sr.getScaleFactor() / 2.0F));
        ((IMixinShaderGroup) blurShader).getListShaders().get(1).getShaderManager().getShaderUniform("BlurXY").set(x * (sr.getScaleFactor() / 2.0F), (factor3 - height) * (sr.getScaleFactor() / 2.0F));
        ((IMixinShaderGroup) blurShader).getListShaders().get(0).getShaderManager().getShaderUniform("BlurCoord").set((width - x) * (sr.getScaleFactor() / 2.0F), (height - y) * (sr.getScaleFactor() / 2.0F));
        ((IMixinShaderGroup) blurShader).getListShaders().get(1).getShaderManager().getShaderUniform("BlurCoord").set((width - x) * (sr.getScaleFactor() / 2.0F), (height - y) * (sr.getScaleFactor() / 2.0F));
        ((IMixinShaderGroup) blurShader).getListShaders().get(0).getShaderManager().getShaderUniform("Radius").set(radius);
        ((IMixinShaderGroup) blurShader).getListShaders().get(1).getShaderManager().getShaderUniform("Radius").set(radius);
        blurShader.loadShaderGroup(((IMixinMinecraft) mc).getTimer().renderPartialTicks);
        mc.getFramebuffer().bindFramebuffer(true);
    }
}
