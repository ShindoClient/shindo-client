package me.miki.shindo.injection.interfaces;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.entity.Entity;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;

import java.io.File;

public interface IMixinMinecraft {
    boolean isRunning();

    Timer getTimer();

    void setSession(Session session);

    void callClickMouse();

    void callRightClickMouse();

    DefaultResourcePack getMcDefaultResourcePack();

    void resizeWindow(int width, int height);

    Entity getRenderViewEntity();

    File getFileResourcepacks();

    ResourcePackRepository getMcResourcePackRepository();

    void setMcResourcePackRepository(ResourcePackRepository repo);
}
