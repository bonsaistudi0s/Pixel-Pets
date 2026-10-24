package com.bonsai.pixelpets.items;

import com.bonsai.pixelpets.networking.NetworkHandler;
import com.bonsai.pixelpets.networking.packets.server.ClientboundOpenPetGraphPacket;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PetGraphItem extends Item {
    public PetGraphItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            Set<ResourceLocation> caughtPets = ((PlayerPetAccess) serverPlayer).pixelPets$getCaughtPets();
            NetworkHandler.CHANNEL.sendToPlayer(
                    new ClientboundOpenPetGraphPacket(caughtPets),
                    serverPlayer
            );
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

}
