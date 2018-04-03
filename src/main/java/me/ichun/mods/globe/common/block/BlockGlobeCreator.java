package me.ichun.mods.globe.common.block;

import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGlobeCreator extends Block implements ITileEntityProvider
{
    public BlockGlobeCreator()
    {
        super(Material.IRON);
        setRegistryName(new ResourceLocation("globe", "globe_creator"));
        setUnlocalizedName("globe.block.globeCreator");
        setCreativeTab(CreativeTabs.DECORATIONS); //TODO emit light
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityGlobeCreator();
    }


    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeCreator)
        {
            TileEntityGlobeCreator gc = (TileEntityGlobeCreator)te;
            if(!gc.hasGlobe)
            {
                ItemStack is = playerIn.getHeldItem(hand);
                if(is.getItem() == Globe.itemGlobe && is.getItemDamage() == 0)
                {
                    gc.hasGlobe = true;
                    if(!playerIn.capabilities.isCreativeMode)
                    {
                        playerIn.setHeldItem(hand, ItemStack.EMPTY);
                    }
                    SoundType soundtype = this.getSoundType();
                    world.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    world.notifyBlockUpdate(pos, state, state, 3);
                    return true;
                }
            }
            else if(gc.timeToGlobe < 0)
            {
                if(!world.isRemote)
                {
                    gc.timeToGlobe = gc.totalGlobeTime = TileEntityGlobeCreator.GLOBE_TIME;
                    gc.radius = 3;
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeCreator)
        {
            TileEntityGlobeCreator gc = (TileEntityGlobeCreator)te;
            if(gc.timeToGlobe >= 12)
            {
                return (int)(MathHelper.clamp(1.0F - (gc.timeToGlobe - 10) / (gc.totalGlobeTime - 10F), 0F, 1F) * 15F * MathHelper.clamp((gc.radius * 2 + 4) / 15F, 0F, 1F));
            }
        }
        return 0;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
