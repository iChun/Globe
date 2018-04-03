package me.ichun.mods.globe.common.tileentity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;

public class TileEntityGlobeStand extends TileEntity implements ITickable
{
    @SideOnly(Side.CLIENT)
    public HashMap<String, TileEntity> renderingTiles = new HashMap<>();

    @SideOnly(Side.CLIENT)
    public HashSet<Entity> renderingEnts = new HashSet<>();

    public NBTTagCompound itemTag;

    public boolean isStand;

    public float disX;
    public float disZ;

    public float prevDisX;
    public float prevDisZ;

    public float rubberbandX;
    public float rubberbandZ;

    public float bobAmp;
    public float bobProg;
    public float prevBobProg;

    public float rotateFactor;
    public float rotation;
    public float prevRotation;

    public boolean updateLighting;
    public int ticks;

    public int snowTime; //5 seconds

    public TileEntityGlobeStand()
    {
        disX = disZ = 0F;
    }

    public TileEntityGlobeStand(NBTTagCompound itemTag, boolean isStand)
    {
        disX = disZ = 0F;
        this.itemTag = itemTag;
        this.isStand = isStand;
        snowTime = 0;
    }

    @Override
    public void update()
    {
        ticks++;
        if(!isStand)
        {
            rotateFactor = bobProg = bobAmp = 0F;
            if(world.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)
            {
                world.checkLight(pos);
            }
        }
        if(itemTag != null)
        {
            if(snowTime > 0)
            {
                snowTime--;
            }
            prevRotation = rotation;
            rotation += rotateFactor;

            rotateFactor *= 0.999F;

            prevDisX = disX;
            prevDisZ = disZ;

            prevBobProg = bobProg;
            bobProg = (bobProg + 5F * (1F + bobAmp)) % 3600000F;
            bobAmp *= 0.95F;

            disX += rubberbandX;
            disZ += rubberbandZ;

            disX *= 0.8F;
            disZ *= 0.8F;

            rubberbandX *= 0.9F;
            rubberbandZ *= 0.9F;

            rubberbandX += -disX / 0.8F;
            rubberbandZ += -disZ / 0.8F;
        }
        if(updateLighting)
        {
            updateLighting = false;
            world.checkLight(pos);
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
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        if(itemTag != null)
        {
            tag.setTag("itemTag", itemTag);
        }
        tag.setBoolean("isStand", isStand);
        tag.setFloat("rotateFactor", rotateFactor);
        tag.setFloat("rotation", rotation);
        tag.setFloat("bobProg", bobProg);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        if(tag.hasKey("itemTag"))
        {
            itemTag = tag.getCompoundTag("itemTag");
        }
        isStand = tag.getBoolean("isStand");
        rotateFactor = tag.getFloat("rotateFactor");
        prevRotation = rotation = tag.getFloat("rotation");
        prevBobProg = bobProg = tag.getFloat("bobProg");
    }
}
