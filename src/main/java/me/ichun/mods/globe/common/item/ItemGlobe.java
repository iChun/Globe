package me.ichun.mods.globe.common.item;

import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;

public class ItemGlobe extends Item
{
    public ItemGlobe(Properties properties)
    {
        super(properties);
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        return (stack.getDamage() == 0 ? "globe.item.globe_empty" : "globe.item.globe");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext itemContext) //mostly from BlockItem
    {
        BlockItemUseContext context = new BlockItemUseContext(itemContext);
        if (!context.canPlace())
        {
            return ActionResultType.FAIL;
        }
        else
        {
            BlockPos pos = context.getPos();
            BlockState iblockstate1 = Globe.Blocks.GLOBE_STAND.get().getStateForPlacement(context);
            BlockPos offset = pos.offset(context.getFace(), -1);
            TileEntity te = context.getWorld().getTileEntity(offset);

            ItemStack itemstack = context.getItem();
            ISelectionContext iselectioncontext = context.getPlayer() == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(context.getPlayer());
            if(te instanceof TileEntityGlobeStand)
            {
                TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
                if(gs.itemTag == null)
                {
                    gs.itemTag = itemstack.hasTag() ? itemstack.getTag() : new CompoundNBT();
                    int i = MathHelper.floor((double)(context.getPlayer().rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
                    gs.prevRotation = gs.rotation = -90 * i;
                    gs.markDirty();
                    BlockState state = context.getWorld().getBlockState(offset);
                    context.getWorld().notifyBlockUpdate(offset, state, state, 3);
                    SoundType soundtype = Globe.Blocks.GLOBE_STAND.get().getDefaultState().getSoundType(context.getWorld(), pos, context.getPlayer());
                    context.getWorld().playSound(context.getPlayer(), pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    itemstack.shrink(1);
                    return ActionResultType.SUCCESS;
                }
            }
            else if((iblockstate1.isValidPosition(context.getWorld(), context.getPos())) && context.getWorld().func_226663_a_(iblockstate1, context.getPos(), iselectioncontext))
            {
                if(placeBlockAt(itemstack, context.getPlayer(), context.getWorld(), pos, iblockstate1))
                {
                    iblockstate1 = context.getWorld().getBlockState(pos);
                    SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, context.getWorld(), pos, context.getPlayer());
                    context.getWorld().playSound(context.getPlayer(), pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    BlockState state = context.getWorld().getBlockState(pos);
                    context.getWorld().notifyBlockUpdate(pos, state, state, 3);
                    itemstack.shrink(1);
                }

                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }

    public boolean placeBlockAt(ItemStack stack, PlayerEntity player, World world, BlockPos pos, BlockState newState)
    {
        if (!world.setBlockState(pos, newState, 11)) return false;

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == Globe.Blocks.GLOBE_STAND.get())
        {
            BlockItem.setTileEntityNBT(world, player, pos, stack);
            Globe.Blocks.GLOBE_STAND.get().onBlockPlacedBy(world, pos, state, player, stack);

            if (player instanceof ServerPlayerEntity)
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity)player, pos, stack);

            TileEntity te = world.getTileEntity(pos);
            if(te instanceof TileEntityGlobeStand)
            {
                TileEntityGlobeStand gs = (TileEntityGlobeStand)te;
                gs.isStand = false;
                gs.itemTag = !stack.hasTag() ? new CompoundNBT() : stack.getTag();

                int i = MathHelper.floor((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
                gs.prevRotation = gs.rotation = -90 * i;
            }
        }

        return true;
    }
}
