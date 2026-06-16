package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.PixelPetData;
import com.bonsai.pixelpets.pixelpets.PixelPetDataRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ModCommands {

    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("pixelPetsSpawn")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("type", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> {
                            PixelPetDataRegistry.INSTANCE.getAll().forEach(d ->
                                    builder.suggest(d.id().toString())
                            );
                            return builder.buildFuture();
                        })
                        .executes((context) -> {
                            CommandSourceStack source = context.getSource();

                            if (!source.isPlayer()) {
                                source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            ResourceLocation id = ResourceLocationArgument.getId(context, "type");
                            PixelPetData data = PixelPetDataRegistry.INSTANCE.getById(id)
                                    .orElseThrow(() -> new SimpleCommandExceptionType(
                                            Component.literal("Unknown pet species: " + id)).create());

                            ServerPlayer player = context.getSource().getPlayerOrException();
                            AbstractPixelPetEntity entity = data.entityType().create(player.level());
                            entity.setDataLocation(id);
                            entity.moveTo(player.position());
                            player.level().addFreshEntity(entity);

                            source.sendSystemMessage(Component.literal("Spawned " + data.genericName() + " pixel pet "));

                            return Command.SINGLE_SUCCESS;
                        })));

    }


}
