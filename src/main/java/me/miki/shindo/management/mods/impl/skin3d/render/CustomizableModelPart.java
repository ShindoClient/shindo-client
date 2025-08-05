package me.miki.shindo.management.mods.impl.skin3d.render;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

import java.util.List;

public class CustomizableModelPart {

    private final List<CustomizableCube> cubes;
    public float x;
    public float y;
    public float z;
    public boolean visible = true;

    public CustomizableModelPart(List<CustomizableCube> list) {
        this.cubes = list;
    }

    public void copyFrom(ModelBox modelPart) {
        this.x = modelPart.posX1;
        this.y = modelPart.posY1;
        this.z = modelPart.posZ1;
    }

    public void setPos(float f, float g, float h) {
        this.x = f;
        this.y = g;
        this.z = h;
    }

    public void render(boolean redTint) {
        if (!this.visible)
            return;
        GlStateManager.pushMatrix();
        translateAndRotate();
        compile(redTint);
        GlStateManager.popMatrix();
    }

    public void translateAndRotate() {
        GlStateManager.translate((this.x / 16.0F), (this.y / 16.0F), (this.z / 16.0F));
    }

    private void compile(boolean redTint) {
        for (CustomizableCube cube : this.cubes) {
            cube.render(Tessellator.getInstance().getWorldRenderer(), redTint);
        }
    }
}