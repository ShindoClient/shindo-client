package me.miki.shindo.injection.mixin.minecraft.block;

import me.miki.shindo.injection.interfaces.ICachedHashcode;
import net.minecraft.block.properties.PropertyInteger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PropertyInteger.class)
public class MixinPropertyInteger {
    /**
     * @author MikiDevAHM
     * @reason Cache hashcode to improve performance and avoid recomputation
     */
    @Overwrite
    public int hashCode() {
        return ((ICachedHashcode) this).getCachedHashcode();
    }
}

