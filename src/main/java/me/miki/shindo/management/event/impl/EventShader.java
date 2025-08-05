package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.client.shader.ShaderGroup;

import java.util.ArrayList;
import java.util.List;

public class EventShader extends Event {

    private final List<ShaderGroup> groups = new ArrayList<ShaderGroup>();

    public List<ShaderGroup> getGroups() {
        return groups;
    }
}
