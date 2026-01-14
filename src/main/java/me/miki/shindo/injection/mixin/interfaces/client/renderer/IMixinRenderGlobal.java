package me.miki.shindo.injection.mixin.interfaces.client.renderer;

import net.minecraft.client.multiplayer.WorldClient;

public interface IMixinRenderGlobal {
    WorldClient getWorldClient();
}

