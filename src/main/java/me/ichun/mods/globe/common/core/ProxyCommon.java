package me.ichun.mods.globe.common.core;

import me.ichun.mods.globe.common.Globe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon
{
    public void preInit()
    {
        Globe.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Globe.eventHandlerServer);

        GameRegistry.registerTileEntity(TileEntityGlobeCreator.class, "globe_globeCreator");
        GameRegistry.registerTileEntity(TileEntityGlobeStand.class, "globe_globeStand");
    }

    public void applyItemRenderers(){}
}
