package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.PixelPetStatus;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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

import static com.bonsai.pixelpets.PixelPets.PET_DATA;

public class ModCommands {

    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("pixelPetsSpawn")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("type", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> {
                            ctx.getSource().getServer().registryAccess().registry(PET_DATA)
                                    .ifPresentOrElse((
                                            registry -> registry.keySet().forEach(loc -> builder.suggest(loc.toString()))),
                                            () -> PixelPets.LOGGER.info("PET_DATA registry not present"));
                            return builder.buildFuture();
                        })
                        .executes((context) -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            var reg = player.level().registryAccess().registryOrThrow(PET_DATA);
                            ResourceLocation id = ResourceLocationArgument.getId(context, "type");
                            PixelPetData data = reg.getOptional(id)
                                    .orElseThrow(() -> new SimpleCommandExceptionType(
                                            Component.literal("Unknown pet species: " + id)).create());

                            AbstractPixelPetEntity entity = data.entityType().create(player.level());
                            if (entity == null) {
                                source.sendFailure(Component.literal("Failed to create pixel pet entity").withStyle(ChatFormatting.RED));
                                return 0;
                            }
                            entity.initialize(id, 1, PixelPetStatus.PASSIVE);
                            entity.moveTo(player.position());
                            player.level().addFreshEntity(entity);

                            source.sendSystemMessage(Component.literal("Spawned " + data.genericName() + " pixel pet "));

                            return Command.SINGLE_SUCCESS;
                        }).then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                .executes((context) -> {
                                    CommandSourceStack source = context.getSource();
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    var reg = player.level().registryAccess().registryOrThrow(PET_DATA);
                                    ResourceLocation id = ResourceLocationArgument.getId(context, "type");
                                    PixelPetData data = reg.getOptional(id)
                                            .orElseThrow(() -> new SimpleCommandExceptionType(
                                                    Component.literal("Unknown pet species: " + id)).create());

                                    int level = IntegerArgumentType.getInteger(context, "level");
                                    level = Math.clamp(level, 1, 5);


                                    AbstractPixelPetEntity entity = data.entityType().create(player.level());
                                    if (entity == null) {
                                        source.sendFailure(Component.literal("Failed to create pixel pet entity").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    entity.initialize(id, level, PixelPetStatus.PASSIVE);
                                    entity.moveTo(player.position());
                                    player.level().addFreshEntity(entity);

                                    source.sendSystemMessage(Component.literal("Spawned " + data.genericName() + " pixel pet"));

                                    return Command.SINGLE_SUCCESS;
                                }).then(Commands.argument("status", StringArgumentType.word())
                                        .suggests(((commandContext, suggestionsBuilder) -> {
                                            suggestionsBuilder.suggest("passive");
                                            suggestionsBuilder.suggest("active");
                                            return suggestionsBuilder.buildFuture();
                                        }))
                                        .executes((context) -> {
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer player = context.getSource().getPlayerOrException();

                                            var reg = player.level().registryAccess().registryOrThrow(PET_DATA);
                                            ResourceLocation id = ResourceLocationArgument.getId(context, "type");
                                            PixelPetData data = reg.getOptional(id)
                                                    .orElseThrow(() -> new SimpleCommandExceptionType(
                                                            Component.literal("Unknown pet species: " + id)).create());

                                            int level = IntegerArgumentType.getInteger(context, "level");
                                            level = Math.clamp(level, 1, 5);

                                            PixelPetStatus status = PixelPetStatus.byName(StringArgumentType.getString(context, "status"));

                                            AbstractPixelPetEntity entity = data.entityType().create(player.level());
                                            if (entity == null) {
                                                source.sendFailure(Component.literal("Failed to create pixel pet entity").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            entity.initialize(id, level, status);
                                            entity.moveTo(player.position());
                                            player.level().addFreshEntity(entity);

                                            source.sendSystemMessage(Component.literal("Spawned " + data.genericName() + " pixel pet"));

                                            return Command.SINGLE_SUCCESS;
                                        })))
                ));

    }


}
