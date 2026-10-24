package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.PixelPetStatus;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.bonsai.pixelpets.PixelPets.PET_DATA;

public class ModCommands {

    private static CompletableFuture<Suggestions> suggestPets(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
        ctx.getSource().getServer().registryAccess().registry(PET_DATA)
                .ifPresentOrElse((registry -> registry.keySet().forEach(loc -> builder.suggest(loc.toString()))),
                        () -> PixelPets.LOGGER.info("PET_DATA registry not present"));
        return builder.buildFuture();
    }

    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {

        // Listing/Adding/Removing pets from players caught pets set
        // TODO consider clear/clear all?
        dispatcher.register(Commands.literal("pixelPetsCaught")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            Set<ResourceLocation> caughtPets = ((PlayerPetAccess) player).pixelPets$getCaughtPets();

                            MutableComponent output = Component.literal("\n")
                                    .append(player.getName())
                                    .append("'s caught PixelPets:");
                            for (ResourceLocation id : caughtPets) {
                                output.append(Component.literal("\n - " + id).withStyle(ChatFormatting.YELLOW));
                            }

                            source.sendSuccess(() -> output, false);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("operation", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("add", "remove"), builder))
                                .then(Commands.argument("pet", ResourceLocationArgument.id())
                                        .suggests(ModCommands::suggestPets)
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            ResourceLocation id = ResourceLocationArgument.getId(context, "pet");
                                            String operation = StringArgumentType.getString(context, "operation");

                                            var reg = player.level().registryAccess().registryOrThrow(PET_DATA);
                                            if (!reg.containsKey(id)) {
                                                source.sendSystemMessage(Component.literal("WARNING: " + id + " isn't a recognized pet").withStyle(ChatFormatting.YELLOW));
                                            }

                                            PlayerPetAccess access = (PlayerPetAccess) player;

                                            if (operation.equals("add")) {
                                                if (access.pixelPets$addCaughtPet(id)) {
                                                    source.sendSystemMessage(Component.literal("Added " + id + " to ")
                                                            .append(player.getName()).append("'s caught PixelPets"));
                                                } else {
                                                    source.sendSystemMessage(Component.literal(id + " already present in ")
                                                            .append(player.getName()).append("'s caught PixelPets"));
                                                }
                                            } else if (operation.equals("remove")) {
                                                if (access.pixelPets$removeCaughtPet(id)) {
                                                    source.sendSystemMessage(Component.literal("Removed " + id + " from ")
                                                            .append(player.getName()).append("'s caught PixelPets"));
                                                } else {
                                                    source.sendSystemMessage(Component.literal(id + " wasn't present in ")
                                                            .append(player.getName()).append("'s caught PixelPets"));
                                                }
                                            } else {
                                                source.sendFailure(Component.literal("Unknown operation").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
        );

        dispatcher.register(Commands.literal("pixelPetsSpawn")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("type", ResourceLocationArgument.id())
                        .suggests(ModCommands::suggestPets)
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
                                            entity.tame(player);
                                            player.level().addFreshEntity(entity);

                                            source.sendSystemMessage(Component.literal("Spawned " + data.genericName() + " pixel pet"));

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
        );

    }


}
