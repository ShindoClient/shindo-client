package me.miki.shindo.injection.mixin.minecraft.shader;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Framebuffer.class)
public class MixinFramebuffer {
    @Shadow
    public int depthBuffer;

    @Shadow
    public boolean useDepth;

    @ModifyArgs(method = "createFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;glRenderbufferStorage(IIII)V"))
    public void setRenderbufferStorageType(Args args) {
        args.set(1, GL30.GL_DEPTH24_STENCIL8);
    }

    @Inject(method = "createFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;glFramebufferRenderbuffer(IIII)V"))
    public void setStencilRenderbuffer(int width, int height, CallbackInfo ci) {
        OpenGlHelper.glFramebufferRenderbuffer(OpenGlHelper.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, OpenGlHelper.GL_RENDERBUFFER, this.depthBuffer);
    }

    @ModifyArg(method = "framebufferClear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V"))
    public int clearStencil(int value) {
        if (useDepth)
            value |= GL11.GL_STENCIL_BUFFER_BIT;
        return value;
    }
}