package com.bonsai.pixelpets.items;

import com.bonsai.pixelpets.networking.packets.server.ClientboundSyncPetsPacket;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PetDebugItem extends Item {
    public PetDebugItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            CompoundTag nbt = new CompoundTag();
            ((PlayerPetAccess) sp).pixelPets$getPetInventory().save(nbt);
            ClientboundSyncPetsPacket.sendToClient(sp, nbt);
        }
        
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}