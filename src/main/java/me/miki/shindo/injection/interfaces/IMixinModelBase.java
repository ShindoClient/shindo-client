package me.miki.shindo.injection.interfaces;

public interface IMixinModelBase {

    /**
     * Sets the texture offset for a specific part of the model.
     *
     * @param name The name of the part.
     * @param x    The x-coordinate of the texture offset.
     * @param y    The y-coordinate of the texture offset.
     */
    void setTextureOffset(String name, int x, int y);

}
