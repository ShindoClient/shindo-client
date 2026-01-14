package me.miki.shindo.injection.mixin.minecraft.world;

import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.impl.TimeChangerMod;
import me.miki.shindo.management.mods.impl.WeatherChangerMod;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldInfo.class)
public class MixinWorldInfo {

    @Shadow
    private long worldTime;

    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    public void preIsRaining(CallbackInfoReturnable<Boolean> cir) {

        WeatherChangerMod mod = WeatherChangerMod.instance;

        if (mod.isToggled()) {

            WeatherChangerMod.Weather weather = mod.weather;
            cir.setReturnValue(weather == WeatherChangerMod.Weather.CLEAR);
        }
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    public void preIsThundering(CallbackInfoReturnable<Boolean> cir) {

        WeatherChangerMod mod = WeatherChangerMod.instance;

        if (mod.isToggled()) {
            WeatherChangerMod.Weather weather = mod.weather;
            cir.setReturnValue(weather == WeatherChangerMod.Weather.STORM);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public long getWorldTime() {

        TimeChangerMod mod = TimeChangerMod.instance;

        if (mod.isToggled()) {
            return (long) (mod.getTimeSetting().getValueFloat() * 1_000L) + 18_000L;
        }

        return this.worldTime;
    }
}

