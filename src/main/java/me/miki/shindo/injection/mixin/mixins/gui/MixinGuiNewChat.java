package me.miki.shindo.injection.mixin.mixins.gui;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.miki.shindo.hooks.GuiNewChatHook;
import me.miki.shindo.management.mods.impl.ChatMod;
import me.miki.shindo.management.mods.impl.ChatTranslateMod;
import me.miki.shindo.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat extends Gui {

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private boolean isScrolled;
    @Unique
    private float client$percentComplete;
    @Unique
    private int client$newLines;
    @Unique
    private long client$prevMillis = System.currentTimeMillis();
    @Unique
    private float client$animationPercent;
    @Unique
    private int client$lineBeingDrawn;
    @Unique
    private String client$lastMessage = "";
    @Unique
    private int client$sameMessageAmount, client$line;
    @Unique
    private ChatLine client$drawingChatLine = null;

    @Shadow
    public abstract int getLineCount();

    @Shadow
    public abstract float getChatScale();

    @Shadow
    public abstract void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId);

    @Unique
    private void client$updatePercentage(long diff) {
        if (client$percentComplete < 1) {
            client$percentComplete += (ChatMod.getInstance().getSmoothSpeedSetting().getValueFloat() / 1000) * (float) diff;
        }
        client$percentComplete = MathUtils.clamp(client$percentComplete, 0, 1);
    }

    @Inject(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ChatLine;getChatComponent()Lnet/minecraft/util/IChatComponent;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getChatLine(int updateCounter, CallbackInfo ci, int i, boolean bl, int j, int k, float f, float g, int l, int m, ChatLine chatLine, int n, double d, int o, int p, int q) {
        client$drawingChatLine = chatLine;
    }

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int redirectText(FontRenderer instance, String text, float x, float y, int color) {

        ChatMod mod = ChatMod.getInstance();
        boolean toggle = mod.isToggled() && mod.getSmoothSetting().isToggled();
        int lastOpacity = 0;

        if (toggle && client$lineBeingDrawn <= client$newLines) {
            int opacity = (color >> 24) & 0xFF;
            opacity *= (int) client$animationPercent;
            lastOpacity = (color & ~(0xFF << 24)) | (opacity << 24);
        } else {
            lastOpacity = color;
        }

        if (mod.isToggled() && mod.getHeadSetting().isToggled()) {
            return GuiNewChatHook.drawStringWithHead(client$drawingChatLine, text, x, y, lastOpacity);
        }

        return instance.drawStringWithShadow(text, x, y, lastOpacity);
    }

    /**
     * @author EldoDebug
     * @reason ChatMod Modifications
     */
    @Overwrite
    public void printChatMessage(IChatComponent component) {

        ChatMod mod = ChatMod.getInstance();

        if (mod.isToggled() && mod.getCompactSetting().isToggled()) {

            if (component.getUnformattedText().equals(client$lastMessage)) {
                mc.ingameGUI.getChatGUI().deleteChatLine(client$line);
                client$sameMessageAmount++;
                client$lastMessage = component.getUnformattedText();
                component.appendText(ChatFormatting.WHITE + " [x" + client$sameMessageAmount + "]");
            } else {
                client$sameMessageAmount = 1;
                client$lastMessage = component.getUnformattedText();
            }

            client$line++;

            if (client$line > 256) {
                client$line = 0;
            }

            printChatMessageWithOptionalDeletion(component, client$line);

            return;
        }

        printChatMessageWithOptionalDeletion(component, 0);
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int getSize(List<?> instance) {

        ChatMod mod = ChatMod.getInstance();

        if (mod.isToggled() && mod.getInfinitySetting().isToggled()) {
            return 0;
        }

        return instance.size();
    }

    @Inject(method = "drawChat", at = @At("HEAD"), cancellable = true)
    private void modifyChatRendering(CallbackInfo ci) {
        long current = System.currentTimeMillis();
        long diff = current - client$prevMillis;
        client$prevMillis = current;
        client$updatePercentage(diff);
        float t = client$percentComplete;
        client$animationPercent = MathUtils.clamp(1 - (--t) * t * t * t, 0, 1);
    }

    @Inject(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", ordinal = 0, shift = At.Shift.AFTER))
    private void translate(CallbackInfo ci) {

        ChatMod mod = ChatMod.getInstance();
        float y = 0;

        if (mod.isToggled() && mod.getSmoothSetting().isToggled() && !this.isScrolled) {
            y += (9 - 9 * client$animationPercent) * this.getChatScale();
        }

        if (ChatTranslateMod.getInstance().isToggled() && mc.currentScreen instanceof GuiChat) {
            y = y - 8;
        }

        GlStateManager.translate(0, y, 0);
    }

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V", ordinal = 0))
    private void transparentBackground(int left, int top, int right, int bottom, int color) {

        ChatMod mod = ChatMod.getInstance();

        if (!mod.isToggled() || (mod.isToggled() && mod.getBackgroundSetting().isToggled())) {
            drawRect(left, top, right, bottom, color);
        }
    }

    @ModifyArg(method = "drawChat", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0, remap = false), index = 0)
    private int getLineBeingDrawn(int line) {
        client$lineBeingDrawn = line;
        return line;
    }

    @Inject(method = "printChatMessageWithOptionalDeletion", at = @At("HEAD"))
    private void printChatMessageWithOptionalDeletion(CallbackInfo ci) {
        client$percentComplete = 0;
    }

    @ModifyVariable(method = "setChatLine", at = @At("STORE"), ordinal = 0)
    private List<IChatComponent> setNewLines(List<IChatComponent> original) {
        client$newLines = original.size() - 1;
        return original;
    }

    @ModifyVariable(method = "getChatComponent", at = @At(value = "STORE", ordinal = 0), ordinal = 4)
    private int modifyY(int original) {

        if (ChatTranslateMod.getInstance().isToggled() && mc.currentScreen instanceof GuiChat) {
            return original - 8;
        }

        return original;
    }

    @Inject(method = "getChatComponent", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/GuiNewChat;scrollPos:I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getChatComponent(int mouseX, int mouseY, CallbackInfoReturnable<IChatComponent> cir, ScaledResolution scaledresolution, int i, float f, int j, int k, int l) {
        int line = k / mc.fontRendererObj.FONT_HEIGHT;
        if (line >= getLineCount()) {
            cir.setReturnValue(null);
        }
    }

    @Redirect(method = "deleteChatLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ChatLine;getChatLineID()I"))
    private int adeleteChatLine(ChatLine instance) {
        if (instance == null) {
            return -1;
        }
        return instance.getChatLineID();
    }
}
