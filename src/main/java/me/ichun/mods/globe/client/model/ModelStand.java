package me.ichun.mods.globe.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * Stand - iChun
 * Created using Tabula 7.0.0
 */
public class ModelStand extends Model //StandActual.tbl
{
    public ModelRenderer sideB;
    public ModelRenderer sideF;
    public ModelRenderer sideL;
    public ModelRenderer sideR;
    public ModelRenderer base;
    public ModelRenderer base1; //shiny inside

    public ModelStand() {
        super(RenderType::getEntityCutout);
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

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        this.sideL.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.sideF.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.sideB.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.sideR.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
