package me.miki.shindo.injection.mixin.mixins.gui;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.impl.EventText;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Shadow
    protected abstract void resetStyles();

    @ModifyVariable(method = "renderString", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String renderString(String text) {

        if (text == null || Shindo.getInstance().getEventManager() == null) {
            return text;
        }

        EventText event = new EventText(text);
        event.call();

        return event.getOutputText();
    }

    @ModifyVariable(method = "getStringWidth", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String getStringWidth(String text) {

        if (text == null || Shindo.getInstance().getEventManager() == null) {
            return text;
        }

        EventText event = new EventText(text);
        event.call();

        return event.getOutputText();
    }

    @Inject(method = "drawString(Ljava/lang/String;FFIZ)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;renderString(Ljava/lang/String;FFIZ)I",
                    ordinal = 0, shift = At.Shift.AFTER))
    private void resetStyle(CallbackInfoReturnable<Integer> ci) {
        this.resetStyles();
    }
}
