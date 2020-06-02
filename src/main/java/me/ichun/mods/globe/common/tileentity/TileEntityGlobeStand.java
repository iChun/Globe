package me.ichun.mods.globe.common.tileentity;

import me.ichun.mods.globe.common.Globe;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.LightType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;

public class TileEntityGlobeStand extends TileEntity implements ITickableTileEntity
{
    public HashMap<String, TileEntity> renderingTiles = new HashMap<>();
    public HashSet<Entity> renderingEnts = new HashSet<>();

    public CompoundNBT itemTag;

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
        super(Globe.TileEntityTypes.GLOBE_STAND.get());
        disX = disZ = 0F;
    }

    public TileEntityGlobeStand(CompoundNBT itemTag, boolean isStand)
    {
        this();
        this.itemTag = itemTag;
        this.isStand = isStand;
        snowTime = 0;
    }

    @Override
    public void tick()
    {
        ticks++;
        if(!isStand)
        {
            rotateFactor = bobProg = bobAmp = 0F;
            if(world.getLightFor(LightType.BLOCK, pos) > 0)
            {
                world.getChunkProvider().getLightManager().checkBlock(pos);
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
            world.getChunkProvider().getLightManager().checkBlock(pos);
        }
    }

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
        if(itemTag != null)
        {
            tag.put("itemTag", itemTag);
        }
        tag.putBoolean("isStand", isStand);
        tag.putFloat("rotateFactor", rotateFactor);
        tag.putFloat("rotation", rotation);
        tag.putFloat("bobProg", bobProg);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag)
    {
        super.read(tag);
        if(tag.contains("itemTag"))
        {
            itemTag = tag.getCompound("itemTag");
        }
        isStand = tag.getBoolean("isStand");
        rotateFactor = tag.getFloat("rotateFactor");
        prevRotation = rotation = tag.getFloat("rotation");
        prevBobProg = bobProg = tag.getFloat("bobProg");
    }
}
