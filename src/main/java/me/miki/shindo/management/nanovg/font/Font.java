package me.miki.shindo.management.nanovg.font;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

import java.nio.ByteBuffer;

@Getter
public class Font {

    private final String name;
    private final ResourceLocation resourceLocation;

    @Setter
    private boolean loaded;

    @Setter
    private ByteBuffer buffer;

    public Font(String name, ResourceLocation resourceLocation) {
        this.name = name;
        this.resourceLocation = resourceLocation;
        this.loaded = false;
        this.buffer = null;
    }

}
