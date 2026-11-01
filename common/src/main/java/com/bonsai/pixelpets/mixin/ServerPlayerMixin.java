package com.bonsai.pixelpets.mixin;

import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    // TODO test
    @Inject(method = "restoreFrom", at = @At("HEAD"))
    private void savePixelPets(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;

        if (self instanceof PlayerPetAccess thisPetAccess && that instanceof PlayerPetAccess thatPetAccess) {
            // Caught pets
            thisPetAccess.pixelPets$getCaughtPets().clear();
            thisPetAccess.pixelPets$getCaughtPets().addAll(thatPetAccess.pixelPets$getCaughtPets());

            // Pet inventory
            thisPetAccess.pixelPets$getPetInventory().replaceWith(thatPetAccess.pixelPets$getPetInventory());
        }
    }

}
