package me.miki.shindo.injection.mixin.mixins.block;

import me.miki.shindo.injection.interfaces.ICachedHashcode;
import net.minecraft.block.properties.PropertyInteger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PropertyInteger.class)
public class MixinPropertyInteger {

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int hashCode() {
        return ((ICachedHashcode) this).getCachedHashcode();
    }
}