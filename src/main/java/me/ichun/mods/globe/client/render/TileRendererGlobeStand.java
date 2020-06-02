package me.ichun.mods.globe.client.render;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.netty.buffer.Unpooled;
import me.ichun.mods.globe.client.core.EventHandlerClient;
import me.ichun.mods.globe.client.model.ModelGlobeStand;
import me.ichun.mods.globe.client.model.ModelStand;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TileRendererGlobeStand extends TileEntityRenderer<TileEntityGlobeStand>
{
    public static final ResourceLocation txGlobeStand = new ResourceLocation("globe", "textures/model/stand.png");
    public static final ResourceLocation txStand = new ResourceLocation("globe", "textures/model/stand_actual.png");
    public static final BlockPos HEAVENS_ABOVE =  new BlockPos(0, 1000000, 0);

    public static MinecraftSessionService sessionService;

    public static int renderLevel = 0;

    public static HashSet<Class<?>> classesNotToRender = new HashSet<>();

    public static ModelGlobeStand modelGlobeStand = new ModelGlobeStand();
    public static ModelStand modelStand = new ModelStand();

    public TileRendererGlobeStand(TileEntityRendererDispatcher renderer)
    {
        super(renderer);

        ItemGlobeRenderer.RENDERER_GLOBE_STAND = this;
    }

    @Override
    public void render(@Nullable TileEntityGlobeStand gs, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        stack.push();

        stack.translate(0.5D, 0.5D, 0.5D);

        if(gs == null || gs.isStand)
        {
            stack.push();
            stack.translate(0, -0.375D, 0);
            stack.scale(1F, -1F, -1F);

            IVertexBuilder bufferStand = bufferIn.getBuffer(RenderType.getEntityCutout(txStand));
            modelStand.render(stack, bufferStand, combinedLightIn, combinedOverlayIn, 1F, 1F, 1F, 1F);
            modelStand.base1.render(stack, bufferStand, 0xf000f0, combinedOverlayIn, 1F, 1F, 1F, 1F);
            stack.pop();
        }

        if(gs != null && gs.itemTag != null)
        {
            if(gs.isStand)
            {
                stack.translate(gs.disX, Math.sin(Math.toRadians(gs.prevBobProg + (gs.bobProg - gs.prevBobProg) * partialTicks)) * 0.05D + 0.05D, gs.disZ); //bobbing
            }
            else
            {
                stack.translate(0, -0.25D, 0);
            }

            float rot = gs.prevRotation + (gs.rotation - gs.prevRotation) * partialTicks;
            stack.rotate(Vector3f.YP.rotationDegrees(rot)); //rotation

            drawGlobe(stack, bufferIn, combinedLightIn, combinedOverlayIn, gs.getWorld(), true, true, true, gs.itemTag, gs.renderingTiles, gs.renderingEnts, gs.getPos(), gs.snowTime, gs.ticks, rot, partialTicks);
        }
        stack.pop();
    }

    public static void drawGlobe(MatrixStack stack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, World world, boolean drawBase, boolean drawGlass, boolean drawInternal, CompoundNBT gsTag, HashMap<String, TileEntity> tileEntityMap, HashSet<Entity> entities, BlockPos gsPos, int snowTime, int ticks, float rotation, float partialTicks)
    {
        if(drawInternal && gsTag != null && gsTag.getInt("radius") > 0 && renderLevel < 2)
        {
            renderLevel++;
            stack.push();

            int radius = gsTag.getInt("radius");
            float scale = 0.05F * (3F / radius);
            stack.scale(scale, scale, scale);

            float f = MathHelper.clamp((snowTime - partialTicks) / 40F, 0F, 1F);

            if (f > 0.0F) //render snow
            {
                int i = 0;
                int j = 0;
                int k = 0;
                IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getEntityTranslucent(WorldRenderer.SNOW_TEXTURES));
                Matrix4f matrix4f = stack.getLast().getMatrix();

                //                GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F); //TODO omit this?
                int i1 = radius - 1;
                int i4 = (combinedLightIn * 3 + 15728880) / 4;
                int j4 = i4 >> 16 & 65535;
                int k4 = i4 & 65535;

                for(int ll = 0; ll < 2; ll++)
                {
                    stack.push();
                    stack.rotate(Vector3f.YP.rotationDegrees(90F * ll));
                    stack.translate(-0.5D, 0D, -0.5D);

                    float f1 = (float)(ticks) + partialTicks;

                    for(int k1 = k - i1; k1 <= k + i1; ++k1)
                    {
                        for(int l1 = i - i1; l1 <= i + i1; ++l1)
                        {
                            double d3 = (double)radius * 0.5D;
                            double d4 = (double)radius * 0.5D;
                            int k2 = j - i1 - 1;
                            int l2 = j + i1 + 3;

                            Random rand = new Random((long)(l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761));

                            double d8 = (double)(-((float)(ticks & 511) + partialTicks) / 512.0F);
                            double d9 = rand.nextDouble() + (double)f1 * 0.01D * (double)((float)rand.nextGaussian());
                            double d10 = rand.nextDouble() + (double)(f1 * (float)rand.nextGaussian()) * 0.001D;
                            double d11 = (double)((float)l1 + 0.5F);
                            double d12 = (double)((float)k1 + 0.5F);
                            float f6 = MathHelper.sqrt(d11 * d11 + d12 * d12) / (float)i1;
                            float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f;

                            buffer.pos(matrix4f, (float)(l1 - d3 + 0.5D), (float)l2, (float)(k1 - d4 + 0.5D)).color(1.0F, 1.0F, 1.0F, f5).tex((float)(0.0D + d9), (float)(k2 * 0.25D + d8 + d10)).overlay(combinedOverlayIn).lightmap(j4, k4).normal(0F, 1F, 0F).endVertex();
                            buffer.pos(matrix4f, (float)(l1 + d3 + 0.5D), (float)l2, (float)(k1 + d4 + 0.5D)).color(1.0F, 1.0F, 1.0F, f5).tex((float)(1.0D + d9), (float)(k2 * 0.25D + d8 + d10)).overlay(combinedOverlayIn).lightmap(j4, k4).normal(0F, 1F, 0F).endVertex();
                            buffer.pos(matrix4f, (float)(l1 + d3 + 0.5D), (float)k2, (float)(k1 + d4 + 0.5D)).color(1.0F, 1.0F, 1.0F, f5).tex((float)(1.0D + d9), (float)(l2 * 0.25D + d8 + d10)).overlay(combinedOverlayIn).lightmap(j4, k4).normal(0F, 1F, 0F).endVertex();
                            buffer.pos(matrix4f, (float)(l1 - d3 + 0.5D), (float)k2, (float)(k1 - d4 + 0.5D)).color(1.0F, 1.0F, 1.0F, f5).tex((float)(0.0D + d9), (float)(l2 * 0.25D + d8 + d10)).overlay(combinedOverlayIn).lightmap(j4, k4).normal(0F, 1F, 0F).endVertex();
                        }
                    }

                    stack.pop();
                }

            }

            //TODO holy mother of god cache this omg.
            //render blocks
            for(int x = -radius; x <= radius; x++)
            {
                for(int y = -radius; y <= radius; y++)
                {
                    for(int z = -radius; z <= radius; z++)
                    {
                        StringBuilder sb = new StringBuilder().append("x").append(x).append("y").append(y).append("z").append(z);
                        String coord = sb.toString();

                        if(!gsTag.contains(coord))
                        {
                            continue;
                        }

                        CompoundNBT tag = gsTag.getCompound(coord);
                        BlockState state = NBTUtil.readBlockState(tag.getCompound("BlockState"));
                        CompoundNBT teTag = null;

                        if(tag.contains("TileEntityData"))
                        {
                            teTag = tag.getCompound("TileEntityData");
                        }

                        if(state.getRenderType() != BlockRenderType.INVISIBLE)
                        {
                            stack.push();
                            Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                            if(state.getRenderType() == BlockRenderType.MODEL) //todo fix liquids properly
                            {
                                stack.translate((float)(x - 0.5D), (float)(y), (float)(z - 0.5D));

                                for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.getBlockRenderTypes()) {
                                    if (RenderTypeLookup.canRenderInLayer(state, type)) {
                                        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(type);
                                        blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, HEAVENS_ABOVE, stack, bufferIn.getBuffer(type), false, new Random(), state.getPositionRandom(BlockPos.fromLong(gsTag.getLong("source"))), combinedOverlayIn);
                                    }
                                }
                                net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
                            }
                            stack.pop();
                        }
                        if(teTag != null)
                        {
                            Class<?> clz = null;
                            try
                            {
                                TileEntity renderTe = tileEntityMap.get(coord);
                                if(renderTe == null)
                                {
                                    TileEntity te = state.getBlock().createTileEntity(state, world);
                                    if(te != null)
                                    {
                                        clz = te.getClass();

                                        te.setWorldAndPos(world, gsPos.add(0, 1, 0));//TODO look into setWorldCreate in TileEntity. Use TileEntity.create?
                                        te.read(teTag); //todo this can cause a crash. check ichunutil
                                        tileEntityMap.put(coord, te);

                                        renderTe = te;
                                    }
                                }
                                if(renderTe != null && !classesNotToRender.contains(renderTe.getClass()))
                                {
                                    stack.push();
                                    stack.translate(x - 0.5D, y, z - 0.5D);
                                    TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(renderTe);
                                    if(renderer != null)
                                    {
                                        renderer.render(renderTe, partialTicks, stack, bufferIn, combinedLightIn, combinedOverlayIn);
                                    }
                                    stack.pop();
                                }
                            }
                            catch(Throwable e)
                            {
                                if(clz != null)
                                {
                                    classesNotToRender.add(clz);
                                }
                                //TODO log... at some point
                            }
                        }
                        if(false && tag.contains("FluidState"))//TODO fix liquid rendering
                        {
                            IFluidState fluidState = TileEntityGlobeCreator.readFluidState(tag.getCompound("FluidState"));
                            if(!fluidState.isEmpty())
                            {
                                stack.push();
                                Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                                stack.translate((float)(x - 0.5D), (float)(y), (float)(z - 0.5D));

                                for (RenderType rendertype : RenderType.getBlockRenderTypes())
                                {
                                    net.minecraftforge.client.ForgeHooksClient.setRenderLayer(rendertype);
                                    if(RenderTypeLookup.canRenderInLayer(fluidState, rendertype))
                                    {
                                        blockrendererdispatcher.renderFluid(HEAVENS_ABOVE, world, bufferIn.getBuffer(rendertype), fluidState);
                                    }
                                }
                                net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
                                stack.pop();
                            }
                        }
                    }
                }
            }
            //end render blocks

            //draw ents
            int entityCount = gsTag.getInt("entityCount"); //TODO fix the entity render in GUI changing normalize
            boolean create = entities.isEmpty();
            if(create && entityCount > 0)
            {
                for(int i = 0; i < entityCount; i++)
                {
                    Optional<Entity> entOpt = EntityType.loadEntityUnchecked(gsTag.getCompound("ent" + i), world);
                    if(entOpt.isPresent())
                    {
                        Entity ent = entOpt.get();
                        entities.add(ent);
                        if(ent instanceof LivingEntity)
                        {
                            ent.setPosition(ent.getPosX(), ent.getPosY() + 500D, ent.getPosZ());
                        }
                        ent.noClip = true;
                        ent.tick();
                        if(ent instanceof LivingEntity)
                        {
                            ent.setPosition(ent.getPosX(), ent.getPosY() - 500D, ent.getPosZ());
                        }
                    }
                }
                if(entities.isEmpty())
                {
                    gsTag.putInt("entityCount", 0);
                }
            }
            int playerCount = gsTag.getInt("playerCount");
            if(create && playerCount > 0)
            {
                for(int i = 0; i < playerCount; i++)
                {
                    GameProfile gp = NBTUtil.readGameProfile(gsTag.getCompound("player" + i).getCompound("Globe_GameProfile"));
                    if(gp != null)
                    {
                        Property property = (Property)Iterables.getFirst(gp.getProperties().get("textures"), (Object)null);

                        if (property == null)
                        {
                            if(sessionService == null)
                            {
                                YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy(), UUID.randomUUID().toString());
                                sessionService = yggdrasilauthenticationservice.createMinecraftSessionService();
                            }
                            gp = sessionService.fillProfileProperties(gp, true);
                        }

                        if(Minecraft.getInstance().getConnection().getPlayerInfo(gp.getId()) == null)
                        {
                            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                            //taken from SPlayerListItemPacket
                            buf.writeEnumValue(SPlayerListItemPacket.Action.ADD_PLAYER);
                            buf.writeVarInt(1);
                            buf.writeUniqueId(gp.getId());
                            buf.writeString(gp.getName());
                            buf.writeVarInt(gp.getProperties().size());
                            for(Property property1 : gp.getProperties().values()) {
                                buf.writeString(property1.getName());
                                buf.writeString(property1.getValue());
                                if (property1.hasSignature()) {
                                    buf.writeBoolean(true);
                                    buf.writeString(property1.getSignature());
                                } else {
                                    buf.writeBoolean(false);
                                }
                            }

                            buf.writeVarInt(GameType.NOT_SET.getID());
                            buf.writeVarInt(-1);
                            buf.writeBoolean(false);

                            SPlayerListItemPacket packet = new SPlayerListItemPacket();

                            try
                            {
                                packet.readPacketData(buf);
                                NetworkPlayerInfo info = new NetworkPlayerInfo(packet.getEntries().get(0));
                                ObfuscationReflectionHelper.findMethod(NetworkPlayerInfo.class, "func_178838_a", int.class).invoke(info, -100);
                                EventHandlerClient.getMcPlayerInfoMap().put(gp.getId(), info);
                            }
                            catch(ObfuscationReflectionHelper.UnableToFindMethodException | IllegalAccessException | InvocationTargetException | IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        RemoteClientPlayerEntity mp = new RemoteClientPlayerEntity((ClientWorld)world, gp);
                        mp.read(gsTag.getCompound("player" + i));
                        mp.setPosition(mp.getPosX(), mp.getPosY() + 500D, mp.getPosZ());
                        mp.tick();
                        mp.setPosition(mp.getPosX(), mp.getPosY() - 500D, mp.getPosZ());
                        entities.add(mp);
                    }
                }
            }
            for(Entity ent : entities)
            {
                stack.push();

                BlockPos source = BlockPos.fromLong(gsTag.getLong("source")).equals(BlockPos.ZERO) ? gsPos : BlockPos.fromLong(gsTag.getLong("source"));

                stack.translate(ent.getPosX() - source.getX() - 0.5D, ent.getPosY() - source.getY(), ent.getPosZ() - source.getZ() - 0.5D);

                EntityRenderer render = Minecraft.getInstance().getRenderManager().getRenderer(ent);
                stack.push();
                try
                {
                    render.render(ent, ent.rotationYaw, partialTicks, stack, bufferIn, combinedLightIn);
                }
                catch(Throwable ignored){}
                stack.pop();

                stack.pop();
            }

            //end draw ents

            stack.pop();

            renderLevel--;
        }

        //draw globe
        stack.push();

        stack.translate(0F, -0.275F, 0F);

        float scale = 0.4F;
        stack.scale(scale, scale, scale);

        //draw base
        if(drawBase)
        {
            modelGlobeStand.render(stack, bufferIn.getBuffer(RenderType.getEntityCutout(txGlobeStand)), combinedLightIn, combinedOverlayIn, 1F, 1F, 1F, 1F);
        }
        //end draw base

        //Draw glass
        if(drawGlass)
        {
            Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            stack.translate((float)(-0.5D), (float)(+ 0.3D), (float)(-0.5D));
            Block block = gsTag != null && gsTag.contains("glassType") ? ForgeRegistries.BLOCKS.getValue(new ResourceLocation(gsTag.getString("glassType"))) : null;
            BlockState glass = block != null ? block.getDefaultState() : Blocks.GLASS.getDefaultState();
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
            for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.getBlockRenderTypes()) {
                if (RenderTypeLookup.canRenderInLayer(glass, type)) {
                    net.minecraftforge.client.ForgeHooksClient.setRenderLayer(type);
                    blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(glass), glass, HEAVENS_ABOVE, stack, bufferIn.getBuffer(type), false, new Random(), glass.getPositionRandom(HEAVENS_ABOVE), combinedOverlayIn);
                }
            }
            net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
        }
        //end draw glass


        stack.pop();
    }
}
