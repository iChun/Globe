package me.ichun.mods.globe.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Stand - iChun
 * Created using Tabula 7.0.0
 */
public class ModelGlobeStand extends ModelBase {
    public ModelRenderer base;

    public ModelGlobeStand() {
        this.textureWidth = 128;
        this.textureHeight = 32;
        this.base = new ModelRenderer(this, 0, 0);
        this.base.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.base.addBox(-10.0F, 0.0F, -10.0F, 20, 5, 20, 0.0F);
    }

    public void render(float f5) {
        this.base.render(f5);
    }
}
