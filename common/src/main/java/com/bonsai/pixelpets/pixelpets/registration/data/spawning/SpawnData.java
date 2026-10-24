package com.bonsai.pixelpets.pixelpets.registration.data.spawning;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;

// TODO when making spawn logic, consider preventing spawns for pets in the players inv
// TODO trading a pet shouldn't count as catching it
public record SpawnData(Optional<HolderSet<DimensionType>> dimensions,
                        Optional<HolderSet<Biome>> biomes,
                        Optional<HolderSet<Structure>> structures,
                        Optional<WeatherSpawnCheck> weather,
                        Optional<GenericIntRange> time,
                        Optional<GenericIntRange> moonPhase,
                        Optional<GenericIntRange> lightLevel,
                        Optional<GenericIntRange> height,
                        PetDependencyData prerequisitePets) {

    public SpawnData() {
        this(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(), PetDependencyData.empty());
    }

    public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryCodecs.homogeneousList(Registries.DIMENSION_TYPE).optionalFieldOf("dimensions").forGetter(SpawnData::dimensions),
                    RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(SpawnData::biomes),
                    RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(SpawnData::structures),
                    WeatherSpawnCheck.CODEC.codec().optionalFieldOf("weather").forGetter(SpawnData::weather),
                    GenericIntRange.CODEC.optionalFieldOf("time").forGetter(SpawnData::time),
                    GenericIntRange.CODEC.optionalFieldOf("moon_phase").forGetter(SpawnData::moonPhase),
                    GenericIntRange.CODEC.optionalFieldOf("light_level").forGetter(SpawnData::lightLevel),
                    GenericIntRange.CODEC.optionalFieldOf("height").forGetter(SpawnData::lightLevel),
                    PetDependencyData.CODEC.optionalFieldOf("prerequisite_pets", PetDependencyData.empty()).forGetter(SpawnData::prerequisitePets)
            ).apply(instance, SpawnData::new));

    public boolean testDimension(ServerLevel level) {
        return dimensions.isEmpty() || dimensions.get().contains(level.dimensionTypeRegistration());
    }

    public boolean testBiome(ServerLevel level, BlockPos pos) {
        return biomes.isEmpty() || biomes.get().contains(level.getBiome(pos));
    }

    public boolean testStructure(ServerLevel level, BlockPos pos) {
        StructureManager manager = level.structureManager();
        return structures.isEmpty() || structures.get().stream()
                .anyMatch(holder -> manager.getStructureWithPieceAt(pos, holder.value()).isValid());
    }

    public boolean testWeather(ServerLevel level) {
        return weather.isEmpty() || weather.get().test(level);
    }

    public boolean testTime(ServerLevel level) {
        return time.isEmpty() || time.get().test(level.getDayTime() % 24000L);
    }

    public boolean testMoonPhase(ServerLevel level) {
        return moonPhase.isEmpty() || moonPhase.get().test(level.getMoonPhase());
    }

    public boolean testLightLevel(ServerLevel level, BlockPos pos) {
        return lightLevel.isEmpty() || lightLevel.get().test(level.getRawBrightness(pos, 0));
    }

    public boolean testHeight(BlockPos pos) {
        return height.isEmpty() || height.get().test(pos.getY());
    }

    public boolean testPrerequisites(ServerPlayer player) {
        return true; // TODO: prerequisite data isn't stored yet
    }
}
