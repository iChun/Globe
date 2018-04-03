package me.ichun.mods.globe.common.block;

import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGlobeCreator extends Block implements ITileEntityProvider
{
    public BlockGlobeCreator()
    {
        super(Material.IRON);
        setRegistryName(new ResourceLocation("globe", "globe_creator"));
        setUnlocalizedName("globe.block.globeCreator");
        setCreativeTab(CreativeTabs.DECORATIONS);
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
                    world.notifyBlockUpdate(pos, state, state, 3);
                    return true;
                }
            }
            else if(gc.timeToGlobe < 0)
            {
                if(!world.isRemote)
                {
                    gc.timeToGlobe = gc.totalGlobeTime = 200;
                    gc.radius = 3;
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
                return true;
            }
        }
        return false;
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
