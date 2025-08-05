package me.miki.shindo.injection.interfaces;

import net.minecraft.client.shader.Shader;

import java.util.List;

public interface IMixinShaderGroup {
    List<Shader> getListShaders();
}
