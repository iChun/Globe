package me.ichun.mods.globe.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;

public class TileEntityGlobeStand extends TileEntity implements ITickable
{
    @SideOnly(Side.CLIENT)
    public HashMap<String, TileEntity> renderingTiles = new HashMap<>();

    public NBTTagCompound itemTag;

    public TileEntityGlobeStand()
    {
    }

    @Override
    public void update()
    {

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
    }
}
