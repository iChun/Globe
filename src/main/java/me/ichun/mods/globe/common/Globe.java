package me.ichun.mods.globe.common;

import me.ichun.mods.globe.client.core.EventHandlerClient;
import me.ichun.mods.globe.common.core.EventHandlerServer;
import me.ichun.mods.globe.common.core.ProxyCommon;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Globe.MOD_ID, name = Globe.MOD_NAME,
        version = Globe.VERSION,
        dependencies = "required-after:forge@[14.23.2.2638,)",
        acceptedMinecraftVersions = "[1.12,1.13)"
)
public class Globe
{
    public static final String MOD_ID = "globe";
    public static final String MOD_NAME = "Globe";
    public static final String VERSION = "0.0.1";

    @Mod.Instance(MOD_ID)
    public static Globe instance;

    @SidedProxy(clientSide = "me.ichun.mods.globe.client.core.ProxyClient", serverSide = "me.ichun.mods.globe.common.core.ProxyCommon")
    public static ProxyCommon proxy;

    public static EventHandlerClient eventHandlerClient;
    public static EventHandlerServer eventHandlerServer;

    public static Block blockGlobeCreator;
    public static Block blockGlobeStand;
    public static Item itemGlobe;

    public static SoundEvent soundChargeup;
    public static SoundEvent soundDing;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit();
    }
}
