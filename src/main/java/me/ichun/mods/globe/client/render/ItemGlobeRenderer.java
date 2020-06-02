package me.ichun.mods.globe.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.globe.common.Globe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.HashSet;

public class ItemGlobeRenderer extends ItemStackTileEntityRenderer
{
    public static final ItemGlobeRenderer INSTANCE = new ItemGlobeRenderer();

    public static TileRendererGlobeCreator RENDERER_GLOBE_CREATOR;
    public static TileRendererGlobeStand RENDERER_GLOBE_STAND;

    private ItemGlobeRenderer(){};

    @Override
    public void render(ItemStack is, MatrixStack stack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        float partialTicks = 0F;
        if(is.getItem() instanceof BlockItem)
        {
            if(((BlockItem)is.getItem()).getBlock() == Globe.Blocks.GLOBE_CREATOR.get())
            {
                RENDERER_GLOBE_CREATOR.render(null, partialTicks, stack, bufferIn, combinedLightIn, combinedOverlayIn);
            }
            else
            {
                stack.push();
                stack.translate(0.0F, 0.35F, 0.0F);
                RENDERER_GLOBE_STAND.render(null, partialTicks, stack, bufferIn, combinedLightIn, combinedOverlayIn);
                stack.pop();
            }
        }
        else if(is.getItem() == Globe.Items.GLOBE.get())
        {
            stack.push();
            stack.translate(0.5F, 0.5F, 0.5F);
            stack.scale(1.5F, 1.5F, 1.5F);
            ItemRenderContainer container = null;
            if(is.hasTag())
            {
                container = Globe.eventHandlerClient.itemRenderContainers.computeIfAbsent(is, k -> new ItemRenderContainer());
                container.lastRenderInTicks = 0;
            }
            TileRendererGlobeStand.drawGlobe(stack,
                    bufferIn,
                    combinedLightIn,
                    combinedOverlayIn,
                    Minecraft.getInstance().world,
                    true,
                    true,
                    Minecraft.getInstance().player != null && (Minecraft.getInstance().player.getHeldItemMainhand() == is || Minecraft.getInstance().player.getHeldItemOffhand() == is) && is.hasTag(),
                    is.getTag(),
                    is.hasTag() ? container.tileEntityMap : null,
                    is.hasTag() ? container.entities : null,
                    TileRendererGlobeStand.HEAVENS_ABOVE,
                    0,
                    0,
                    Minecraft.getInstance().getRenderViewEntity() != null ? -(Minecraft.getInstance().getRenderViewEntity().prevRotationYaw + (Minecraft.getInstance().getRenderViewEntity().rotationYaw - Minecraft.getInstance().getRenderViewEntity().prevRotationYaw) * partialTicks) + 180F : 0F,
                    partialTicks);
            stack.pop();
        }
    }

    public static class ItemRenderContainer
    {
        public int lastRenderInTicks = 0;
        public HashMap<String, TileEntity> tileEntityMap = new HashMap<>();
        public HashSet<Entity> entities = new HashSet<>();
    }
}
