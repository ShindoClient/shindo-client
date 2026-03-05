package me.miki.shindo.hooks;

import me.miki.shindo.injection.mixin.interfaces.client.gui.IMixinChatLine;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;

public class GuiNewChatHook {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int drawStringWithHead(ChatLine line, String text, float x, float y, int color) {

        float actualX = x;

        IMixinChatLine hook = (IMixinChatLine) line;

        Object playerInfo = hook.client$getPlayerInfo();
        NetworkPlayerInfo networkPlayerInfo = playerInfo instanceof NetworkPlayerInfo ? (NetworkPlayerInfo) playerInfo : null;

        actualX += networkPlayerInfo != null ? 10f : 0;

        if (networkPlayerInfo != null) {

            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();

            mc.getTextureManager().bindTexture(networkPlayerInfo.getLocationSkin());

            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1.0f, 1.0f, 1.0f, ColorUtils.getAlphaByInt(color));

            RenderUtils.drawScaledCustomSizeModalRect(x, y - 0.5, 8.0f, 8.0f, 8, 8, 8, 8, 64.0f, 64.0f);
            RenderUtils.drawScaledCustomSizeModalRect(x, y - 0.5, 40.0f, 8.0f, 8, 8, 8, 8, 64.0f, 64.0f);

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }

        return mc.fontRendererObj.drawStringWithShadow(text, actualX, y, color);
    }
}
