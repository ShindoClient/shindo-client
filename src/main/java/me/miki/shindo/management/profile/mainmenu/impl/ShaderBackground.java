package me.miki.shindo.management.profile.mainmenu.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.management.language.TranslateText;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class ShaderBackground extends Background {

    private final TranslateText nameTranslate;
    @Getter
    private final File shaderFile;
    @Getter
    private final ResourceLocation shaderResource;
    @Setter
    @Getter
    private int shaderId = -1;

    public ShaderBackground(int id, TranslateText nameTranslate, File shaderFile) {
        super(id, nameTranslate.getText());
        this.nameTranslate = nameTranslate;
        this.shaderFile = shaderFile;
        this.shaderResource = new ResourceLocation("shindo/shaders/menu.fsh");
    }

    public ShaderBackground(int id, TranslateText nameTranslate, ResourceLocation shaderResource) {
        super(id, nameTranslate.getText());
        this.nameTranslate = nameTranslate;
        this.shaderResource = shaderResource;
        this.shaderFile = null;
    }

    @Override
    public String getName() {
        return nameTranslate.getText();
    }

    public String getNameKey() {
        return nameTranslate.getKey();
    }

    public boolean isShaderLoaded() {
        return shaderId != -1;
    }

    public boolean hasResourceShader() {
        return shaderResource != null;
    }

    public boolean hasFileShader() {
        return shaderFile != null && shaderFile.exists();
    }
}