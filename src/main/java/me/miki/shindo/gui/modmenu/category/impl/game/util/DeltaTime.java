package me.miki.shindo.gui.modmenu.category.impl.game.util;

public class DeltaTime {
    private static final DeltaTime dt = new DeltaTime();
    private long lastTime;
    private float deltaTime;

    public DeltaTime() {
        lastTime = System.nanoTime();
        deltaTime = 0;
    }

    public static DeltaTime getInstance() {
        return dt;
    }

    public void update() {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

}