package me.miki.shindo.injection.mixin.interfaces.client.shader;

import net.minecraft.client.shader.Shader;

import java.util.List;

public interface IMixinShaderGroup {
    List<Shader> getListShaders();
}

