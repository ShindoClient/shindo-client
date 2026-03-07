package eu.shoroa.contrib.animation;

import java.util.function.Supplier;

public class Animate {
    public static float DELTA = 0;

    private final float speed;
    private final Easing ease;
    private Supplier<Boolean> condition;
    private float value;

    public Animate(float speed, Easing ease) {
        this.speed = speed;
        this.ease = ease;
        this.condition = () -> false;
        this.value = 0f;
    }

    public Animate easeIf(Supplier<Boolean> condition) {
        this.condition = condition;
        return this;
    }

    public void update() {
        float dt = DELTA * speed;
        if (condition.get()) value += dt;
        else value -= dt;

        value = Math.max(0f, Math.min(1f, value));

        if (Float.isNaN(value) || Float.isInfinite(value)) forceFinish();
    }

    public void forceFinish() {
        value = condition.get() ? 1f : 0f;
    }

    public float getValue() {
        return ease.ease(value);
    }

    public float getLinearValue() {
        return value;
    }

    public boolean canEase() {
        return condition.get();
    }
}
