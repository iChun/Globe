package me.ichun.mods.globe.common.tileentity;

import me.ichun.mods.globe.common.Globe;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nullable;
import java.util.HashMap;

public class TileEntityGlobeCreator extends TileEntity implements ITickable
{
    public static final int GLOBE_TIME = 200;

    public boolean hasGlobe;
    public int timeToGlobe;
    public int totalGlobeTime;
    public int radius;
    public NBTTagCompound itemTag;

    @SideOnly(Side.CLIENT)
    public HashMap<String, TileEntity> renderingTEs;

    public boolean globed;
    public int lastLight;

    public TileEntityGlobeCreator()
    {
        timeToGlobe = -1;
        radius = 5;
        totalGlobeTime = 0;
        itemTag = new NBTTagCompound();
    }

    @Override
    public void update()
    {
        if(timeToGlobe > 0)
        {
            timeToGlobe--;
            if(timeToGlobe == 12)
            {
                if(!world.isRemote)
                {
                    //TODO animals?
                    itemTag.setInteger("radius", radius);
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
                                IBlockState state = world.getBlockState(refPos);
                                if(!(state.getMaterial() == Material.AIR || state.getBlockHardness(world, refPos) < 0))
                                {
                                    StringBuilder sb = new StringBuilder().append("x").append(x).append("y").append(y).append("z").append(z);
                                    String coord = sb.toString();
                                    NBTTagCompound coordTag = new NBTTagCompound();
                                    Block block = state.getBlock();
                                    ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(block);
                                    coordTag.setString("Block", resourcelocation.toString());
                                    coordTag.setByte("Data", (byte)state.getBlock().getMetaFromState(state));
                                    if(block.hasTileEntity(state))
                                    {
                                        TileEntity te = world.getTileEntity(refPos);
                                        if(te != null)
                                        {
                                            coordTag.setTag("TileEntityData", te.writeToNBT(new NBTTagCompound())); //TODO might have to change the xyz in the NBT;
                                        }
                                    }
                                    itemTag.setTag(coord, coordTag);
                                }
                            }
                        }
                    }
                    IBlockState state = world.getBlockState(pos);
                    world.notifyBlockUpdate(pos, state, state, 3);

                    //TODO remove the blocks
                }
            }
            else if(timeToGlobe == 0)
            {
                globed = true;
                if(!world.isRemote)
                {
                    ItemStack is = new ItemStack(Globe.itemGlobe, 1, 1);
                    itemTag.setString("identification", RandomStringUtils.randomAlphanumeric(20));
                    itemTag.setLong("source", getPos().toLong());

                    is.setTagCompound(itemTag);

                    EntityItem entityitem = new EntityItem(this.world, getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D, is);
                    entityitem.setPickupDelay(40);
                    world.spawnEntity(entityitem);

                    world.setBlockToAir(pos);
                }
            }
            if(world.isRemote)
            {
                int newLight = Globe.blockGlobeCreator.getLightValue(world.getBlockState(pos), world, pos);
                if(newLight != lastLight)
                {
                    world.checkLight(pos);
                }
                lastLight = newLight;
            }
        }
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        //TODO do trigger read.
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setBoolean("hasGlobe", hasGlobe);
        tag.setInteger("timeToGlobe", timeToGlobe);
        tag.setInteger("totalGlobeTime", totalGlobeTime);
        tag.setInteger("radius", radius);
        tag.setTag("itemTag", itemTag);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        hasGlobe = tag.getBoolean("hasGlobe");
        timeToGlobe = tag.getInteger("timeToGlobe");
        totalGlobeTime = tag.getInteger("totalGlobeTime");
        radius = tag.getInteger("radius");
        itemTag = tag.getCompoundTag("itemTag");
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass == 0 && timeToGlobe < 0 || pass == 1 && timeToGlobe >= 0;
    }

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
