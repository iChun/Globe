package me.ichun.mods.globe.common.block;

import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockGlobeCreator extends Block implements IWaterLoggable
{
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public BlockGlobeCreator()
    {
        super(Block.Properties.create(Material.IRON).notSolid().hardnessAndResistance(5F, 10F).sound(SoundType.METAL));
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public String getTranslationKey()
    {
        return "globe.block.globeCreator";
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TileEntityGlobeCreator();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeCreator)
        {
            TileEntityGlobeCreator gc = (TileEntityGlobeCreator)te;
            if(!gc.hasGlobe)
            {
                ItemStack is = playerIn.getHeldItem(hand);
                if(is.getItem() == Globe.Items.GLOBE.get() && is.getDamage() == 0)
                {
                    gc.hasGlobe = true;
                    if(!playerIn.abilities.isCreativeMode)
                    {
                        playerIn.setHeldItem(hand, ItemStack.EMPTY);
                    }
                    SoundType soundtype = state.getSoundType(world, pos, playerIn);
                    world.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    world.notifyBlockUpdate(pos, state, state, 3);
                    return ActionResultType.SUCCESS;
                }
            }
            else if(gc.timeToGlobe < 0 && !gc.globed)
            {
                if(!world.isRemote)
                {
                    gc.timeToGlobe = gc.totalGlobeTime = TileEntityGlobeCreator.GLOBE_TIME;
                    gc.radius = 3;
                    world.notifyBlockUpdate(pos, state, state, 3);
                    world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Globe.Sounds.CHARGEUP.get(), SoundCategory.BLOCKS, 0.3F, 1F);
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public float getBlockHardness(BlockState blockState, IBlockReader world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeCreator)
        {
            TileEntityGlobeCreator gc = (TileEntityGlobeCreator)te;
            if(gc.globed || gc.timeToGlobe >= 0)
            {
                return -1F; // you can't break it if the globing process has started
            }
        }
        return this.blockHardness;
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player)
    {
        if(player.abilities.isCreativeMode)
        {
            return;
        }
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeCreator)
        {
            TileEntityGlobeCreator gc = (TileEntityGlobeCreator)te;
            if(!gc.globed && gc.timeToGlobe < 0 && gc.hasGlobe && player.inventory.addItemStackToInventory(new ItemStack(Globe.Items.GLOBE.get(), 1)))
            {
                gc.hasGlobe = false;
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
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
        return state.getLightValue();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        TileEntity te = builder.get(LootParameters.BLOCK_ENTITY);
        if (te instanceof TileEntityGlobeCreator)
        {
            TileEntityGlobeCreator gs = (TileEntityGlobeCreator)te;
            if(gs.hasGlobe)
            {
                drops.add(new ItemStack(Globe.Items.GLOBE.get()));
            }
        }

        return drops;
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid)
    {
        this.onBlockHarvested(world, pos, state, player);
        boolean flag = world.setBlockState(pos, fluid.getBlockState(), world.isRemote ? 11 : 3);
        world.getChunkProvider().getLightManager().checkBlock(pos); //hacky light fix
        return flag;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }

        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockState blockstate1 = this.getDefaultState();
        IWorldReader iworldreader = context.getWorld();
        BlockPos blockpos = context.getPos();
        IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());

        if (blockstate1.isValidPosition(iworldreader, blockpos)) {
            return blockstate1.with(WATERLOGGED, ifluidstate.getFluid() == Fluids.WATER);
        }

        return null;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }


    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
