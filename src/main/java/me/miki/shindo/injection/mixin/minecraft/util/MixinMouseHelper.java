package me.miki.shindo.injection.mixin.minecraft.util;

import me.miki.shindo.management.mods.impl.RawInputMod;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {

    @Shadow
    public int deltaX;

    @Shadow
    public int deltaY;

    @Inject(method = "mouseXYChange", at = @At("HEAD"), cancellable = true)
    public void onRawInput(CallbackInfo ci) {

        RawInputMod mod = RawInputMod.instance;

        if (Objects.requireNonNull(mod).isToggled() && Mouse.isGrabbed() && mod.isAvailable()) {
            ci.cancel();
            deltaX = (int) mod.getDx();
            deltaY = (int) -mod.getDy();
            RawInputMod.MouseThread thread = mod.getThread();
            if (thread != null) {
                thread.reset();
            }
        }
    }
}

