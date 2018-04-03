package me.ichun.mods.globe.common.item;

import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemGlobe extends Item
{
    public ItemGlobe()
    {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
        setRegistryName(new ResourceLocation("globe", "globe"));
        setUnlocalizedName("globe.item.globe");
        setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return "item." +(stack.getItemDamage() == 0 ? "globe.item.globe_empty" : "globe.item.globe");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(facing);
        }

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack))
        {
            IBlockState iblockstate1 = Globe.blockGlobeStand.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, player, hand);
            BlockPos offset = pos.offset(facing, -1);
            TileEntity te = worldIn.getTileEntity(offset);
            if(te instanceof TileEntityGlobeStand)
            {
                TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
                if(gs.itemTag == null)
                {
                    gs.itemTag = itemstack.hasTagCompound() ? itemstack.getTagCompound() : new NBTTagCompound();
                    gs.markDirty();
                    IBlockState state = worldIn.getBlockState(offset);
                    worldIn.notifyBlockUpdate(offset, state, state, 3);
                    SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                    worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    itemstack.shrink(1);
                    return EnumActionResult.SUCCESS;
                }
            }
            else if(worldIn.mayPlace(Globe.blockGlobeStand, pos, false, facing, (Entity)null))
            {
                if(placeBlockAt(itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1))
                {
                    iblockstate1 = worldIn.getBlockState(pos);
                    SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                    worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    itemstack.shrink(1);
                }

                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }

    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        if (!world.setBlockState(pos, newState, 11)) return false;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == Globe.blockGlobeStand)
        {
            ItemBlock.setTileEntityNBT(world, player, pos, stack);
            Globe.blockGlobeStand.onBlockPlacedBy(world, pos, state, player, stack);

            if (player instanceof EntityPlayerMP)
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);

            if(!world.isRemote)
            {
                TileEntity te = world.getTileEntity(pos);
                if(te instanceof TileEntityGlobeStand)
                {
                    TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
                    gs.isStand = false;
                    gs.itemTag = !stack.hasTagCompound() ? new NBTTagCompound() : stack.getTagCompound();

                    int i = MathHelper.floor((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
                    gs.prevRotation = gs.rotation = -90 * i;
                }
            }
        }

        return true;
    }
}
