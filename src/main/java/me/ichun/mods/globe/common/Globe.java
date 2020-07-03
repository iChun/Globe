package me.ichun.mods.globe.common;

import me.ichun.mods.globe.client.core.EventHandlerClient;
import me.ichun.mods.globe.client.render.ItemGlobeRenderer;
import me.ichun.mods.globe.client.render.TileRendererGlobeCreator;
import me.ichun.mods.globe.client.render.TileRendererGlobeStand;
import me.ichun.mods.globe.common.block.BlockGlobeCreator;
import me.ichun.mods.globe.common.block.BlockGlobeStand;
import me.ichun.mods.globe.common.item.ItemGlobe;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeCreator;
import me.ichun.mods.globe.common.tileentity.TileEntityGlobeStand;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(Globe.MOD_ID)
public class Globe
{
    public static final String MOD_ID = "globe";
    public static final String MOD_NAME = "Globe";
    public static final String VERSION = "0.0.1";

    public static EventHandlerClient eventHandlerClient;

    public Globe()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Blocks.REGISTRY.register(bus);
        Items.REGISTRY.register(bus);
        Sounds.REGISTRY.register(bus);
        TileEntityTypes.REGISTRY.register(bus);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(this::onClientSetup);

            MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());
        });
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(TileEntityTypes.GLOBE_CREATOR.get(), TileRendererGlobeCreator::new);
        ClientRegistry.bindTileEntityRenderer(TileEntityTypes.GLOBE_STAND.get(), TileRendererGlobeStand::new);
    }

    public static class Blocks
    {
        private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

        public static final RegistryObject<BlockGlobeCreator> GLOBE_CREATOR = REGISTRY.register("globe_creator", BlockGlobeCreator::new);
        public static final RegistryObject<BlockGlobeStand> GLOBE_STAND = REGISTRY.register("globe_stand", BlockGlobeStand::new);
    }

    public static class Items
    {
        private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

        public static final RegistryObject<ItemGlobe> GLOBE = REGISTRY.register("globe", () -> new ItemGlobe(DistExecutor.runForDist(() -> () -> attachISTER(new Item.Properties().maxDamage(0).group(ItemGroup.DECORATIONS)), () -> () -> new Item.Properties().maxDamage(0).group(ItemGroup.DECORATIONS))));

        public static final RegistryObject<BlockItem> GLOBE_CREATOR = REGISTRY.register("globe_creator", () -> new BlockItem(Blocks.GLOBE_CREATOR.get(), DistExecutor.runForDist(() -> () -> attachISTER((new Item.Properties()).group(ItemGroup.DECORATIONS)), () -> () -> (new Item.Properties()).group(ItemGroup.DECORATIONS))));
        public static final RegistryObject<BlockItem> GLOBE_STAND = REGISTRY.register("globe_stand", () -> new BlockItem(Blocks.GLOBE_STAND.get(), DistExecutor.runForDist(() -> () -> attachISTER((new Item.Properties()).group(ItemGroup.DECORATIONS)), () -> () -> (new Item.Properties()).group(ItemGroup.DECORATIONS))));
    }

    private static Item.Properties attachISTER(Item.Properties properties)
    {
        properties.setISTER(() -> () -> ItemGlobeRenderer.INSTANCE);
        return properties;
    }

    public static class Sounds
    {
        private static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID); //.setRegistryName(new ResourceLocation("torched", "rpt") ??

        public static final RegistryObject<SoundEvent> CHARGEUP = REGISTRY.register("chargeup", () -> new SoundEvent(new ResourceLocation("globe", "chargeup")));
        public static final RegistryObject<SoundEvent> DING = REGISTRY.register("ding", () -> new SoundEvent(new ResourceLocation("globe", "ding")));
    }

    public static class TileEntityTypes
    {
        private static final DeferredRegister<TileEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

        public static final RegistryObject<TileEntityType<TileEntityGlobeCreator>> GLOBE_CREATOR = REGISTRY.register("globe_creator", () -> TileEntityType.Builder.create(TileEntityGlobeCreator::new, Blocks.GLOBE_CREATOR.get()).build(null));
        public static final RegistryObject<TileEntityType<TileEntityGlobeStand>> GLOBE_STAND = REGISTRY.register("globe_stand", () -> TileEntityType.Builder.create(TileEntityGlobeStand::new, Blocks.GLOBE_STAND.get()).build(null));
    }
}
