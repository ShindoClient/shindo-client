package me.miki.shindo.ui.particle;

import lombok.Getter;
import me.miki.shindo.utils.TimerUtils;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Random;

public class Particle {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    private final float size;

    @Getter
    private final float ySpeed = new Random().nextInt(5);

    @Getter
    private final float xSpeed = new Random().nextInt(5);

    @Getter
    private final TimerUtils timer = new TimerUtils();

    @Getter
    private float x;

    @Getter
    private float y;

    @Getter
    private int height;

    @Getter
    private int width;

    public Particle(int x, int y) {
        this.x = x;
        this.y = y;
        this.size = genRandom() + 0.4F;
    }

    private float lint1(float f) {
        return ((float) 1.02 * (1.0f - f)) + (f);
    }

    private float lint2(float f) {
        return (float) 1.02 + f * ((float) 1.0 - (float) 1.02);
    }

    public void connect(float x, float y) {
        RenderUtils.connectPoints(getX(), getY(), x, y);
    }

    public void interpolation() {

        for (int n = 0; n <= 64; ++n) {
            final float f = n / 64.0f;
            final float p1 = lint1(f);
            final float p2 = lint2(f);

            if (p1 != p2) {
                y -= f;
                x -= f;
            }
        }
    }

    public void fall() {

        ScaledResolution sr = new ScaledResolution(mc);

        y = (y + ySpeed);
        x = (x + xSpeed);

        if (y > mc.displayHeight) {
            y = 1;
        }

        if (x > mc.displayWidth) {
            x = 1;
        }

        if (x < 1) {
            x = sr.getScaledWidth();
        }

        if (y < 1) {
            y = sr.getScaledHeight();
        }
    }

    private float genRandom() {
        return (float) (0.3f + Math.random() * (0.6f - 0.3f + 1.0F));
    }


}

