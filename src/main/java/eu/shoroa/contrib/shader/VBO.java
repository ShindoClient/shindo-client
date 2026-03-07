/*
 * Nanovg Blur
 * © Shoroa 2025, All Rights Reserved
 */

package eu.shoroa.contrib.shader;

import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;

public class VBO {
    private int vid = GL15.glGenBuffers();

    public void bind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vid);
    }

    public void unbind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        GL15.glDeleteBuffers(vid);
    }

    public void data(FloatBuffer buffer) {
        bind();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        unbind();
    }
}
