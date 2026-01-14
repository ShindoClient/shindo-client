package me.miki.shindo.injection.mixin.minecraft.world;

import me.miki.shindo.management.mods.impl.WeatherChangerMod;
import net.minecraft.world.biome.WorldChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunkManager.class)
public class MixinWorldChunkManager {

    @Inject(method = "getTemperatureAtHeight", at = @At("HEAD"), cancellable = true)
    public void preGetTemperatureAtHeight(float p_76939_1_, int p_76939_2_, CallbackInfoReturnable<Float> cir) {

        WeatherChangerMod mod = WeatherChangerMod.instance;
        WeatherChangerMod.Weather weather = mod.weather;

        if (mod.isToggled() && weather == WeatherChangerMod.Weather.SNOW) {
            cir.setReturnValue(0F);
        }
    }
}

