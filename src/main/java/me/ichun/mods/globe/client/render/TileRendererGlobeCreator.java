package me.ichun.mods.globe.client.render;

import me.ichun.mods.globe.client.model.ModelGlobeCreator;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class TileRendererGlobeCreator extends TileEntitySpecialRenderer<TileEntityGlobeCreator>
{
    public static final ResourceLocation txGlobeCreator = new ResourceLocation("globe", "textures/model/creator.png");

    public ModelGlobeCreator modelGlobeCreator;

    public TileRendererGlobeCreator()
    {
        modelGlobeCreator = new ModelGlobeCreator();
    }

    public void render(TileEntityGlobeCreator te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

        bindTexture(txGlobeCreator);
        modelGlobeCreator.render(0.0625F);

        if(te != null && te.hasGlobe)
        {
            //render shit
            GlStateManager.pushMatrix();

            GlStateManager.scale(0.8F, 0.8F, 0.8F);

            TileRendererGlobeStand.drawGlobe(te.getWorld(), true, true, false, null, null, BlockPos.ORIGIN, partialTicks);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }
}
