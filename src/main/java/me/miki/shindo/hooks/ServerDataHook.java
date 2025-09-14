package me.miki.shindo.hooks;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;

public class ServerDataHook extends ServerData {

    public static final ResourceLocation STAR_ICON = new ResourceLocation("shindo/star.png");

    public ServerDataHook(String serverName, String serverIP) {
        super(serverName, serverIP, false);
    }
}
