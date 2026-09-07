package dev.shadowsoffire.apotheosis.attachments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix.DamageType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

/**
 * Damage reductions can be attached to a {@link Mob} to reduce the amount of damage taken from specific sources.
 * <p>
 * Each {@link Reduction} is keyed by an id, similarly to attribute modifiers, so that individual entries can be added
 * and removed independently of each other.
 * <p>
 * The effective reduction for a {@link DamageType} is computed multiplicatively. Starting from a damage factor of 1,
 * each reduction {@code n} scales the factor by {@code (1 - n)}. This means two 25% reductions produce an effective
 * reduction of 43.75%, and the effective reduction can only reach 100% if a single 100% reduction is present.
 * <p>
 * This class is immutable. Use {@link Mutable} to create modified copies.
 */
public class DamageReductions {

    public static final DamageReductions EMPTY = new DamageReductions(Map.of());

    public static final Codec<DamageReductions> CODEC = Codec.simpleMap(DamageType.CODEC, Reduction.CODEC.listOf(), StringRepresentable.keys(DamageType.values()))
        .xmap(DamageReductions::new, dr -> dr.reductions)
        .codec();

    private final Map<DamageType, List<Reduction>> reductions;

    /**
     * Cache of the effective reduction for each damage type present in {@link #reductions}.
     */
    private final Map<DamageType, Float> effectiveReductions;

    private DamageReductions(Map<DamageType, List<Reduction>> reductions) {
        Map<DamageType, List<Reduction>> copy = new EnumMap<>(DamageType.class);
        Map<DamageType, Float> effective = new EnumMap<>(DamageType.class);
        reductions.forEach((type, list) -> {
            if (!list.isEmpty()) {
                copy.put(type, List.copyOf(list));
                float factor = 1F;
                for (Reduction r : list) {
                    factor *= 1F - r.amount();
                }
                effective.put(type, 1F - factor);
            }
        });
        this.reductions = Collections.unmodifiableMap(copy);
        this.effectiveReductions = Collections.unmodifiableMap(effective);
    }

    /**
     * {@return an unmodifiable view of all reductions held by this object}
     */
    public Map<DamageType, List<Reduction>> reductions() {
        return this.reductions;
    }

    /**
     * {@return an unmodifiable view of the reductions for the given damage type, which may be empty}
     */
    public List<Reduction> getReductions(DamageType type) {
        return this.reductions.getOrDefault(type, List.of());
    }

    /**
     * Gets the effective reduction for the given damage type, which is the multiplicative combination of all individual reductions.
     *
     * @return The effective reduction, as a value in [0, 1]. If no reductions are present for the type, this is 0.
     */
    public float getEffectiveReduction(DamageType type) {
        return this.effectiveReductions.getOrDefault(type, 0F);
    }

    /**
     * Applies the effective reduction of every damage type matching the given source to an incoming damage amount.
     *
     * @param src    The damage source being tested. A single source may match multiple damage types.
     * @param amount The incoming damage amount.
     * @return The damage amount after all applicable reductions.
     */
    public float applyReductions(DamageSource src, float amount) {
        for (Map.Entry<DamageType, Float> entry : this.effectiveReductions.entrySet()) {
            if (entry.getKey().test(src)) {
                amount *= 1F - entry.getValue();
            }
        }
        return amount;
    }

    public boolean isEmpty() {
        return this.reductions.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof DamageReductions that && this.reductions.equals(that.reductions);
    }

    @Override
    public int hashCode() {
        return this.reductions.hashCode();
    }

    @Override
    public String toString() {
        return "DamageReductions{reductions=" + this.reductions + "}";
    }

    /**
     * A single damage reduction entry.
     *
     * @param id     The id of this reduction, used to add or remove individual entries.
     * @param amount The reduction amount, as a value in [0, 1].
     */
    public record Reduction(ResourceLocation id, float amount) {

        public static final Codec<Reduction> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(Reduction::id),
                Codec.floatRange(0, 1).fieldOf("amount").forGetter(Reduction::amount))
            .apply(inst, Reduction::new));

    }

    /**
     * A mutable variant of {@link DamageReductions}, used to create modified copies of the immutable form.
     */
    public static class Mutable {

        private final Map<DamageType, List<Reduction>> reductions = new EnumMap<>(DamageType.class);

        public Mutable(DamageReductions base) {
            base.reductions.forEach((type, list) -> this.reductions.put(type, new ArrayList<>(list)));
        }

        /**
         * Adds a new reduction for the given damage type, replacing any existing reduction with the same id.
         * <p>
         * If the amount is less than or equal to zero, the reduction with the given id is removed instead.
         */
        public void set(DamageType type, ResourceLocation id, float amount) {
            if (amount <= 0F) {
                this.remove(type, id);
                return;
            }
            List<Reduction> list = this.reductions.computeIfAbsent(type, t -> new ArrayList<>());
            list.removeIf(r -> r.id().equals(id));
            list.add(new Reduction(id, Math.min(amount, 1F)));
        }

        /**
         * Removes the reduction with the given id, if present.
         *
         * @return True if a reduction was removed.
         */
        public boolean remove(DamageType type, ResourceLocation id) {
            List<Reduction> list = this.reductions.get(type);
            if (list == null) {
                return false;
            }
            boolean removed = list.removeIf(r -> r.id().equals(id));
            if (list.isEmpty()) {
                this.reductions.remove(type);
            }
            return removed;
        }

        public DamageReductions toImmutable() {
            return this.reductions.isEmpty() ? EMPTY : new DamageReductions(this.reductions);
        }

    }

}
