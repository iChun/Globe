package me.ichun.mods.globe.client.core;

import me.ichun.mods.globe.client.render.TileRendererGlobeStand;
import me.ichun.mods.globe.common.core.ProxyCommon;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInit()
    {
        super.preInit();

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGlobeStand.class, new TileRendererGlobeStand());
    }

}
