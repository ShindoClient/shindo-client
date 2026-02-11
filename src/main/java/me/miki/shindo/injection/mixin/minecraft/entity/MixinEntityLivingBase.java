package me.miki.shindo.injection.mixin.minecraft.entity;

import me.miki.shindo.injection.mixin.interfaces.entity.IMixinEntityLivingBase;
import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer;
import me.miki.shindo.management.event.impl.EventLivingUpdate;
import me.miki.shindo.management.mods.impl.SlowSwingMod;
import me.miki.shindo.management.settings.impl.NumberSetting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Objects;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity implements IMixinEntityLivingBase {

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Shadow
    protected abstract int getArmSwingAnimationEnd();

    @Inject(method = "onEntityUpdate", at = @At("TAIL"))
    public void onEntityUpdate(CallbackInfo ci) {
        new EventLivingUpdate((EntityLivingBase) (Object) this).call();
    }

    @Inject(method = "getArmSwingAnimationEnd", at = @At("HEAD"), cancellable = true)
    public void changeSwingSpeed(CallbackInfoReturnable<Integer> cir) {

        SlowSwingMod mod = SlowSwingMod.instance;
        NumberSetting delaySetting = Objects.requireNonNull(mod).getDelaySetting();

        if (mod.isToggled() && delaySetting != null) {
            cir.setReturnValue(delaySetting.getValueInt());
        }
    }

    @Inject(method = "getLook", at = @At("HEAD"), cancellable = true)
    private void mouseDelayFix(float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        if ((EntityLivingBase) (Object) this instanceof EntityPlayerSP) {
            cir.setReturnValue(super.getLook(partialTicks));
        }
    }

    @Inject(method = "setRotationYawHead", at = @At("HEAD"))
    private void onSetRotationYawHead(float yawHead, CallbackInfo ci) {
        if ((Object) this instanceof EntityOtherPlayerMP && this instanceof IMixinEntityPlayer) {
            ((IMixinEntityPlayer) this).getPlayerDataSamples().setRotationYawHead(yawHead);
        }
    }

    @Override
    public int getArmSwingAnimation() {
        return getArmSwingAnimationEnd();
    }

    @Inject(method = "updatePotionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionEffect;onUpdate(Lnet/minecraft/entity/EntityLivingBase;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void checkPotionEffect(CallbackInfo ci, Iterator<Integer> iterator, Integer integer, PotionEffect potioneffect) {
        if (potioneffect == null) {
            ci.cancel();
        }
    }
}


