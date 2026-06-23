package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.teamresourceful.resourcefullib.common.item.tabs.ResourcefulCreativeModeTab;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;

import java.util.function.Supplier;

public class ModItems {

    /// ITEMS

    public static final ResourcefulRegistry<Item> ITEMS = ResourcefulRegistries.create(BuiltInRegistries.ITEM, PixelPets.MOD_ID);

    // example item:
    // public static final Supplier<SpawnEggItem> JUNGLE_SPAWN_EGG = ITEMS.register("jungle_creeper_spawn_egg", () -> createSpawnEgg(
    //            ModEntities.JUNGLE_CREEPER, 0x507541, 0x59461A, new Item.Properties()));

    // example block item:
    // public static final Supplier<Item> TINY_CACTUS =  ITEMS.register("tiny_cactus", () -> new BlockItem(ModBlocks.TINY_CACTUS.get(), new Item.Properties()));




    /// CREATIVE TABS

    public static final ResourcefulRegistry<CreativeModeTab> CREATIVE_TABS = ResourcefulRegistries.create(BuiltInRegistries.CREATIVE_MODE_TAB, PixelPets.MOD_ID);

    public static final Supplier<CreativeModeTab> PIXEL_PETS = CREATIVE_TABS.register("item_group", () ->
            new ResourcefulCreativeModeTab(PixelPets.identifier("item_group"))
                    .setItemIcon(() -> Items.NAME_TAG) // TODO choose better item
                    .addRegistry(ModItems.ITEMS)
                    .build());

    ///  RECIPES
    // add later if relevant
}
