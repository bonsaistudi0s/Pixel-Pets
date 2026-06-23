package com.bonsai.pixelpets.mixin;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TamableAnimal.class)
public class TamableAnimalMixin {

    // Stops death messages for pixel pets
    @Redirect(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void suppressDeathMessage(LivingEntity instance, Component component) {
        if ((Object) this instanceof AbstractPixelPetEntity) return;
        instance.sendSystemMessage(component);
    }

}
