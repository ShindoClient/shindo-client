package me.miki.shindo.injection.mixin.mixins.block;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockPos.class)
public class MixinBlockPos extends Vec3i {

    public MixinBlockPos(double xIn, double yIn, double zIn) {
        super(xIn, yIn, zIn);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos up() {
        return new BlockPos(this.getX(), this.getY() + 1, this.getZ());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos up(int offset) {
        return offset == 0 ? (BlockPos) (Object) this : new BlockPos(this.getX(), this.getY() + offset, this.getZ());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos down() {
        return new BlockPos(this.getX(), this.getY() - 1, this.getZ());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos down(int offset) {
        return offset == 0 ? (BlockPos) (Object) this : new BlockPos(this.getX(), this.getY() - offset, this.getZ());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos north() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() - 1);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos north(int offset) {
        return offset == 0 ? (BlockPos) (Object) this : new BlockPos(this.getX(), this.getY(), this.getZ() - offset);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos south() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() + 1);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos south(int offset) {
        return offset == 0 ? (BlockPos) (Object) this : new BlockPos(this.getX(), this.getY(), this.getZ() + offset);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos west() {
        return new BlockPos(this.getX() - 1, this.getY(), this.getZ());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos west(int offset) {
        return offset == 0 ? (BlockPos) (Object) this : new BlockPos(this.getX() - offset, this.getY(), this.getZ());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos east() {
        return new BlockPos(this.getX() + 1, this.getY(), this.getZ());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos east(int offset) {
        return offset == 0 ? (BlockPos) (Object) this : new BlockPos(this.getX() + offset, this.getY(), this.getZ());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public BlockPos offset(EnumFacing direction) {
        return new BlockPos(this.getX() + direction.getFrontOffsetX(), this.getY() + direction.getFrontOffsetY(), this.getZ() + direction.getFrontOffsetZ());
    }
}
