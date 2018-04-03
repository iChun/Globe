package me.ichun.mods.globe.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Stand - iChun
 * Created using Tabula 7.0.0
 */
public class ModelStand extends ModelBase {
    public ModelRenderer sideB;
    public ModelRenderer sideF;
    public ModelRenderer sideL;
    public ModelRenderer sideR;
    public ModelRenderer base;
    public ModelRenderer base1;

    public ModelStand() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.sideL = new ModelRenderer(this, 42, 0);
        this.sideL.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.sideL.addBox(3.0F, 0.0F, -3.0F, 2, 2, 6, 0.0F);
        this.sideF = new ModelRenderer(this, 0, 0);
        this.sideF.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.sideF.addBox(-5.0F, 0.0F, -5.0F, 10, 2, 2, 0.0F);
        this.sideB = new ModelRenderer(this, 24, 0);
        this.sideB.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.sideB.addBox(-5.0F, 0.0F, 3.0F, 10, 2, 2, 0.0F);
        this.sideR = new ModelRenderer(this, 0, 4);
        this.sideR.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.sideR.addBox(-5.0F, 0.0F, -3.0F, 2, 2, 6, 0.0F);
        this.base1 = new ModelRenderer(this, 16, 4);
        this.base1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.base1.addBox(-3.0F, 0.5F, -3.0F, 6, 1, 6, 0.0F);
        this.base = new ModelRenderer(this, 16, 4);
        this.base.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.base.addBox(-3.0F, 1.0F, -3.0F, 6, 1, 6, 0.0F);
    }

    public void render(float f5) {
        this.sideL.render(f5);
        this.sideF.render(f5);
        this.sideB.render(f5);
        this.sideR.render(f5);
        this.base.render(f5);
    }
}
