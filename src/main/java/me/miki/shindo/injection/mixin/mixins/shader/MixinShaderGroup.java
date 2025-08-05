package me.miki.shindo.injection.mixin.mixins.shader;

import com.google.common.collect.Lists;
import me.miki.shindo.injection.interfaces.IMixinShaderGroup;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ShaderGroup.class)
public class MixinShaderGroup implements IMixinShaderGroup {

    @Shadow
    @Final
    private final List<Shader> listShaders = Lists.newArrayList();

    @Override
    public List<Shader> getListShaders() {
        return listShaders;
    }
}
