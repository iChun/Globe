package me.ichun.mods.globe.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.ichun.mods.globe.common.Globe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityGlobeCreator extends TileEntity implements ITickableTileEntity
{
    public static final int GLOBE_TIME = 200; //TODO blacklist blocks
    public static final DamageSource ds = new DamageSource("globe.globeProcess").setDamageBypassesArmor();

    public boolean hasGlobe;
    public int timeToGlobe;
    public int totalGlobeTime;
    public int radius;
    public CompoundNBT itemTag;

    public HashMap<String, TileEntity> renderingTEs;
    public HashSet<Entity> renderingEnts;

    public boolean globed;
    public int lastLight;
    public static boolean soundCached;

    public TileEntityGlobeCreator()
    {
        super(Globe.TileEntityTypes.GLOBE_CREATOR.get());
        timeToGlobe = -1;
        radius = 5;
        totalGlobeTime = 0;
        itemTag = new CompoundNBT();
    }

    @Override
    public void tick()
    {
        if(!soundCached && world.isRemote)
        {
            soundCached = true;
            world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Globe.Sounds.CHARGEUP.get(), SoundCategory.BLOCKS, 0.0005F, 1F);
            world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Globe.Sounds.DING.get(), SoundCategory.BLOCKS, 0.0005F, 1F);
        }
        if(timeToGlobe > 0)
        {
            timeToGlobe--;
            if(timeToGlobe == 12)
            {
                if(!world.isRemote)
                {
                    itemTag.putInt("radius", radius);

                    List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(getPos()).grow(radius, radius, radius));

                    int playerCount = 0;
                    int entityCount = 0;
                    for(Entity ent : entities)
                    {
                        if(ent instanceof PlayerEntity)
                        {
                            if(ent instanceof FakePlayer) //TODO proper fake player checks
                            {
                                continue;
                            }
                            CompoundNBT tag = new CompoundNBT();
                            ent.writeWithoutTypeId(tag);
                            CompoundNBT nbttagcompound = new CompoundNBT();
                            NBTUtil.writeGameProfile(nbttagcompound, ((PlayerEntity)ent).getGameProfile());
                            tag.put("Globe_GameProfile", nbttagcompound);
                            itemTag.put("player" + playerCount, tag);
                            playerCount++;

                            if(!(((PlayerEntity)ent).abilities.isCreativeMode || ent.isSpectator()))
                            {
                                ent.attackEntityFrom(ds, 1000000F);
                            }
                        }
                        else
                        {
                            CompoundNBT tag = new CompoundNBT();
                            if(ent.writeUnlessRemoved(tag))
                            {
                                itemTag.put("ent" + entityCount, tag);
                                entityCount++;
                            }
                        }
                    }
                    itemTag.putInt("playerCount", playerCount);
                    itemTag.putInt("entityCount", entityCount);

                    for(int x = -radius; x <= radius; x++)
                    {
                        for(int y = -radius; y <= radius; y++)
                        {
                            for(int z = -radius; z <= radius; z++)
                            {
                                if(x == 0 && y == 0 && z == 0)
                                {
                                    continue;
                                }
                                BlockPos refPos = getPos().add(x, y, z);
                                BlockState state = world.getBlockState(refPos);
                                if(!(state.getMaterial() == Material.AIR || state.getBlockHardness(world, refPos) < 0))
                                {
                                    StringBuilder sb = new StringBuilder().append("x").append(x).append("y").append(y).append("z").append(z);
                                    String coord = sb.toString();
                                    CompoundNBT coordTag = new CompoundNBT();
                                    coordTag.put("BlockState", NBTUtil.writeBlockState(state));
                                    if(state.hasTileEntity())
                                    {
                                        TileEntity te = world.getTileEntity(refPos);
                                        if(te != null)
                                        {
                                            coordTag.put("TileEntityData", te.write(new CompoundNBT())); //TODO might have to change the xyz in the NBT;
                                        }
                                    }
                                    IFluidState fluidState = world.getFluidState(refPos);
                                    if(!fluidState.isEmpty()) // we have a fluid
                                    {
                                        coordTag.put("FluidState", writeFluidState(fluidState));
                                    }
                                    itemTag.put(coord, coordTag);
                                }
                            }
                        }
                    }

                    for(int x = -radius; x <= radius; x++)
                    {
                        for(int y = -radius; y <= radius; y++)
                        {
                            for(int z = -radius; z <= radius; z++)
                            {
                                if(x == 0 && y == 0 && z == 0)
                                {
                                    continue;
                                }
                                BlockPos refPos = getPos().add(x, y, z);
                                BlockState state = world.getBlockState(refPos);
                                if(!(state.getMaterial() == Material.AIR || state.getBlockHardness(world, refPos) < 0))
                                {
                                    world.setBlockState(refPos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
                                }
                            }
                        }
                    }

                    entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(getPos()).grow(radius, radius, radius));

                    for(Entity ent : entities)
                    {
                        if(!(ent instanceof PlayerEntity))
                        {
                            ent.remove();
                        }
                    }

                    BlockState state = world.getBlockState(pos);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
            }
            else if(timeToGlobe == 0)
            {
                globed = true;
                if(!world.isRemote)
                {
                    ItemStack is = new ItemStack(Globe.Items.GLOBE.get(), 1);
                    itemTag.putString("identification", RandomStringUtils.randomAlphanumeric(20));
                    itemTag.putLong("source", getPos().toLong());

                    is.setTag(itemTag);
                    is.setDamage(1);

                    ItemEntity entityitem = new ItemEntity(this.world, getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D, is);
                    entityitem.setPickupDelay(40);
                    world.addEntity(entityitem);

                    world.removeBlock(pos, false);
                }
                world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Globe.Sounds.DING.get(), SoundCategory.BLOCKS, 0.3F, 1F);
            }
            if(world.isRemote)
            {
                int newLight = Globe.Blocks.GLOBE_CREATOR.get().getLightValue(world.getBlockState(pos), world, pos);
                if(newLight != lastLight)
                {
                    world.getChunkProvider().getLightManager().checkBlock(pos);
                }
                lastLight = newLight;
            }
        }
    }

    //Mostly taken from NBTUtil
    public static IFluidState readFluidState(CompoundNBT tag) {
        if (!tag.contains("Name", 8)) {
            return Fluids.EMPTY.getDefaultState();
        } else {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("Name")));
            if(fluid == null)
            {
                return Fluids.EMPTY.getDefaultState();
            }
            IFluidState fluidState = fluid.getDefaultState();
            if (tag.contains("Properties", 10)) {
                CompoundNBT compoundnbt = tag.getCompound("Properties");
                StateContainer<Fluid, IFluidState> statecontainer = fluid.getStateContainer();

                for(String s : compoundnbt.keySet()) {
                    IProperty<?> iproperty = statecontainer.getProperty(s);
                    if (iproperty != null) {
                        fluidState = setValueHelper(fluidState, iproperty, s, compoundnbt, tag);
                    }
                }
            }

            return fluidState;
        }
    }

    private static <S extends IStateHolder<S>, T extends Comparable<T>> S setValueHelper(S p_193590_0_, IProperty<T> p_193590_1_, String p_193590_2_, CompoundNBT p_193590_3_, CompoundNBT p_193590_4_) {
        Optional<T> optional = p_193590_1_.parseValue(p_193590_3_.getString(p_193590_2_));
        if (optional.isPresent()) {
            return (S)(p_193590_0_.with(p_193590_1_, (T)(optional.get())));
        } else {
            // LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", p_193590_2_, p_193590_3_.getString(p_193590_2_), p_193590_4_.toString());
            return p_193590_0_;
        }
    }

    public static CompoundNBT writeFluidState(IFluidState tag) {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.putString("Name", tag.getFluid().getRegistryName().toString());
        ImmutableMap<IProperty<?>, Comparable<?>> immutablemap = tag.getValues();
        if (!immutablemap.isEmpty()) {
            CompoundNBT compoundnbt1 = new CompoundNBT();

            for(Map.Entry<IProperty<?>, Comparable<?>> entry : immutablemap.entrySet()) {
                IProperty<?> iproperty = entry.getKey();
                compoundnbt1.putString(iproperty.getName(), getName(iproperty, entry.getValue()));
            }

            compoundnbt.put("Properties", compoundnbt1);
        }

        return compoundnbt;
    }

    private static <T extends Comparable<T>> String getName(IProperty<T> p_190010_0_, Comparable<?> p_190010_1_) {
        return p_190010_0_.getName((T)p_190010_1_);
    }
    //End Mostly taken from NBTUtil

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        this.read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        super.write(tag);
        tag.putBoolean("hasGlobe", hasGlobe);
        tag.putInt("timeToGlobe", timeToGlobe);
        tag.putInt("totalGlobeTime", totalGlobeTime);
        tag.putInt("radius", radius);
        tag.put("itemTag", itemTag);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag)
    {
        super.read(tag);
        hasGlobe = tag.getBoolean("hasGlobe");
        timeToGlobe = tag.getInt("timeToGlobe");
        totalGlobeTime = tag.getInt("totalGlobeTime");
        radius = tag.getInt("radius");
        itemTag = tag.getCompound("itemTag");
    }

    //    @Override //TODO remember this
    //    public boolean shouldRenderInPass(int pass)
    //    {
    //        return pass == 0 && timeToGlobe < 0 || pass == 1 && timeToGlobe >= 0;
    //    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        if(timeToGlobe < 0)
        {
            return new AxisAlignedBB(pos, pos.add(1, 1, 1));
        }
        else
        {
            return new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(radius + 1, radius + 1, radius + 1));
        }
    }
}
