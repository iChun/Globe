package me.ichun.mods.globe.client.core;

import me.ichun.mods.globe.common.Globe;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerClient
{
    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Globe.blockGlobeCreator), 0, new ModelResourceLocation("globe:globe_base", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Globe.blockGlobeStand), 0, new ModelResourceLocation("globe:globe_base", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Globe.itemGlobe, 0, new ModelResourceLocation("globe:globe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Globe.itemGlobe, 1, new ModelResourceLocation("globe:globe", "inventory"));
    }
}
