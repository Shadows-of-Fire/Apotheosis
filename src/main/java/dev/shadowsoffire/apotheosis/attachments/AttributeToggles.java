package dev.shadowsoffire.apotheosis.attachments;

import java.util.HashSet;
import java.util.Set;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.affix.effect.AttributeToggleAffix;
import dev.shadowsoffire.apotheosis.util.AttributeInstanceExt;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * Holds the set of attributes whose bonuses a player has elected to suppress via an {@link AttributeToggleAffix}.
 * <p>
 * Suppression is a clamp. An attribute's value is limited to its base value (plus the vanilla sprint boost, which is
 * carved out). The final value is {@code min(actual, cap)}. Technically speaking we have four cases:
 * <ul>
 * <li>Bonuses alone: clamped to the base value.</li>
 * <li>Penalties alone (e.g. Slowness): unchanged, the player stays slowed.</li>
 * <li>Bonuses outweighing penalties (e.g. Speed V + Slowness I): clamped to the base value, the player is <em>not</em> slowed.</li>
 * <li>Penalties outweighing bonuses: unchanged, the net-slowed value passes through.</li>
 * </ul>
 */
public final class AttributeToggles {

    public static final AttributeToggles EMPTY = new AttributeToggles(Set.of());

    public static final Codec<AttributeToggles> CODEC = PlaceboCodecs.setOf(BuiltInRegistries.ATTRIBUTE.holderByNameCodec())
        .xmap(AttributeToggles::new, AttributeToggles::suppressed);

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeToggles> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE)
        .<Set<Holder<Attribute>>>apply(ByteBufCodecs.collection(HashSet::new))
        .map(AttributeToggles::new, AttributeToggles::suppressed);

    /**
     * Id of {@code LivingEntity#SPEED_MODIFIER_SPRINTING}, the transient +30% multiplied-total modifier applied while sprinting.
     */
    public static final ResourceLocation SPRINTING_MODIFIER_ID = ResourceLocation.withDefaultNamespace("sprinting");

    private final Set<Holder<Attribute>> suppressed;

    /**
     * Transient cache of computed caps. Small array map since a player rarely suppresses more than one attribute.
     */
    private final Reference2ObjectArrayMap<Holder<Attribute>, CapEntry> capCache = new Reference2ObjectArrayMap<>(2);

    public AttributeToggles(Set<Holder<Attribute>> suppressed) {
        this.suppressed = Set.copyOf(suppressed);
    }

    public Set<Holder<Attribute>> suppressed() {
        return this.suppressed;
    }

    public boolean isEmpty() {
        return this.suppressed.isEmpty();
    }

    public boolean isSuppressed(Holder<Attribute> attribute) {
        return !this.suppressed.isEmpty() && this.suppressed.contains(attribute);
    }

    /**
     * Returns a copy of this object with the suppression state of the given attribute flipped.
     */
    public AttributeToggles toggle(Holder<Attribute> attribute) {
        Set<Holder<Attribute>> copy = new HashSet<>(this.suppressed);
        if (!copy.remove(attribute)) {
            copy.add(attribute);
        }
        return new AttributeToggles(copy);
    }

    /**
     * Returns this object if every suppressed attribute is in {@code allowed}, otherwise a copy retaining only the allowed ones.
     */
    public AttributeToggles retain(Set<Holder<Attribute>> allowed) {
        if (allowed.containsAll(this.suppressed)) {
            return this;
        }
        Set<Holder<Attribute>> copy = new HashSet<>(this.suppressed);
        copy.retainAll(allowed);
        return copy.isEmpty() ? EMPTY : new AttributeToggles(copy);
    }

    /**
     * Computes the suppressed value of an attribute instance.
     * <p>
     * The result is {@code min(actualValue, cap)}, where {@code cap} is the attribute value calculated with every modifier
     * removed except {@link #SPRINTING_MODIFIER_ID}. The cap is cached against the instance's modification counter.
     */
    public double getCappedValue(AttributeInstance inst) {
        AttributeInstanceExt ext = AttributeInstanceExt.of(inst);
        int modCount = ext.apoth$getModificationCount();

        CapEntry entry = this.capCache.get(inst.getAttribute());
        if (entry == null) {
            entry = new CapEntry();
            this.capCache.put(inst.getAttribute(), entry);
        }
        if (entry.modCount != modCount || !entry.valid) {
            entry.cap = computeCap(inst);
            entry.modCount = modCount;
            entry.valid = true;
        }

        return Math.min(inst.getValue(), entry.cap);
    }

    /**
     * Mirrors {@code AttributeInstance#calculateValue()}, skipping any modifier rejected by {@link #isRetained(AttributeModifier)}.
     */
    private static double computeCap(AttributeInstance inst) {
        double base = inst.getBaseValue();

        for (AttributeModifier mod : inst.getModifiers(Operation.ADD_VALUE).values()) {
            if (isRetained(mod)) {
                base += mod.amount();
            }
        }

        double result = base;

        for (AttributeModifier mod : inst.getModifiers(Operation.ADD_MULTIPLIED_BASE).values()) {
            if (isRetained(mod)) {
                result += base * mod.amount();
            }
        }

        for (AttributeModifier mod : inst.getModifiers(Operation.ADD_MULTIPLIED_TOTAL).values()) {
            if (isRetained(mod)) {
                result *= 1.0 + mod.amount();
            }
        }

        return inst.getAttribute().value().sanitizeValue(result);
    }

    /**
     * We explicitly ignore sprinting since we still want the player to be able to sprint when speed boosts are disabled.
     */
    private static boolean isRetained(AttributeModifier mod) {
        return SPRINTING_MODIFIER_ID.equals(mod.id());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AttributeToggles other && this.suppressed.equals(other.suppressed);
    }

    @Override
    public int hashCode() {
        return this.suppressed.hashCode();
    }

    @Override
    public String toString() {
        return "AttributeToggles" + this.suppressed;
    }

    private static final class CapEntry {
        int modCount;
        double cap;
        boolean valid;
    }

}
