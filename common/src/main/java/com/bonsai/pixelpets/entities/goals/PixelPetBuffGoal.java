package com.bonsai.pixelpets.entities.goals;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.bonsai.pixelpets.pixelpets.registration.data.ability.AttributeModifierApplication;
import com.bonsai.pixelpets.pixelpets.registration.data.ability.LeveledBuffData.BuffData;
import com.bonsai.pixelpets.pixelpets.registration.data.ability.StatusEffectApplication;
import com.bonsai.pixelpets.pixelpets.registration.data.cosmetic.ParticleData;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO status effects work, proper testing of attributes needed
// TODO test that attributes properly get removed/updated on pet level-up
// TODO test that attributes properly get removed when pet despawn/unequip/log-out/etc
public class PixelPetBuffGoal extends Goal {

    private final AbstractPixelPetEntity pet;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<String, Map<UUID, Map<ResourceLocation, TrackedModifier>>> appliedModifiers = new HashMap<>();
    private List<BuffData> buffs = List.of();
    private long ticks;

    public PixelPetBuffGoal(AbstractPixelPetEntity pet) {
        this.pet = pet;
        this.refreshBuffs();
    }

    @Override
    public boolean canUse() {
        return this.pet.isAlive() && this.pet.isTame();
    }

    @Override
    public void tick() {
        this.ticks++;

        for (BuffData buff : this.buffs) {
            this.processBuff(buff);
        }
    }

    public void onAbilityLevelChanged() {
        this.cleanup();
        this.refreshBuffs();
    }

    public void cleanup() {
        this.cleanupAllModifiers();
        this.cooldowns.clear();
    }

