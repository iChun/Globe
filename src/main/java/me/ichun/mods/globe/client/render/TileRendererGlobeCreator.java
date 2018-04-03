package me.ichun.mods.globe.client.render;

import me.ichun.mods.globe.client.model.ModelGlobeCreator;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class TileRendererGlobeCreator extends TileEntitySpecialRenderer<TileEntityGlobeCreator>
{
    public static final ResourceLocation txGlobeCreator = new ResourceLocation("globe", "textures/model/creator.png");

    public ModelGlobeCreator modelGlobeCreator;
    public ModelRenderer voxel; //I'm lazy okay

    public TileRendererGlobeCreator()
    {
        modelGlobeCreator = new ModelGlobeCreator();
        voxel = new ModelRenderer(new ModelBase() {}, 0, 0);
        voxel.setRotationPoint(0.0F, 0.0F, 0.0F);
        voxel.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
    }

    public void render(TileEntityGlobeCreator te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

        if(te != null && te.hasGlobe)
        {
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);

            float bigProg = MathHelper.clamp(1.0F - ((te.timeToGlobe - 7 - partialTicks) / (te.totalGlobeTime - 7F)), 0F, 1F);
            float smallProg = MathHelper.clamp(1.0F - ((te.timeToGlobe - 7 - partialTicks) / 7F), 0F, 1F);
            float bigProgPow =  (float)Math.pow(bigProg, 2D);

            //render globe
            GlStateManager.pushMatrix();

            GlStateManager.scale(0.8F, 0.8F, 0.8F);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if(te.timeToGlobe >= 0)
            {
                GlStateManager.depthMask(true);
                GlStateManager.enableCull();
                GlStateManager.rotate(7200F * bigProgPow + 360F * smallProg, 0F, 1F, 0F);
            }

            TileRendererGlobeStand.drawGlobe(te.getWorld(), true, true, false, null, null, null, BlockPos.ORIGIN, 0, 0, 0F, partialTicks);

            GlStateManager.popMatrix();
            //end render globe

            //render blocks
            if (te.timeToGlobe >= 0 && te.timeToGlobe <= 12 && te.itemTag != null && !te.itemTag.hasNoTags())
            {
                int i = te.getWorld().getCombinedLight(te.getPos(), 0);
                float f = (float)(i & 65535);
                float f1 = (float)(i >> 16);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);

                GlStateManager.pushMatrix();

                GlStateManager.translate(0D, -0.5D * (1.0F - smallProg), 0D);

                float scale = 1 / 0.05F * (3F / te.radius) * (1.0F - (float)Math.pow(smallProg, 2));
                GlStateManager.scale(scale, scale, scale);

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
                    TileRendererGlobeStand.drawGlobe(te.getWorld(), false, false, true, te.itemTag, te.renderingTEs, te.renderingEnts, te.getPos(), 0, 0, 0F, partialTicks);
                }

                GlStateManager.popMatrix();
            }
            //end render blocks

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            //render light?
            if (te.timeToGlobe >= 0)
            {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                float f = bigProg;
                float f1 = 0.0F;

                if (f > 0.8F)
                {
                    f1 = (f - 0.8F) / 0.2F;
                }

                Random random = new Random(432L);
                GlStateManager.disableTexture2D();
                GlStateManager.shadeModel(7425);
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                GlStateManager.disableAlpha();
                GlStateManager.depthMask(false);
                GlStateManager.pushMatrix();
                double scale1 = (1D + ((0.4D * bigProgPow - 0.4D * smallProg) * (te.radius * 2)) - 1.4D * smallProg) * 0.05D;
                if(scale1 < 0D)
                {
                    scale1 = 0D;
                }
                GlStateManager.scale(scale1, scale1, scale1);

                for (int i = 0; (float)i < (f + f * f) / 2.0F * 60.0F; ++i)
                {
                    GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(random.nextFloat() * 360.0F + f * 90.0F, 0.0F, 0.0F, 1.0F);
                    float f2 = random.nextFloat() * 20.0F + 5.0F + f1 * 10.0F;
                    float f3 = random.nextFloat() * 2.0F + 1.0F + f1 * 2.0F;
                    bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
                    bufferbuilder.pos(0.0D, 0.0D, 0.0D).color(255, 255, 255, (int)(255.0F * (1.0F - f1))).endVertex();
                    bufferbuilder.pos(-0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(0, 255, 255, 0).endVertex();
                    bufferbuilder.pos(0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(0, 255, 255, 0).endVertex();
                    bufferbuilder.pos(0.0D, (double)f2, (double)(1.0F * f3)).color(0, 255, 255, 0).endVertex();
                    bufferbuilder.pos(-0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(0, 255, 255, 0).endVertex();
                    tessellator.draw();
                }

                GlStateManager.popMatrix();
                GlStateManager.depthMask(true);
                GlStateManager.shadeModel(7424);
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableTexture2D();
                GlStateManager.enableAlpha();
            }
            //end render light

            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            //render first cube
            if(!te.globed)
            {
                GlStateManager.pushMatrix();

                if(te.timeToGlobe >= 0)
                {
                    double scale1 = 1D + ((0.1D * bigProgPow - 0.1D * smallProg) * (te.radius * 2)) - 1.1D * smallProg;
                    if(scale1 < 0D)
                    {
                        scale1 = 0D;
                    }
                    GlStateManager.scale(scale1, scale1, scale1);
                    GlStateManager.rotate(3600F * bigProgPow + 200F * smallProg, 1F, 10F * bigProg, 0F);
                    GlStateManager.color(1F, 1F, 1F, 1F - 0.4F * bigProgPow);
                }

                bindTexture(txGlobeCreator);
                modelGlobeCreator.render(0.0625F);
                GlStateManager.popMatrix();
            }
            //end render first cube

            //render boundaries
            if(te.timeToGlobe >= 0)
            {
                Entity player = Minecraft.getMinecraft().getRenderViewEntity();
                boolean playerInCube = player != null && player.posX < te.getPos().getX() + te.radius + 1 && player.posX > te.getPos().getX() - te.radius && player.posY + player.getEyeHeight() < te.getPos().getY() + te.radius + 1 && player.posY + player.getEyeHeight() > te.getPos().getY() - te.radius && player.posZ < te.getPos().getZ() + te.radius + 1 && player.posZ > te.getPos().getZ() - te.radius;
                GlStateManager.pushMatrix();

                GlStateManager.disableTexture2D();

                GlStateManager.color(0F, 1F, 1F, 0.5F * bigProg - 0.5F * smallProg);

                double scale2 = (1D + (te.radius * 2)) * 16D + 0.0001D;

                GlStateManager.scale(scale2, scale2, scale2);

                if(playerInCube)
                {
                    GlStateManager.disableCull();
                }

                voxel.render(0.0625F);

                if(playerInCube)
                {
                    GlStateManager.enableCull();
                }

                GlStateManager.enableTexture2D();

                GlStateManager.popMatrix();
            }
            //end render boundaries

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        }
        else
        {
            bindTexture(txGlobeCreator);
            modelGlobeCreator.render(0.0625F);
        }

        GlStateManager.popMatrix();
    }
}
