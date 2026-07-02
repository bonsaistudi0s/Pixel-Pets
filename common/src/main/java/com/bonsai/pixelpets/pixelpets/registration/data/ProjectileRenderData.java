package com.bonsai.pixelpets.pixelpets.registration.data;

import com.bonsai.pixelpets.PixelPets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.function.Supplier;

public sealed interface ProjectileRenderData {

    // TODO could also encode particle trails
    float scale();
    boolean fullBright();

    Codec<ProjectileRenderData> CODEC = Codec.STRING.dispatch(
            "type",
            data -> switch (data) {
                case Modeled m -> "model";
                case ItemBased i -> "item";
            },
            type -> switch (type) {
                case "model" -> Modeled.MAP_CODEC;
                case "item" -> ItemBased.MAP_CODEC;
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }
    );

    record Modeled(ResourceLocation model, ResourceLocation texture, ResourceLocation animation, float scale, boolean fullBright) implements ProjectileRenderData {

        public static final ResourceLocation DEFAULT_ID = PixelPets.identifier("empty_pet_projectile");

        static final MapCodec<Modeled> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("model", DEFAULT_ID).forGetter(Modeled::model),
                ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(m -> Optional.of(m.texture())),
                ResourceLocation.CODEC.optionalFieldOf("animation", DEFAULT_ID).forGetter(Modeled::animation),
                Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(Modeled::scale),
                Codec.BOOL.optionalFieldOf("full_bright", false).forGetter(Modeled::fullBright)

                ).apply(instance, (model, texture, animation, scale, fullBright) ->
                new Modeled(model, texture.orElse(model), animation, scale, fullBright)
        ));
    }

    record ItemBased(Supplier<Item> item, float scale, boolean fullBright) implements ProjectileRenderData {

        static final MapCodec<ItemBased> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item", Items.SNOWBALL).forGetter(data -> data.item().get()),
                Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(ItemBased::scale),
                Codec.BOOL.optionalFieldOf("full_bright", false).forGetter(ItemBased::fullBright)

        ).apply(instance, (item, scale, fullBright) -> new ItemBased(() -> item, scale, fullBright)));
    }
}
