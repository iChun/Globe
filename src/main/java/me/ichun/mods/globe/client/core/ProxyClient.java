package me.ichun.mods.globe.client.core;

import me.ichun.mods.globe.client.render.ItemGlobeRenderer;
import me.ichun.mods.globe.client.render.TileRendererGlobeCreator;
import me.ichun.mods.globe.client.render.TileRendererGlobeStand;
import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.core.ProxyCommon;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInit()
    {
        super.preInit();

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGlobeCreator.class, ItemGlobeRenderer.RENDERER_GLOBE_CREATOR);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGlobeStand.class,ItemGlobeRenderer.RENDERER_GLOBE_STAND);

        Globe.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(Globe.eventHandlerClient);
    }

    @Override
    public void applyItemRenderers()
    {
        ItemGlobeRenderer gr = new ItemGlobeRenderer();
        Item.getItemFromBlock(Globe.blockGlobeCreator).setTileEntityItemStackRenderer(gr);
        Item.getItemFromBlock(Globe.blockGlobeStand).setTileEntityItemStackRenderer(gr);
        Globe.itemGlobe.setTileEntityItemStackRenderer(gr);
    }
}
