package me.miki.shindo.injection.mixin.mixins.world;

import me.miki.shindo.injection.interfaces.IMixinWorld;
import me.miki.shindo.management.mods.impl.WeatherChangerMod;
import me.miki.shindo.management.mods.impl.WeatherChangerMod.Weather;
import me.miki.shindo.utils.EnumFacings;
import me.miki.shindo.viaversion.fixes.FixedSoundEngine;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorld {

    @Shadow
    @Final
    public boolean isRemote;

    @Unique
    private int client$updateRange;

    @Shadow
    public abstract boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty);

    @Shadow
    protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    public void preGetRainStrength(float delta, CallbackInfoReturnable<Float> cir) {

        WeatherChangerMod mod = WeatherChangerMod.getInstance();
        Weather weather = mod.getWeather();

        if (mod.isToggled() && weather == Weather.CLEAR) {
            cir.setReturnValue(0f);
        } else if (mod.isToggled()) {
            cir.setReturnValue(mod.getRainStrength().getValueFloat());
        }
    }

    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    public void preGgetThunderStrength(float delta, CallbackInfoReturnable<Float> cir) {

        WeatherChangerMod mod = WeatherChangerMod.getInstance();
        Weather weather = mod.getWeather();

        if (mod.isToggled() && weather != Weather.STORM) {
            cir.setReturnValue(0f);
        } else if (mod.isToggled()) {
            cir.setReturnValue(mod.getThunderStrength().getValueFloat());
        }
    }

    @Override
    public boolean client$isLoaded(int x, int z, boolean allowEmpty) {
        return isChunkLoaded(x, z, allowEmpty);
    }

    @ModifyVariable(method = "updateEntityWithOptionalForce", at = @At("STORE"), ordinal = 0)
    private boolean checkIfWorldIsRemoteBeforeForceUpdating(boolean isForced) {
        return isForced && !this.isRemote;
    }

    @Inject(method = "getCollidingBoundingBoxes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void filterEntities(Entity entityIn, AxisAlignedBB bb, CallbackInfoReturnable<List<AxisAlignedBB>> cir, List<AxisAlignedBB> list) {
        if (entityIn instanceof EntityTNTPrimed || entityIn instanceof EntityFallingBlock || entityIn instanceof EntityItem || entityIn instanceof EntityFX) {
            cir.setReturnValue(list);
        }
    }

    @Inject(method = "getHorizon", at = @At("HEAD"), cancellable = true)
    private void injectGetHorizon(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.0D); // ou qualquer valor que você deseje retornar
    }

    @ModifyArg(method = "checkLightFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(Lnet/minecraft/util/BlockPos;IZ)Z", ordinal = 0), index = 1)
    public int reduceAreaLoadedCheckRange(int radius) {
        return 16;
    }

    @Inject(method = "checkLightFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 0))
    public void calculateUpdateRange(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.client$updateRange = this.isAreaLoaded(pos, 18, false) ? 17 : 15;
    }

    @ModifyConstant(method = "checkLightFor", constant = @Constant(intValue = 17), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 0)), allow = 2)
    public int replaceRangeConstants(int constant) {
        return this.client$updateRange;
    }

    @Redirect(method = "getRawLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    public EnumFacing[] getRawLight$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "checkLightFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    public EnumFacing[] checkLightFor$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Redirect(method = "isBlockIndirectlyGettingPowered", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    public EnumFacing[] isBlockIndirectlyGettingPowered$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return FixedSoundEngine.destroyBlock((World) (Object) this, pos, dropBlock);
    }
}
