package me.ichun.mods.globe.common.block;

import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Direction.Axis.X;
import static net.minecraft.util.Direction.Axis.Z;

public class BlockGlobeStand extends Block implements IWaterLoggable
{
    public static final ResourceLocation GLASS_TAG = new ResourceLocation("forge", "glass");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape GLOBE_AABB = Block.makeCuboidShape(4D, 0.0D, 4D, 12D, 8D, 12D);
    public static final VoxelShape STAND_AABB = Block.makeCuboidShape(3D, 0.0D, 3D, 13D, 2D, 13D);
    public static final VoxelShape STAND_AND_GLOBE_AABB = Block.makeCuboidShape(4D, 0.0D, 4D, 12D, 14.4D, 12D);

    public BlockGlobeStand()
    {
        super(Block.Properties.create(Material.IRON).notSolid().hardnessAndResistance(5F, 10F).sound(SoundType.METAL));
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public String getTranslationKey()
    {
        return "globe.block.globeStand";
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
        return new TileEntityGlobeStand(null, true);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntityGlobeStand)
        {
            TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
            if(!gs.isStand)
            {
                return GLOBE_AABB;
            }
            else if(gs.itemTag == null)
            {
                return STAND_AABB;
            }
            else
            {
                return STAND_AND_GLOBE_AABB;
            }
        }
        return super.getShape(state, worldIn, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntityGlobeStand)
        {
            TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
            if(!gs.isStand)
            {
                return GLOBE_AABB;
            }
            else if(gs.itemTag == null)
            {
                return STAND_AABB;
            }
            else
            {
                return STAND_AND_GLOBE_AABB;
            }
        }
        return VoxelShapes.empty();
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player)
    {
        if(player.abilities.isCreativeMode)
        {
            return;
        }
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeStand)
        {
            TileEntityGlobeStand gc = (TileEntityGlobeStand)te;
            if(gc.itemTag != null)
            {
                ItemStack is = new ItemStack(Globe.Items.GLOBE.get(), 1);
                if(!gc.itemTag.isEmpty())
                {
                    is.setTag(gc.itemTag);
                    is.setDamage(1);
                }
                if(player.inventory.addItemStackToInventory(is))
                {
                    gc.itemTag = null;
                    if(gc.isStand)
                    {
                        //                        BlockState state = world.getBlockState(pos);
                        world.notifyBlockUpdate(pos, state, state, 3);
                    }
                    else
                    {
                        world.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeStand)
        {
            TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
            if(gs.isStand)
            {
                return 7;
            }
        }
        return 0;
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
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeStand)
        {
            TileEntityGlobeStand gs = (TileEntityGlobeStand)te; //TODO snow?
            if(gs.itemTag != null)
            {
                gs.snowTime = 200;
                boolean flag = false;
                ItemStack is = playerIn.getHeldItem(hand);
                if(is.getItem() instanceof BlockItem)
                {
                    Block block = ((BlockItem)is.getItem()).getBlock();
                    if(block.getTags().contains(GLASS_TAG)) //TODO test this
                    {
                        gs.itemTag.putString("glassType", block.getRegistryName().toString());
                        world.notifyBlockUpdate(pos, state, state, 3);

                        flag = true;
                    }
                }
                if(gs.isStand) // interact!
                {
                    gs.bobAmp = (playerIn.rotationPitch < 0 ? -10F : 10F) + playerIn.rotationPitch / 10F;

                    float x = -MathHelper.sin(playerIn.rotationYaw * 0.017453292F) * MathHelper.cos(playerIn.rotationPitch * 0.017453292F);
                    float y = -MathHelper.sin((playerIn.rotationPitch) * 0.017453292F);
                    float z = MathHelper.cos(playerIn.rotationYaw * 0.017453292F) * MathHelper.cos(playerIn.rotationPitch * 0.017453292F);
                    float f = MathHelper.sqrt(x * x + y * y + z * z);
                    x = x / f * 0.2F;
                    z = z / f * 0.2F;

                    gs.rubberbandX = x;
                    gs.rubberbandZ = z;

                    if(hit.getFace().getAxis() == Z)
                    {
                        float rot = (float)(hit.getHitVec().x - 0.5D - pos.getX()) * hit.getFace().getZOffset() * 10F;
                        if(rot < 0 && gs.rotateFactor > 0 || rot > 0 && gs.rotateFactor < 0)
                        {
                            gs.rotateFactor *= 0.7F;
                        }
                        gs.rotateFactor += rot;
                    }
                    else if(hit.getFace().getAxis() == X)
                    {
                        float rot = (float)(hit.getHitVec().z - 0.5D - pos.getZ()) * -hit.getFace().getXOffset() * 10F;
                        if(rot < 0 && gs.rotateFactor > 0 || rot > 0 && gs.rotateFactor < 0)
                        {
                            gs.rotateFactor *= 0.7F;
                        }
                        gs.rotateFactor += rot;
                    }
                    if(Math.abs(gs.rotateFactor) > 30F)
                    {
                        gs.rotateFactor = gs.rotateFactor < 0 ? -30F : 30F;
                    }
                    flag = true;
                }
                return flag ? ActionResultType.SUCCESS : ActionResultType.PASS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGlobeStand)
        {
            TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
            if(gs.itemTag != null)
            {
                ItemStack is = new ItemStack(Globe.Items.GLOBE.get(), 1);
                if(!gs.itemTag.isEmpty())
                {
                    is.setTag(gs.itemTag);
                    is.setDamage(1);
                }
                return is;
            }
        }
        return getItem(world, pos, state);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return hasSolidSideOnTop(worldIn, pos.down());
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !stateIn.isValidPosition(worldIn, currentPos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            if (stateIn.get(WATERLOGGED)) {
                worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
            }

            return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
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
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        TileEntity te = builder.get(LootParameters.BLOCK_ENTITY);
        if (te instanceof TileEntityGlobeStand)
        {
            TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
            if(gs.isStand)
            {
                drops.add(new ItemStack(Globe.Blocks.GLOBE_STAND.get(), 1));
            }
            if(gs.itemTag != null)
            {
                ItemStack is = new ItemStack(Globe.Items.GLOBE.get(), 1);
                if(!gs.itemTag.isEmpty())
                {
                    is.setTag(gs.itemTag);
                    is.setDamage(1);
                }
                drops.add(is);
            }
        }
        return drops;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
