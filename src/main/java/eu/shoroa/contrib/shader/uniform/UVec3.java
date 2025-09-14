/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.shader.uniform;

public class UVec3 extends Uniform {
    private final float x;
    private final float y;
    private final float z;

    public UVec3(String name, float x, float y, float z) {
        super(name);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }
}
