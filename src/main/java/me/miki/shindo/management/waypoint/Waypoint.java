package me.miki.shindo.management.waypoint;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;

import java.awt.*;

public class Waypoint {

    private final SimpleAnimation trashAnimation = new SimpleAnimation();
    private final String world;
    private final String name;
    private final double x;
    private final double y;
    private final double z;
    private final Color color;

    public Waypoint(String world, String name, double x, double y, double z, Color color) {
        this.world = world;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    public String getWorld() {
        return world;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Color getColor() {
        return color;
    }

    public SimpleAnimation getTrashAnimation() {
        return trashAnimation;
    }
}
