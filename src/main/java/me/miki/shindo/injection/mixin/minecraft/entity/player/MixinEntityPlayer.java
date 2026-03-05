package me.miki.shindo.injection.mixin.minecraft.entity.player;

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer;
import me.miki.shindo.management.event.impl.EventAttackEntity;
import me.miki.shindo.management.event.impl.EventJump;
import me.miki.shindo.management.mods.impl.skin3d.render.CustomizableModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer implements IMixinEntityPlayer {

    private CustomizableModelPart headLayer;
    private CustomizableModelPart[] skinLayer;

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
    public void attackEntity(Entity entity, CallbackInfo ci) {
        if (entity.canAttackWithItem()) {
            new EventAttackEntity(entity).call();
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    public void preJump(CallbackInfo ci) {
        new EventJump().call();
    }


    @Override
    public CustomizableModelPart[] getSkinLayers() {
        return skinLayer;
    }

    @Override
    public void setupSkinLayers(@NotNull CustomizableModelPart[] box) {
        this.skinLayer = box;
    }

    @Override
    public CustomizableModelPart getHeadLayers() {
        return headLayer;
    }

    @Override
    public void setupHeadLayers(CustomizableModelPart box) {
        this.headLayer = box;
    }
}
