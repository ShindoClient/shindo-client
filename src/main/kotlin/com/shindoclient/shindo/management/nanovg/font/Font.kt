package com.shindoclient.shindo.management.nanovg.font

import net.minecraft.util.ResourceLocation
import java.nio.ByteBuffer

class Font(
    val name: String,
    val resourceLocation: ResourceLocation,
    var isLoaded: Boolean = false,
    var buffer: ByteBuffer? = null,
)
