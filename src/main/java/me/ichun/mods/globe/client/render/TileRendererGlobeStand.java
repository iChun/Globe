package me.ichun.mods.globe.client.render;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import me.ichun.mods.globe.client.core.EventHandlerClient;
import me.ichun.mods.globe.client.model.ModelGlobeStand;
import me.ichun.mods.globe.client.model.ModelStand;
import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class TileRendererGlobeStand extends TileEntitySpecialRenderer<TileEntityGlobeStand>
{
    public static final ResourceLocation txGlobeStand = new ResourceLocation("globe", "textures/model/stand.png");
    public static final ResourceLocation txStand = new ResourceLocation("globe", "textures/model/stand_actual.png");
    public static final ResourceLocation txSnow = new ResourceLocation("textures/environment/snow.png");

    public static MinecraftSessionService sessionService;

    public static int renderLevel = 0;

    public static HashSet<Class<? extends TileEntity>> classesNotToRender = new HashSet<>();

    public static ModelGlobeStand modelGlobeStand = new ModelGlobeStand();
    public static ModelStand modelStand = new ModelStand();

    @Override
    public void render(TileEntityGlobeStand gs, double px, double py, double pz, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();

        GlStateManager.translate(px + 0.5D, py + 0.5D, pz + 0.5D);

        if(gs != null)
        {
            int i = gs.getWorld().getCombinedLight(gs.getPos(), 0);
            float f = (float)(i & 65535);
            float f1 = (float)(i >> 16);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
        }

        if(gs == null || gs.isStand)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, -0.375D, 0);
            GlStateManager.scale(1F, -1F, -1F);
            bindTexture(txStand);
            modelStand.render(0.0625F);
            if(gs != null)
            {
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            }
            modelStand.base1.render(0.0625F);
            if(gs != null)
            {
                int i = gs.getWorld().getCombinedLight(gs.getPos(), 0);
                float f = (float)(i & 65535);
                float f1 = (float)(i >> 16);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
            }
            GlStateManager.popMatrix();
        }

        if(gs != null && gs.itemTag != null)
        {
            if(gs.isStand)
            {
                GlStateManager.translate(gs.disX, Math.sin(Math.toRadians(gs.prevBobProg + (gs.bobProg - gs.prevBobProg) * partialTicks)) * 0.05D + 0.05D, gs.disZ); //bobbing
            }
            else
            {
                GlStateManager.translate(0, -0.25D, 0);
            }

            float rot = gs.prevRotation + (gs.rotation - gs.prevRotation) * partialTicks;
            GlStateManager.rotate(rot, 0, 1, 0); //rotation

            drawGlobe(gs.getWorld(), true, true, true, gs.itemTag, gs.renderingTiles, gs.renderingEnts, gs.getPos(), gs.snowTime, gs.ticks, rot, partialTicks);
        }
        GlStateManager.popMatrix();
    }

    public static void drawGlobe(World world, boolean drawBase, boolean drawGlass, boolean drawInternal, NBTTagCompound gsTag, HashMap<String, TileEntity> tileEntityMap, HashSet<Entity> entities, BlockPos gsPos, int snowTime, int ticks, float rotation, float partialTicks)
    {
        if(drawInternal && gsTag != null && gsTag.getInteger("radius") > 0 && renderLevel < 2)
        {
            renderLevel++;
            GlStateManager.pushMatrix();

            int radius = gsTag.getInteger("radius");
            float scale = 0.05F * (3F / radius);
            GlStateManager.scale(scale, scale, scale);

            Minecraft mc = Minecraft.getMinecraft();

            float f = MathHelper.clamp((snowTime - partialTicks) / 40F, 0F, 1F);

            if (f > 0.0F)
            {
                int i = 0;
                int j = 0;
                int k = 0;
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                GlStateManager.disableCull();
                GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.alphaFunc(516, 0.1F);
                int i1 = radius - 1;
                int i4 = (world.getCombinedLight(gsPos, 0) * 3 + 15728880) / 4;
                int j4 = i4 >> 16 & 65535;
                int k4 = i4 & 65535;

                for(int ll = 0; ll < 2; ll++)
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(90F * ll, 0F, 1F, 0F);
                    GlStateManager.translate(-0.5D, 0D, -0.5D);

                    int j1 = -1;
                    float f1 = (float)(ticks) + partialTicks;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                    for(int k1 = k - i1; k1 <= k + i1; ++k1)
                    {
                        for(int l1 = i - i1; l1 <= i + i1; ++l1)
                        {
                            double d3 = (double)radius * 0.5D;
                            double d4 = (double)radius * 0.5D;
                            int k2 = j - i1 - 1;
                            int l2 = j + i1 + 3;

                            Random rand = new Random((long)(l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761));

                            if(j1 != 1)
                            {
                                j1 = 1;
                                mc.getTextureManager().bindTexture(txSnow);
                                bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                            }

                            double d8 = (double)(-((float)(mc.world.getWorldTime() & 511) + partialTicks) / 512.0F);
                            double d9 = rand.nextDouble() + (double)f1 * 0.01D * (double)((float)rand.nextGaussian());
                            double d10 = rand.nextDouble() + (double)(f1 * (float)rand.nextGaussian()) * 0.001D;
                            double d11 = (double)((float)l1 + 0.5F);
                            double d12 = (double)((float)k1 + 0.5F);
                            float f6 = MathHelper.sqrt(d11 * d11 + d12 * d12) / (float)i1;
                            float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f;
                            bufferbuilder.pos((double)l1 - d3 + 0.5D, (double)l2, (double)k1 - d4 + 0.5D).tex(0.0D + d9, (double)k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            bufferbuilder.pos((double)l1 + d3 + 0.5D, (double)l2, (double)k1 + d4 + 0.5D).tex(1.0D + d9, (double)k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            bufferbuilder.pos((double)l1 + d3 + 0.5D, (double)k2, (double)k1 + d4 + 0.5D).tex(1.0D + d9, (double)l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            bufferbuilder.pos((double)l1 - d3 + 0.5D, (double)k2, (double)k1 - d4 + 0.5D).tex(0.0D + d9, (double)l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                        }
                    }

                    if (j1 >= 0)
                    {
                        tessellator.draw();
                    }

                    GlStateManager.popMatrix();
                }

                GlStateManager.enableCull();
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1F);
            }

            //render blocks
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

                        if(tag.hasKey("TileEntityData"))
                        {
                            teTag = tag.getCompoundTag("TileEntityData");
                        }

                        GlStateManager.pushMatrix();

                        if(state.getRenderType() != EnumBlockRenderType.INVISIBLE)
                        {
                            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                            GlStateManager.disableLighting();
                            Tessellator tessellator = Tessellator.getInstance();
                            BufferBuilder bufferbuilder = tessellator.getBuffer();
                            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
                            if(state.getRenderType() == EnumBlockRenderType.MODEL) //todo fix liquids properly
                            {
                                GlStateManager.translate((float)(x - (double)gsPos.getX() - 0.5D), (float)(y - (double)gsPos.getY()), (float)(z - (double)gsPos.getZ() - 0.5D));
                                blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, gsPos, bufferbuilder, false, MathHelper.getPositionRandom(BlockPos.fromLong(gsTag.getLong("source"))));
                            }
                            else if(state.getRenderType() == EnumBlockRenderType.LIQUID)
                            {
                                GlStateManager.translate((float)(x - (double)gsPos.getX() - 0.5D), (float)(y - (double)gsPos.getY()) + 0.8125F, (float)(z - (double)gsPos.getZ() - 0.5D));
                                blockrendererdispatcher.renderBlock(state, gsPos, world, bufferbuilder);
                            }
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
                                    //TODO log... at some point
                                }
                            }
                        }

                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                        GlStateManager.popMatrix();
                    }
                }
            }
            //end render blocks

            //draw ents
            int entityCount = gsTag.getInteger("entityCount"); //TODO fix the entity render in GUI changing normalize
            boolean create = entities.isEmpty();
            if(create && entityCount > 0)
            {
                for(int i = 0; i < entityCount; i++)
                {
                    Entity ent = EntityList.createEntityFromNBT(gsTag.getCompoundTag("ent" + i), world);
                    if(ent != null)
                    {
                        entities.add(ent);
                        if(ent instanceof EntityLivingBase)
                        {
                            ent.posY += 500D;
                        }
                        ent.noClip = true;
                        ent.onUpdate();
                        if(ent instanceof EntityLivingBase)
                        {
                            ent.posY -= 500D;
                        }
                    }
                }
                if(entities.isEmpty())
                {
                    gsTag.setInteger("entityCount", 0);
                }
            }
            int playerCount = gsTag.getInteger("playerCount");
            if(create && playerCount > 0)
            {
                for(int i = 0; i < playerCount; i++)
                {
                    GameProfile gp = NBTUtil.readGameProfileFromNBT(gsTag.getCompoundTag("player" + i).getCompoundTag("Globe_GamePlofile"));
                    if(gp != null)
                    {
                        Property property = (Property)Iterables.getFirst(gp.getProperties().get("textures"), (Object)null);

                        if (property == null)
                        {
                            if(sessionService == null)
                            {
                                YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
                                sessionService = yggdrasilauthenticationservice.createMinecraftSessionService();
                            }
                            gp = sessionService.fillProfileProperties(gp, true);
                        }

                        if(Minecraft.getMinecraft().getConnection().getPlayerInfo(gp.getId()) == null)
                        {
                            NetworkPlayerInfo info = new NetworkPlayerInfo(gp);
                            try
                            {
                                Method method = NetworkPlayerInfo.class.getDeclaredMethod("func_178838_a", int.class);
                                method.setAccessible(true);
                                method.invoke(info, -100);
                            }
                            catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) //TODO ew reflection
                            {
                                try
                                {
                                    Method method = NetworkPlayerInfo.class.getDeclaredMethod("setResponseTime", int.class);
                                    method.setAccessible(true);
                                    method.invoke(info, -100);
                                }
                                catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored1){}
                            }
                            EventHandlerClient.getMcPlayerInfoMap().put(gp.getId(), info);
                        }
                        EntityOtherPlayerMP mp = new EntityOtherPlayerMP(world, gp);
                        mp.readFromNBT(gsTag.getCompoundTag("player" + i));
                        mp.posY += 500D;
                        mp.onUpdate();
                        mp.posY -= 500D;
                        entities.add(mp);
                    }
                }
            }
            for(Entity ent : entities)
            {
                GlStateManager.pushMatrix();

                BlockPos source = BlockPos.fromLong(gsTag.getLong("source")).equals(BlockPos.ORIGIN) ? gsPos : BlockPos.fromLong(gsTag.getLong("source"));

                GlStateManager.translate(ent.posX - source.getX() - 0.5D, ent.posY - source.getY(), ent.posZ - source.getZ() - 0.5D);

                Render render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(ent);
                try
                {
                    float oriY = Minecraft.getMinecraft().getRenderManager().playerViewY;
                    Minecraft.getMinecraft().getRenderManager().playerViewY += rotation;
                    render.doRender(ent, 0D, 0D, 0D, ent.rotationYaw, partialTicks);
                    Minecraft.getMinecraft().getRenderManager().playerViewY = oriY;
                }
                catch(Exception ignored){}

                GlStateManager.popMatrix();
            }
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GlStateManager.color(1F, 1F, 1F, 1F);

            //end draw ents

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
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
            GlStateManager.translate((float)(-(double)gsPos.getX() - 0.5D), (float)(-(double)gsPos.getY()) + 0.3D, (float)(-(double)gsPos.getZ() - 0.5D));
            IBlockState glass = gsTag != null && gsTag.hasKey("glassType") ? Blocks.STAINED_GLASS.getStateFromMeta(gsTag.getInteger("glassType")) : Blocks.GLASS.getDefaultState();
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(glass), glass, gsPos, bufferbuilder, false, MathHelper.getPositionRandom(BlockPos.ORIGIN));
            tessellator.draw();

            GlStateManager.enableLighting();
        }
        //end draw glass


        GlStateManager.popMatrix();
    }
}
