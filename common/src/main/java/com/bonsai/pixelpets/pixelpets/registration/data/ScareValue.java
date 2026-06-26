package com.bonsai.pixelpets.pixelpets.registration.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;

public sealed interface ScareValue {

    Codec<ScareValue> CODEC = ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
            tagOrElement -> tagOrElement.tag()
                    ? new Tag(TagKey.create(Registries.ENTITY_TYPE, tagOrElement.id()))
                    : new Element(tagOrElement.id()),
            v -> v instanceof Tag t
                    ? new ExtraCodecs.TagOrElementLocation(t.tag().location(), true)
                    : new ExtraCodecs.TagOrElementLocation(((Element) v).id(), false)
    );

    record Element(ResourceLocation id) implements ScareValue {}

    record Tag(TagKey<EntityType<?>> tag) implements ScareValue { }
}
