package me.ichun.mods.globe.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.globe.client.model.ModelGlobeCreator;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class TileRendererGlobeCreator extends TileEntityRenderer<TileEntityGlobeCreator>
{
    public static final ResourceLocation TEX_GLOBE_CREATOR = new ResourceLocation("globe", "textures/model/creator.png");
    public static final RenderType VOXEL_RENDER = RenderType.makeType("globe_voxel_render", DefaultVertexFormats.ENTITY, 7, 256, true, false, RenderType.State.getBuilder()
            .texture(RenderState.NO_TEXTURE)
            .transparency(RenderState.TRANSLUCENT_TRANSPARENCY)
            .diffuseLighting(RenderState.DIFFUSE_LIGHTING_ENABLED)
            .alpha(RenderState.DEFAULT_ALPHA)
            .fog(RenderState.NO_FOG)
            .lightmap(RenderState.LIGHTMAP_ENABLED)
            .build(false));
    public static final RenderType VOXEL_RENDER_PLAYER_INSIDE = RenderType.makeType("globe_voxel_render", DefaultVertexFormats.ENTITY, 7, 256, true, false, RenderType.State.getBuilder()
            .texture(RenderState.NO_TEXTURE)
            .transparency(RenderState.TRANSLUCENT_TRANSPARENCY)
            .diffuseLighting(RenderState.DIFFUSE_LIGHTING_ENABLED)
            .alpha(RenderState.DEFAULT_ALPHA)
            .fog(RenderState.NO_FOG)
            .lightmap(RenderState.LIGHTMAP_ENABLED)
            .cull(RenderState.CULL_DISABLED)
            .build(false));

    public ModelGlobeCreator modelGlobeCreator;
    public ModelRenderer voxel; //I'm lazy okay

    public TileRendererGlobeCreator(TileEntityRendererDispatcher renderer)
    {
        super(renderer);

        modelGlobeCreator = new ModelGlobeCreator();
        voxel = new ModelRenderer(64, 32, 0, 0);
        voxel.setRotationPoint(0.0F, 0.0F, 0.0F);
        voxel.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);

        ItemGlobeRenderer.RENDERER_GLOBE_CREATOR = this;
    }

    @Override
    public void render(@Nullable TileEntityGlobeCreator te, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        stack.push();

        stack.translate(0.5D, 0.5D, 0.5D);

        if(te != null && te.hasGlobe)
        {
            float bigProg = MathHelper.clamp(1.0F - ((te.timeToGlobe - 7 - partialTicks) / (te.totalGlobeTime - 7F)), 0F, 1F);
            float smallProg = MathHelper.clamp(1.0F - ((te.timeToGlobe - 7 - partialTicks) / 7F), 0F, 1F);
            float bigProgPow =  (float)Math.pow(bigProg, 2D);

            //render globe
            stack.push();

            stack.scale(0.8F, 0.8F, 0.8F);

            if(te.timeToGlobe >= 0)
            {
                stack.rotate(Vector3f.YP.rotationDegrees(7200F * bigProgPow + 360F * smallProg));
            }

            TileRendererGlobeStand.drawGlobe(stack, bufferIn, combinedLightIn, combinedOverlayIn, te.getWorld(), true, true, false, null, null, null, te.getPos(), 0, 0, 0F, partialTicks);

            stack.pop();
            //end render globe

            //render blocks
            if (te.timeToGlobe >= 0 && te.timeToGlobe <= 12 && te.itemTag != null && !te.itemTag.isEmpty())
            {
                stack.push();

                stack.translate(0D, -0.5D * (1.0F - smallProg), 0D);

                float scale = 1 / 0.05F * (3F / te.radius) * (1.0F - (float)Math.pow(smallProg, 2));
                stack.scale(scale, scale, scale);

                if(scale > 0.0D)
                {
                    if(te.renderingTEs == null)
                    {
                        te.renderingTEs = new HashMap<>();
                    }
                    if(te.renderingEnts == null)
                    {
                        te.renderingEnts = new HashSet<>();
                    }
                    TileRendererGlobeStand.drawGlobe(stack, bufferIn, combinedLightIn, combinedOverlayIn, te.getWorld(), false, false, true, te.itemTag, te.renderingTEs, te.renderingEnts, te.getPos(), 0, 0, 0F, partialTicks);
                }

                stack.pop();
            }
            //end render blocks

            //render light?
            if (te.timeToGlobe >= 0) //taken from EnderDragon Death
            {
                float f = bigProg;
                float f1 = 0.0F;

                if (f > 0.8F)
                {
                    f1 = (f - 0.8F) / 0.2F;
                }

                Random random = new Random(432L);
                stack.push();
                float scale1 = (float)((1D + ((0.4D * bigProgPow - 0.4D * smallProg) * (te.radius * 2)) - 1.4D * smallProg) * 0.05D);
                if(scale1 < 0F)
                {
                    scale1 = 0F;
                }
                stack.scale(scale1, scale1, scale1);

                IVertexBuilder ivertexbuilder2 = bufferIn.getBuffer(RenderType.getLightning());

                for (int i = 0; (float)i < (f + f * f) / 2.0F * 60.0F; ++i)
                {
                    stack.rotate(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
                    stack.rotate(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
                    stack.rotate(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F));
                    stack.rotate(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
                    stack.rotate(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
                    stack.rotate(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + f * 90.0F));
                    float f2 = random.nextFloat() * 20.0F + 5.0F + f1 * 10.0F;
                    float f3 = random.nextFloat() * 2.0F + 1.0F + f1 * 2.0F;
                    Matrix4f matrix4f = stack.getLast().getMatrix();
                    int j = (int)(255.0F * (1.0F - f1));
                    deathPosStart(ivertexbuilder2, matrix4f, j);
                    deathPosA(ivertexbuilder2, matrix4f, f2, f3);
                    deathPosB(ivertexbuilder2, matrix4f, f2, f3);
                    deathPosStart(ivertexbuilder2, matrix4f, j);
                    deathPosB(ivertexbuilder2, matrix4f, f2, f3);
                    deathPosC(ivertexbuilder2, matrix4f, f2, f3);
                    deathPosStart(ivertexbuilder2, matrix4f, j);
                    deathPosC(ivertexbuilder2, matrix4f, f2, f3);
                    deathPosA(ivertexbuilder2, matrix4f, f2, f3);
                }

                stack.pop();
            }
            //end render light

            //render first cube
            if(!te.globed)
            {
                stack.push();

                float alpha = 1F;
                if(te.timeToGlobe >= 0)
                {
                    float scale1 = (float)(1D + ((0.1D * bigProgPow - 0.1D * smallProg) * (te.radius * 2)) - 1.1D * smallProg);
                    if(scale1 < 0F)
                    {
                        scale1 = 0F;
                    }
                    stack.scale(scale1, scale1, scale1);
                    float rotVal = 3600F * bigProgPow + 200F * smallProg;
                    Quaternion quat = Vector3f.YP.rotationDegrees(rotVal * (10F * bigProg));
                    quat.multiply(Vector3f.XP.rotationDegrees(rotVal));
                    stack.rotate(quat);
                    alpha = 1F - 0.4F * bigProgPow;
                }

                modelGlobeCreator.render(stack, bufferIn.getBuffer(RenderType.getEntityTranslucent(TEX_GLOBE_CREATOR)), combinedLightIn, combinedOverlayIn, 1F, 1F, 1F, alpha);
                stack.pop();
            }
            //end render first cube

            //render boundaries
            if(te.timeToGlobe >= 0)
            {
                Entity player = Minecraft.getInstance().getRenderViewEntity();
                boolean playerInCube = player != null && player.getPosX() < te.getPos().getX() + te.radius + 1 && player.getPosX() > te.getPos().getX() - te.radius && player.getPosY() + player.getEyeHeight() < te.getPos().getY() + te.radius + 1 && player.getPosY() + player.getEyeHeight() > te.getPos().getY() - te.radius && player.getPosZ() < te.getPos().getZ() + te.radius + 1 && player.getPosZ() > te.getPos().getZ() - te.radius;
                stack.push();

                float scale2 = (float)((1D + (te.radius * 2)) * 16D + 0.0001D);

                stack.scale(scale2, scale2, scale2);

                voxel.render(stack, bufferIn.getBuffer(playerInCube ? VOXEL_RENDER_PLAYER_INSIDE : VOXEL_RENDER), combinedLightIn, OverlayTexture.NO_OVERLAY, 0F, 1F, 1F, (0.5F * bigProg - 0.5F * smallProg));

                stack.pop();
            }
            //end render boundaries
        }
        else
        {
            modelGlobeCreator.render(stack, bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(TEX_GLOBE_CREATOR)), combinedLightIn, combinedOverlayIn, 1F, 1F, 1F, 1F);
        }

        stack.pop();
    }

    private static void deathPosStart(IVertexBuilder buffer, Matrix4f matrix4f, int alpha) {
        buffer.pos(matrix4f, 0.0F, 0.0F, 0.0F).color(255, 255, 255, alpha).endVertex();
        buffer.pos(matrix4f, 0.0F, 0.0F, 0.0F).color(255, 255, 255, alpha).endVertex();
    }

    private static void deathPosA(IVertexBuilder buffer, Matrix4f matrix4f, float y, float prog) {
        buffer.pos(matrix4f, -0.866F * prog, y, -0.5F * prog).color(0, 255, 255, 0).endVertex();
    }

    private static void deathPosB(IVertexBuilder buffer, Matrix4f matrix4f, float y, float prog) {
        buffer.pos(matrix4f, 0.866F * prog, y, -0.5F * prog).color(0, 255, 255, 0).endVertex();
    }

    private static void deathPosC(IVertexBuilder buffer, Matrix4f matrix4f, float y, float prog) {
        buffer.pos(matrix4f, 0.0F, y, 1.0F * prog).color(0, 255, 255, 0).endVertex();
    }
}
