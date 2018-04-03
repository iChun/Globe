package me.ichun.mods.globe.client.render;

import me.ichun.mods.globe.common.Globe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;

public class ItemGlobeRenderer extends TileEntityItemStackRenderer
{
    public static final TileRendererGlobeCreator RENDERER_GLOBE_CREATOR = new TileRendererGlobeCreator();
    public static final TileRendererGlobeStand RENDERER_GLOBE_STAND = new TileRendererGlobeStand();

    @Override
    public void renderByItem(ItemStack is, float partialTicks)
    {
        if(is.getItem() instanceof ItemBlock)
        {
            if(((ItemBlock)is.getItem()).getBlock() == Globe.blockGlobeCreator)
            {
                RENDERER_GLOBE_CREATOR.render(null, 0, 0, 0, 0, -1, 0.0F);
            }
            else
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 0.35F, 0.0F);
                RENDERER_GLOBE_STAND.render(null, 0, 0, 0, 0, -1, 0.0F);
                GlStateManager.popMatrix();
            }
        }
        else if(is.getItem() == Globe.itemGlobe)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            GlStateManager.scale(1.5F, 1.5F, 1.5F);
            TileRendererGlobeStand.drawGlobe(Minecraft.getMinecraft().world, true, true, Minecraft.getMinecraft().player != null && (Minecraft.getMinecraft().player.getHeldItemMainhand() == is || Minecraft.getMinecraft().player.getHeldItemOffhand() == is) && is.hasTagCompound(), is.getTagCompound(), is.hasTagCompound() ? new HashMap<>() : null, is.hasTagCompound() ? new HashSet<>() : null, BlockPos.ORIGIN, Minecraft.getMinecraft().getRenderViewEntity() != null ? -(Minecraft.getMinecraft().getRenderViewEntity().prevRotationYaw + (Minecraft.getMinecraft().getRenderViewEntity().rotationYaw - Minecraft.getMinecraft().getRenderViewEntity().prevRotationYaw) * partialTicks) + 180F : 0F, partialTicks); //TODO OPTIMIZE THIS.
            GlStateManager.popMatrix();
        }
    }
}