    @Override
    public void stop() {
        this.cleanup();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void refreshBuffs() {
        this.buffs = this.pet.getData().map(data -> List.copyOf(data.buff().resolve(this.pet.getAbilityLevel()).buffs())).orElseGet(List::of);
    }

    private void processBuff(BuffData buff) {
        String buffId = buff.id();

        List<LivingEntity> eligibleTargets = this.getTargets(buff).stream().filter(buff::test).toList();

        Set<UUID> eligibleTargetIds = new HashSet<>();
        for (LivingEntity target : eligibleTargets) {
            eligibleTargetIds.add(target.getUUID());
        }

        this.removeTrackedTargetsNotIn(buffId, eligibleTargetIds);

        if (eligibleTargets.isEmpty()) {
            return;
        }

        if (this.ticks < this.cooldowns.getOrDefault(buffId, 0L)) {
            return;
        }

        for (LivingEntity target : eligibleTargets) {
            boolean newlyApplied = this.applyStatusEffects(target, buff.statusEffects());

            for (AttributeModifierApplication application : buff.attributeModifiers()) {
                newlyApplied |= this.applyTrackedModifier(buffId, target, application);
            }

            if (newlyApplied) {
                buff.particles().ifPresent(particles -> this.applyParticles(target, particles));
            }
        }

        this.cooldowns.put(buffId, this.ticks + Math.max(0, buff.cooldown()));
    }

    private List<LivingEntity> getTargets(BuffData buff) {
        if (!(this.pet.level() instanceof ServerLevel serverLevel)) {
            return List.of();
        }

        Map<UUID, LivingEntity> targets = new LinkedHashMap<>();

        if (buff.target().self()) {
            this.addTarget(targets, this.pet);
        }

        LivingEntity owner = this.pet.getOwner();

        if (buff.target().owner()) {
            this.addTarget(targets, owner);
        }

        if (buff.target().otherPets() && owner instanceof Player player && player instanceof PlayerPetAccess access) {
            for (UUID uuid : access.pixelPets$getActivePets()) {
                if (uuid.equals(this.pet.getUUID())) {
                    continue;
                }

                Entity entity = serverLevel.getEntity(uuid);
                if (entity instanceof AbstractPixelPetEntity otherPet) {
                    this.addTarget(targets, otherPet);
                }
            }
        }

        return List.copyOf(targets.values());
    }

    private void addTarget(Map<UUID, LivingEntity> targets, LivingEntity target) {
        if (target != null && target.isAlive()) {
            targets.putIfAbsent(target.getUUID(), target);
        }
    }

    private boolean applyStatusEffects(LivingEntity target, List<StatusEffectApplication> applications) {
        boolean newlyApplied = false;
        for (StatusEffectApplication application : applications) {
            if (target.getRandom().nextFloat() >= application.chance()) continue;

            // addEffect would be enough if duration wasn't counted as updating
            boolean alreadyHadEffect = target.hasEffect(application.effect());
            boolean applied = target.addEffect(new MobEffectInstance(application.effect(), application.duration(), application.amplifier()));

            if (!alreadyHadEffect && applied) {
                newlyApplied = true;
            }
        }
        return newlyApplied;
    }

    private boolean applyTrackedModifier(String buffId, LivingEntity target, AttributeModifierApplication application) {
        AttributeInstance attribute = target.getAttribute(application.attribute());
        if (attribute == null) {
            return false;
        }

        ResourceLocation runtimeId = this.runtimeModifierId(buffId, application);

        Map<ResourceLocation, TrackedModifier> owned = this.appliedModifiers.computeIfAbsent(buffId, ignored -> new HashMap<>()).computeIfAbsent(target.getUUID(), ignored -> new HashMap<>());

        TrackedModifier previous = owned.get(runtimeId);
        if (previous != null && previous.target() != target) {
            previous.remove();
            owned.remove(runtimeId);
        }

        AttributeModifier definition = application.modifier();
        AttributeModifier current = attribute.getModifier(runtimeId);

        if (owned.containsKey(runtimeId)
                && current != null
                && Double.compare(current.amount(), definition.amount()) == 0
                && current.operation() == definition.operation()) {
            return false;
        }

        if (current != null) {
            attribute.removeModifier(runtimeId);
        }

        attribute.addTransientModifier(new AttributeModifier(runtimeId, definition.amount(), definition.operation()));

        if (attribute.getModifier(runtimeId) != null) {
            owned.put(runtimeId, new TrackedModifier(target, application.attribute(), runtimeId));
            return true;
        }

        return false;
    }

    // TODO might be a nicer/cleaner approach
    private ResourceLocation runtimeModifierId(String buffId, AttributeModifierApplication application) {
        ResourceLocation attributeId = application.attribute().unwrapKey().map(key -> key.location()).orElseThrow(() -> new IllegalStateException("Buff attribute has no registry key: " + application.attribute()));

        String identity = this.pet.getUUID() + "\0" + buffId + "\0" + attributeId + "\0" + application.modifier().id();

        String uniquePart = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");

        return PixelPets.id(uniquePart);
    }

    private void applyParticles(LivingEntity target, ParticleData data) {
        if (!(target.level() instanceof ServerLevel level)) return;

        if (data.particleType() instanceof SimpleParticleType particle) {
            level.sendParticles(
                    particle,
                    target.getX(),
                    target.getY() + target.getBbHeight() / 2.0,
                    target.getZ(),
                    data.count(),
                    0.3,
                    target.getBbHeight() / 4.0,
                    0.3,
                    0.02
            );
        }
    }

    private void removeTrackedTargetsNotIn(String buffId, Set<UUID> eligibleTargetIds) {
        Map<UUID, Map<ResourceLocation, TrackedModifier>> byTarget = this.appliedModifiers.get(buffId);

        if (byTarget == null) {
            return;
        }

        for (UUID targetId : new ArrayList<>(byTarget.keySet())) {
            if (!eligibleTargetIds.contains(targetId)) {
                this.removeTargetModifiers(buffId, targetId);
            }
        }
    }

    private void removeTargetModifiers(String buffId, UUID targetId) {
        Map<UUID, Map<ResourceLocation, TrackedModifier>> byTarget = this.appliedModifiers.get(buffId);

        if (byTarget == null) {
            return;
        }

        Map<ResourceLocation, TrackedModifier> removed = byTarget.remove(targetId);
        if (removed != null) {
            for (TrackedModifier modifier : removed.values()) {
                modifier.remove();
            }
        }

        if (byTarget.isEmpty()) {
            this.appliedModifiers.remove(buffId);
        }
    }

    private void cleanupAllModifiers() {
        for (Map<UUID, Map<ResourceLocation, TrackedModifier>> byTarget : this.appliedModifiers.values()) {
            for (Map<ResourceLocation, TrackedModifier> byModifier : byTarget.values()) {
                for (TrackedModifier modifier : byModifier.values()) {
                    modifier.remove();
                }
            }
        }

        this.appliedModifiers.clear();
    }

    private record TrackedModifier(LivingEntity target, Holder<Attribute> attribute, ResourceLocation runtimeId) {
        private void remove() {
            AttributeInstance instance = this.target.getAttribute(this.attribute);
            if (instance != null) {
                instance.removeModifier(this.runtimeId);
            }
        }
    }
}