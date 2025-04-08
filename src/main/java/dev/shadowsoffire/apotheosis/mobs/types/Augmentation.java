package dev.shadowsoffire.apotheosis.mobs.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.mobs.util.EntityModifier;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * An Augmentation is a non-exclusive modifier that may apply to any naturally spawned mob.
 * <p>
 * Augmentations are applied very early in the mob spawn pipeline, immediately after world tier modifiers.
 * <p>
 * Mobs have a chance to be selected for augmenting. If they are selected, every loaded augmentation will attempt to apply.
 *
 * @param chance      The chance that this augmentation is selected.
 * @param constraints Any context-based restrictions on the application of this augmentation.
 * @param conditions  Any entity-based restrictions on the application of this augmentation.
 * @param modifiers   The list of modifiers that will be applied to the target entity.
 */
public record Augmentation(float chance, Constraints constraints, List<SpawnCondition> conditions, List<EntityModifier> modifiers) implements CodecProvider<Augmentation> {

    public static final Codec<Augmentation> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.floatRange(0, 1).fieldOf("application_chance").forGetter(Augmentation::chance),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(Augmentation::constraints),
            SpawnCondition.CODEC.listOf().optionalFieldOf("conditions", Collections.emptyList()).forGetter(Augmentation::conditions),
            EntityModifier.CODEC.listOf().fieldOf("modifiers").forGetter(Augmentation::modifiers))
        .apply(inst, Augmentation::new));

    @Override
    public Codec<? extends Augmentation> getCodec() {
        return CODEC;
    }

    public boolean canApply(ServerLevelAccessor level, Mob mob, MobSpawnType type, GenContext ctx) {
        if (!this.constraints.test(ctx)) {
            return false;
        }

        return SpawnCondition.checkAll(this.conditions, mob, level, type);
    }

    public void apply(Mob mob, GenContext ctx) {
        for (EntityModifier em : this.modifiers) {
            em.apply(mob, ctx);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float chance = 1F;
        private Constraints constraints = Constraints.EMPTY;
        private List<SpawnCondition> conditions = new ArrayList<>();
        private List<EntityModifier> modifiers = new ArrayList<>();

        public Builder chance(float chance) {
            this.chance = chance;
            return this;
        }

        public Builder constraints(Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public Builder conditions(SpawnCondition... condition) {
            this.conditions.addAll(Arrays.asList(condition));
            return this;
        }

        public Builder modifiers(EntityModifier... modifiers) {
            this.modifiers.addAll(Arrays.asList(modifiers));
            return this;
        }

        public Augmentation build() {
            if (modifiers.isEmpty()) {
                throw new IllegalStateException("At least one modifier must be added");
            }
            return new Augmentation(chance, constraints, conditions, modifiers);
        }
    }

}
