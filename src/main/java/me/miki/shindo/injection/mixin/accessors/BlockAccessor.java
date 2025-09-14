package me.miki.shindo.injection.mixin.accessors;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockAccessor {

    @Accessor
    void setMaxY(double maxY);

    @Invoker("setBlockBounds")
    void invokerSetBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);
}