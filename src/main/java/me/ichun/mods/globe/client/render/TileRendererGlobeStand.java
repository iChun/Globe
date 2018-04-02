package me.ichun.mods.globe.client.render;

import me.ichun.mods.globe.client.model.ModelGlobeStand;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;

public class TileRendererGlobeStand extends TileEntitySpecialRenderer<TileEntityGlobeStand>
{
    public static final ResourceLocation txGlobeStand = new ResourceLocation("globe", "textures/model/stand.png");

    public static int renderLevel = 0;

    public static HashSet<Class<? extends TileEntity>> classesNotToRender = new HashSet<>();

    public static ModelGlobeStand modelGlobeStand = new ModelGlobeStand();

    @Override
    public void render(TileEntityGlobeStand gs, double px, double py, double pz, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();

        GlStateManager.translate(px + 0.5D, py + 0.5D, pz + 0.5D);

        GlStateManager.translate(0, Math.sin(Math.toRadians((getWorld().getWorldTime() + partialTicks) * 5)) * 0.1D, 0); //bobbing

        GlStateManager.rotate((getWorld().getWorldTime() + partialTicks) * 2L, 0, 1, 0); //rotation

//        int i = gs.getWorld().getLight(gs.getPos().add(0, 1, 0), true);
//        int j = i % 65536;
//        int k = i / 65536;
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);

        int i = gs.getWorld().getCombinedLight(gs.getPos(), 0);
        float f = (float)(i & 65535);
        float f1 = (float)(i >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);

        drawGlobe(gs.getWorld(), true, true, true, gs.itemTag, gs.renderingTiles, gs.getPos(), partialTicks);

        GlStateManager.popMatrix();
    }

    public static void drawGlobe(World world, boolean drawBase, boolean drawGlass, boolean drawInternal, NBTTagCompound gsTag, HashMap<String, TileEntity> tileEntityMap, BlockPos gsPos, float partialTicks)
    {
        if(drawInternal && gsTag != null && gsTag.getInteger("radius") > 0 && renderLevel < 2)
        {
            renderLevel++;
            GlStateManager.pushMatrix();

            int radius = gsTag.getInteger("radius");
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

                        if(!gsTag.hasKey(coord))
                        {
                            continue;
                        }

                        NBTTagCompound tag = gsTag.getCompoundTag(coord);
                        IBlockState state = Block.getBlockFromName(tag.getString("Block")).getStateFromMeta(tag.getByte("Data") & 255);
                        NBTTagCompound teTag = null;

                        if (tag.hasKey("TileEntityData"))
                        {
                            teTag = tag.getCompoundTag("TileEntityData");
                        }

                        GlStateManager.pushMatrix();

                        if(state.getRenderType() == EnumBlockRenderType.MODEL && state.getRenderType() != EnumBlockRenderType.INVISIBLE) //todo fix liquids
                        {
                            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                            GlStateManager.disableLighting();
                            Tessellator tessellator = Tessellator.getInstance();
                            BufferBuilder bufferbuilder = tessellator.getBuffer();

                            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
                            GlStateManager.translate((float)(x - (double)gsPos.getX() - 0.5D), (float)(y - (double)gsPos.getY()), (float)(z - (double)gsPos.getZ() - 0.5D));
                            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                            blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, gsPos, bufferbuilder, false, MathHelper.getPositionRandom(BlockPos.fromLong(gsTag.getLong("source"))));
                            tessellator.draw();

                            GlStateManager.enableLighting();
                        }
                        if(teTag != null)
                        {
                            TileEntity renderTe = tileEntityMap.get(coord);
                            if(renderTe == null)
                            {
                                TileEntity te = state.getBlock().createTileEntity(world, state);
                                if(te != null)
                                {
                                    te.setPos(gsPos.add(0, 1, 0));
                                    te.setWorld(world);
                                    te.readFromNBT(teTag);
                                    tileEntityMap.put(coord, te);

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
            GlStateManager.popMatrix();

            renderLevel--;
        }

        //draw globe
        GlStateManager.pushMatrix();

        GlStateManager.translate(0F, -0.275F, 0F);

        float scale = 0.4F;
        GlStateManager.scale(scale, scale, scale);

        //draw base
        if(drawBase)
        {
            Minecraft.getMinecraft().getTextureManager().bindTexture(txGlobeStand);
            modelGlobeStand.render(0.0625F);
        }
        //end draw base

        //Draw glass
        if(drawGlass)
        {
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
            GlStateManager.translate((float)(-(double)gsPos.getX() - 0.5D), (float)(-(double)gsPos.getY()) + 0.3D, (float)(-(double)gsPos.getZ() - 0.5D));
            IBlockState glass = Blocks.GLASS.getDefaultState();
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(glass), glass, gsPos, bufferbuilder, false, MathHelper.getPositionRandom(BlockPos.ORIGIN));
            tessellator.draw();

            GlStateManager.enableLighting();
        }
        //end draw glass


        GlStateManager.popMatrix();
    }
}
