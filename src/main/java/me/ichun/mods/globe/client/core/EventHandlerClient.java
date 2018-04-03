package me.ichun.mods.globe.client.core;

import me.ichun.mods.globe.client.render.ItemGlobeRenderer;
import me.ichun.mods.globe.common.Globe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class EventHandlerClient
{
    public HashMap<ItemStack, ItemGlobeRenderer.ItemRenderContainer> itemRenderContainers = new HashMap<>();
    public HashMap<UUID, NetworkPlayerInfo> networkPlayerInfos = new HashMap<>();

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Globe.blockGlobeCreator), 0, new ModelResourceLocation("globe:globe_base", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Globe.blockGlobeStand), 0, new ModelResourceLocation("globe:globe_base", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Globe.itemGlobe, 0, new ModelResourceLocation("globe:globe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Globe.itemGlobe, 1, new ModelResourceLocation("globe:globe", "inventory"));
    }

    @SubscribeEvent
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Pre event)
    {
        if(event.getType() == RenderGameOverlayEvent.ElementType.PLAYER_LIST && Minecraft.getMinecraft().getConnection() != null)
        {
            networkPlayerInfos.clear();
            Iterator<Map.Entry<UUID, NetworkPlayerInfo>> ite = EventHandlerClient.getMcPlayerInfoMap().entrySet().iterator();
            while(ite.hasNext())
            {
                Map.Entry<UUID, NetworkPlayerInfo> e = ite.next();
                if(e.getValue().getResponseTime() <= -100)
                {
                    networkPlayerInfos.put(e.getKey(), e.getValue());
                    ite.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event)
    {
        if(event.getType() == RenderGameOverlayEvent.ElementType.PLAYER_LIST && Minecraft.getMinecraft().getConnection() != null)
        {
            EventHandlerClient.getMcPlayerInfoMap().putAll(networkPlayerInfos);
            networkPlayerInfos.clear();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Iterator<Map.Entry<ItemStack, ItemGlobeRenderer.ItemRenderContainer>> ite = itemRenderContainers.entrySet().iterator();
            while(ite.hasNext())
            {
                Map.Entry<ItemStack, ItemGlobeRenderer.ItemRenderContainer> e = ite.next();
                e.getValue().lastRenderInTicks++;
                if(e.getValue().lastRenderInTicks > 100) //5 second timeout
                {
                    ite.remove();
                }
            }
        }
    }

    public static Map<UUID, NetworkPlayerInfo> getMcPlayerInfoMap()
    {
        return (Map<UUID, NetworkPlayerInfo>)ObfuscationReflectionHelper.getPrivateValue(NetHandlerPlayClient.class, Minecraft.getMinecraft().getConnection(), "field_147310_i", "playerInfoMap");
    }
}
