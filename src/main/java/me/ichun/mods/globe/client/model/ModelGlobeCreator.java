package me.ichun.mods.globe.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Creator - iChun
 * Created using Tabula 7.0.0
 */
public class ModelGlobeCreator extends ModelBase {
    public ModelRenderer pilon1;
    public ModelRenderer pilon2;
    public ModelRenderer pilon3;
    public ModelRenderer pilon4;
    public ModelRenderer barB1;
    public ModelRenderer barB2;
    public ModelRenderer barT1;
    public ModelRenderer barT2;
    public ModelRenderer barB3;
    public ModelRenderer barB4;
    public ModelRenderer barT3;
    public ModelRenderer barT4;

    public ModelGlobeCreator() {
        this.textureWidth = 64;
        this.textureHeight = 64;
        this.pilon1 = new ModelRenderer(this, 24, 0);
        this.pilon1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.pilon1.addBox(4.0F, -8.0F, -8.0F, 4, 16, 4, 0.0F);

        this.pilon2 = new ModelRenderer(this, 40, 0);
        this.pilon2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.pilon2.addBox(-8.0F, -8.0F, -8.0F, 4, 16, 4, 0.0F);

        this.pilon3 = new ModelRenderer(this, 24, 20);
        this.pilon3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.pilon3.addBox(-8.0F, -8.0F, 4.0F, 4, 16, 4, 0.0F);

        this.pilon4 = new ModelRenderer(this, 40, 20);
        this.pilon4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.pilon4.addBox(4.0F, -8.0F, 4.0F, 4, 16, 4, 0.0F);

        this.barB1 = new ModelRenderer(this, 0, 0);
        this.barB1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barB1.addBox(-4.0F, 4.0F, -8.0F, 8, 4, 4, 0.0F);

        this.barB2 = new ModelRenderer(this, 0, 0);
        this.barB2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barB2.addBox(-4.0F, 4.0F, 4.0F, 8, 4, 4, 0.0F);

        this.barT1 = new ModelRenderer(this, 0, 0);
        this.barT1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barT1.addBox(-4.0F, -8.0F, -8.0F, 8, 4, 4, 0.0F);

        this.barT2 = new ModelRenderer(this, 0, 0);
        this.barT2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barT2.addBox(-4.0F, -8.0F, 4.0F, 8, 4, 4, 0.0F);

        this.barB3 = new ModelRenderer(this, 0, 8);
        this.barB3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barB3.addBox(-8.0F, 4.0F, -4.0F, 4, 4, 8, 0.0F);

        this.barB4 = new ModelRenderer(this, 0, 8);
        this.barB4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barB4.addBox(4.0F, 4.0F, -4.0F, 4, 4, 8, 0.0F);

        this.barT3 = new ModelRenderer(this, 0, 8);
        this.barT3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barT3.addBox(-8.0F, -8.0F, -4.0F, 4, 4, 8, 0.0F);

        this.barT4 = new ModelRenderer(this, 0, 8);
        this.barT4.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.barT4.addBox(4.0F, -8.0F, -4.0F, 4, 4, 8, 0.0F);
    }

    public void render(float f5) {
        this.pilon1.render(f5);
        this.pilon2.render(f5);
        this.pilon3.render(f5);
        this.pilon4.render(f5);
        this.barB1.render(f5);
        this.barB2.render(f5);
        this.barT1.render(f5);
        this.barT2.render(f5);
        this.barB3.render(f5);
        this.barB4.render(f5);
        this.barT3.render(f5);
        this.barT4.render(f5);
    }
}
