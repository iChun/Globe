package me.ichun.mods.globe.client.render;

import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;

public class TileRendererGlobeStand extends TileEntitySpecialRenderer<TileEntityGlobeStand>
{
    public static int renderLevel = 0;

    public HashSet<Class<? extends TileEntity>> classesNotToRender = new HashSet<>();

    @Override
    public void render(TileEntityGlobeStand gs, double px, double py, double pz, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();

        GlStateManager.translate(px + 0.5D, py + 0.5D, pz + 0.5D);

        GlStateManager.translate(0, 1, 0); //TODO remove this


//        int i = gs.getWorld().getLight(gs.getPos().add(0, 1, 0), true);
//        int j = i % 65536;
//        int k = i / 65536;
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);

        int i = gs.getWorld().getCombinedLight(gs.getPos(), 0);
        float f = (float)(i & 65535);
        float f1 = (float)(i >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);

        if(gs.itemTag != null && gs.itemTag.getInteger("radius") > 0 && renderLevel < 2)
        {
            renderLevel++;

            int radius = gs.itemTag.getInteger("radius");
            float scale = 0.05F * (3F / radius);
            GlStateManager.scale(scale, scale, scale);
            for(int x = -radius; x <= radius; x++)
            {
                for(int y = -radius; y <= radius; y++)
                {
                    for(int z = -radius; z <= radius; z++)
                    {
                        StringBuilder sb = new StringBuilder().append("x").append(x).append("y").append(y).append("z").append(z);
                        String coord = sb.toString();

                        if(!gs.itemTag.hasKey(coord))
                        {
                            continue;
                        }

                        NBTTagCompound tag = gs.itemTag.getCompoundTag(coord);
                        IBlockState state = Block.getBlockFromName(tag.getString("Block")).getStateFromMeta(tag.getByte("Data") & 255);
                        NBTTagCompound teTag = null;

                        if (tag.hasKey("TileEntityData"))
                        {
                            teTag = tag.getCompoundTag("TileEntityData");
                        }

                        GlStateManager.pushMatrix();

                        if(state.getRenderType() == EnumBlockRenderType.MODEL && state != gs.getWorld().getBlockState(gs.getPos()) && state.getRenderType() != EnumBlockRenderType.INVISIBLE)
                        {
                            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                            GlStateManager.disableLighting();
                            Tessellator tessellator = Tessellator.getInstance();
                            BufferBuilder bufferbuilder = tessellator.getBuffer();

                            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
                            GlStateManager.translate((float)(x - (double)gs.getPos().getX() - 0.5D), (float)(y - (double)gs.getPos().getY()), (float)(z - (double)gs.getPos().getZ() - 0.5D));
                            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                            blockrendererdispatcher.getBlockModelRenderer().renderModel(gs.getWorld(), blockrendererdispatcher.getModelForState(state), state, gs.getPos(), bufferbuilder, false, MathHelper.getPositionRandom(BlockPos.fromLong(gs.itemTag.getLong("source"))));
                            tessellator.draw();

                            GlStateManager.enableLighting();
                        }
                        if(teTag != null)
                        {
                            TileEntity renderTe = gs.renderingTiles.get(coord);
                            if(renderTe == null)
                            {
                                TileEntity te = state.getBlock().createTileEntity(gs.getWorld(), state);
                                if(te != null)
                                {
                                    te.setPos(gs.getPos().add(0, 1, 0));
                                    te.setWorld(gs.getWorld());
                                    te.readFromNBT(teTag);
                                    gs.renderingTiles.put(coord, te);

                                    renderTe = te;
                                }
                            }
                            if(renderTe != null && !classesNotToRender.contains(renderTe.getClass()))
                            {
                                try
                                {
                                    TileEntityRendererDispatcher.instance.render(renderTe, x - 0.5D, y, z - 0.5D, partialTicks);
                                }
                                catch(ReportedException e)
                                {
                                    classesNotToRender.add(renderTe.getClass());
                                    //TODO log
                                }
                            }
                        }

                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                        GlStateManager.popMatrix();
                    }
                }
            }
            renderLevel--;
        }

        GlStateManager.popMatrix();
    }
}
