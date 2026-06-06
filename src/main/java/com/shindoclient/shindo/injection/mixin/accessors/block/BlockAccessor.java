package com.shindoclient.shindo.injection.mixin.accessors.block;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockAccessor {
    @Accessor("maxY")
    void setMaxY(double maxY);

    @Invoker("setBlockBounds")
    void invokeSetBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);
}
