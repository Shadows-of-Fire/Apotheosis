package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * A spawn condition is a predicate that applies to a spawning entity, which may be used to determine if something should happen.
 * <p>
 * This is used by both {@link Invader} and {@link Elite} to do advanced filtering that is not possible via {@link Constraints}.
 */
public interface SpawnCondition extends CodecProvider<SpawnCondition> {

    public static final CodecMap<SpawnCondition> CODEC = new CodecMap<>("Apothic Spawn Conditions");

    /**
     * Checks if this spawn condition should allow the underlying effect to activate.
     * <p>
     * How multiple spawn conditions interact is up to the underlying effect. Typically, multiple conditions in a list will be merged via logical and.
     *
     * @param mob       The mob in question.
     * @param level     The level the mob is spawning into.
     * @param spawnType The spawn type of the mob.
     * @param entityNbt The entity NBT. This is always null unless a spawn condition requests it via {@link #requiresNbtAccess()}.
     * @return True if the condition has passed, false otherwise.
     */
    boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, @Nullable CompoundTag entityNbt);

    default boolean requiresNbtAccess() {
        return false;
    }

    /**
     * Checks all spawn conditions in a list. The entire list is considered to be "true" if it is empty, or every condition in the list returns true.
     */
    public static boolean checkAll(List<SpawnCondition> conditions, Mob mob, ServerLevelAccessor level, MobSpawnType type) {
        if (conditions.isEmpty()) {
            return true;
        }

        boolean requiresNbt = false;
        for (SpawnCondition ex : conditions) {
            requiresNbt |= ex.requiresNbtAccess();
        }

        CompoundTag nbt = requiresNbt ? mob.saveWithoutId(new CompoundTag()) : null;

        boolean success = true;
        for (SpawnCondition ex : conditions) {
            success &= ex.test(mob, level, type, nbt);
        }

        return success;
    }

    public static void initCodecs() {
        register("spawn_type", SpawnTypeCondition.CODEC);
        register("surface_type", SurfaceTypeCondition.CODEC);
        register("has_tag", EntityTagCondition.CODEC);
        register("is_monster", IsMonsterCondition.CODEC);
        register("nbt", NbtCondition.CODEC);
        register("and", AndCondition.CODEC);
        register("or", OrCondition.CODEC);
        register("not", NotCondition.CODEC);
        register("xor", XorCondition.CODEC);
    }

    private static void register(String id, Codec<? extends SpawnCondition> codec) {
        CODEC.register(Apotheosis.loc(id), codec);
    }

    /**
     * Requires that the target mob have a specific spawn type.
     */
    public static record SpawnTypeCondition(Set<MobSpawnType> types) implements SpawnCondition {

        public static Codec<SpawnTypeCondition> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                PlaceboCodecs.setOf(PlaceboCodecs.enumCodec(MobSpawnType.class)).fieldOf("spawn_types").forGetter(SpawnTypeCondition::types))
            .apply(inst, SpawnTypeCondition::new));

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
            return this.types.contains(spawnType);
        }

        public static SpawnTypeCondition of(MobSpawnType... types) {
            return new SpawnTypeCondition(new LinkedHashSet<>(Arrays.asList(types)));
        }

    }

    /**
     * Checks that an entity matches the surface requirement as specified by the underlying {@link SurfaceType}.
     */
    public static record SurfaceTypeCondition(SurfaceType type) implements SpawnCondition {

        public static Codec<SurfaceTypeCondition> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                SurfaceType.CODEC.fieldOf("surface_type").forGetter(SurfaceTypeCondition::type))
            .apply(inst, SurfaceTypeCondition::new));

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
            return this.type.test(level, mob.blockPosition());
        }

    }

    /**
     * Checks that an entity has the given entity type tag.
     */
    public static record EntityTagCondition(TagKey<EntityType<?>> tag) implements SpawnCondition {

        public static Codec<EntityTagCondition> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                TagKey.codec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(EntityTagCondition::tag))
            .apply(inst, EntityTagCondition::new));

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
            return mob.getType().is(tag);
        }

    }

    /**
     * Checks that an entity is an {@link Monster}.
     */
    public static record IsMonsterCondition() implements SpawnCondition {

        public static Codec<IsMonsterCondition> CODEC = Codec.unit(IsMonsterCondition::new);

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
            return mob instanceof Monster;
        }

    }

    /**
     * Requires that the entity has a specific NBT value.
     * <p>
     * Use sparingly, as introducing this condition will cause all entities that spawn to be serialized for this check to work.
     * It may be preferrable to request a new spawn condition be added instead of using this in production.
     */
    public static record NbtCondition(CompoundTag nbt) implements SpawnCondition {

        public static Codec<NbtCondition> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                NBTAdapter.EITHER_CODEC.fieldOf("nbt").forGetter(NbtCondition::nbt))
            .apply(inst, NbtCondition::new));

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
            return NbtUtils.compareNbt(this.nbt, entityNbt, true);
        }

        @Override
        public boolean requiresNbtAccess() {
            return true;
        }

    }

    /**
     * This class performs the logical-and of all child exclusions.
     */
    public static record AndCondition(List<SpawnCondition> spawnConditions) implements SpawnCondition {

        public static Codec<AndCondition> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                SpawnCondition.CODEC.listOf().fieldOf("spawn_conditions").forGetter(AndCondition::spawnConditions))
            .apply(inst, AndCondition::new));

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType type, CompoundTag nbt) {
            boolean success = true;
            for (SpawnCondition cond : this.spawnConditions) {
                success &= cond.test(mob, level, type, nbt);
            }

            return success;
        }

        @Override
        public boolean requiresNbtAccess() {
            boolean requiresNbt = false;
            for (SpawnCondition ex : this.spawnConditions) {
                requiresNbt |= ex.requiresNbtAccess();
            }
            return requiresNbt;
        }

    }

    /**
     * This class performs the logical-or of all child exclusions.
     */
    public static record OrCondition(List<SpawnCondition> spawnConditions) implements SpawnCondition {

        public static Codec<OrCondition> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                SpawnCondition.CODEC.listOf().fieldOf("spawn_conditions").forGetter(OrCondition::spawnConditions))
            .apply(inst, OrCondition::new));

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType type, CompoundTag nbt) {
            for (SpawnCondition ex : this.spawnConditions) {
                if (ex.test(mob, level, type, nbt)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean requiresNbtAccess() {
            boolean requiresNbt = false;
            for (SpawnCondition ex : this.spawnConditions) {
                requiresNbt |= ex.requiresNbtAccess();
            }
            return requiresNbt;
        }

    }

    /**
     * Inverts the contained spawn condition.
     */
    public static record NotCondition(SpawnCondition spawnCondition) implements SpawnCondition {

        public static Codec<NotCondition> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                SpawnCondition.CODEC.fieldOf("spawn_condition").forGetter(NotCondition::spawnCondition))
            .apply(inst, NotCondition::new));

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType type, CompoundTag nbt) {
            return !this.spawnCondition.test(mob, level, type, nbt);
        }

        @Override
        public boolean requiresNbtAccess() {
            return this.spawnCondition.requiresNbtAccess();
        }

    }

    /**
     * Returns the exclusive or between two spawn conditions.
     */
    public static record XorCondition(SpawnCondition left, SpawnCondition right) implements SpawnCondition {

        public static Codec<XorCondition> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                SpawnCondition.CODEC.fieldOf("left").forGetter(XorCondition::left),
                SpawnCondition.CODEC.fieldOf("right").forGetter(XorCondition::right))
            .apply(inst, XorCondition::new));

        @Override
        public Codec<? extends SpawnCondition> getCodec() {
            return CODEC;
        }

        @Override
        public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType type, CompoundTag nbt) {
            return this.left.test(mob, level, type, nbt) ^ this.right.test(mob, level, type, nbt);
        }

        @Override
        public boolean requiresNbtAccess() {
            return this.left.requiresNbtAccess() || this.right.requiresNbtAccess();
        }

    }

}
