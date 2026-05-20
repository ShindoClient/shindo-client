package me.miki.shindo.management.profile.mainmenu.impl

import me.miki.shindo.management.language.TranslateText
import net.minecraft.util.ResourceLocation
import java.io.File

class ShaderBackground(
    id: Int,
    private val nameTranslate: TranslateText,
    private val shaderFile: File?,
    private val shaderResource: ResourceLocation,
) : Background(id, nameTranslate.getText()) {
    constructor(id: Int, nameTranslate: TranslateText, shaderFile: File) : this(
        id,
        nameTranslate,
        shaderFile,
        ResourceLocation("shindo/shaders/menu.fsh"),
    )

    constructor(id: Int, nameTranslate: TranslateText, shaderResource: ResourceLocation) : this(
        id,
        nameTranslate,
        null,
        shaderResource,
    )

    private var shaderId: Int = -1

    fun setShaderId(value: Int) {
        shaderId = value
    }

    override fun getName(): String = nameTranslate.getText()

    fun getNameKey(): String = nameTranslate.getKey()

    fun getShaderFile(): File? = shaderFile

    fun getShaderResource(): ResourceLocation = shaderResource

    fun getShaderId(): Int = shaderId

    fun isShaderLoaded(): Boolean = shaderId != -1

    fun hasFileShader(): Boolean = shaderFile != null && shaderFile.exists()
}
