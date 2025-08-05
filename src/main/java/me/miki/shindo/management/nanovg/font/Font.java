package me.miki.shindo.management.nanovg.font;

import net.minecraft.util.ResourceLocation;

import java.nio.ByteBuffer;

public class Font {

    private final String name;
    private final ResourceLocation resourceLocation;
    private boolean loaded;
    private ByteBuffer buffer;

    public Font(String name, ResourceLocation resourceLocation) {
        this.name = name;
        this.resourceLocation = resourceLocation;
        this.loaded = false;
        this.buffer = null;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public String getName() {
        return name;
    }
}
