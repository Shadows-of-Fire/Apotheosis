package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Augmentation;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apothic_attributes.modifiers.EquipmentSlotCompat;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSet.SetPredicate;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;

/**
 * Underlying modifiers used by {@link Augmentation}.
 */
public interface EntityModifier extends CodecProvider<EntityModifier> {

    public static final CodecMap<EntityModifier> CODEC = new CodecMap<>("Apothic Entity Modifier");

    /**
     * Applies this modifier to the target mob.
     */
    void apply(Mob mob, GenContext ctx);

    @Deprecated(forRemoval = true)
    default void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {}

    public static void initCodecs() {
        register("mob_effect", EffectModifier.CODEC);
        register("attribute", AttributeModifier.CODEC);
        register("gear_set", GearSetModifier.CODEC);
        register("random_affix_item", RandomAffixItemModifier.CODEC);
    }

    private static void register(String id, Codec<? extends EntityModifier> codec) {
        CODEC.register(Apotheosis.loc(id), codec);
    }

    /**
     * Applies a mob effect to the target entity.
     * <p>
     * The effect is applied with infinite duration, unless the entity is a creeper, in which case the duration is reduced to 5 minutes.
     */
    public static record EffectModifier(ChancedEffectInstance effect) implements EntityModifier {

        public static Codec<EffectModifier> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ChancedEffectInstance.CONSTANT_CODEC.fieldOf("effect").forGetter(EffectModifier::effect))
            .apply(inst, EffectModifier::new));

        @Override
        public Codec<? extends EntityModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(Mob mob, GenContext ctx) {
            int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;
            mob.addEffect(this.effect.createDeterministic(duration));
        }

    }

    /**
     * Applies an attribute modifier to the target entity.
     * <p>
     * The modifier will be ignored if the entity does not have the attribute.
     */
    public static record AttributeModifier(RandomAttributeModifier modifier) implements EntityModifier {

        public static Codec<AttributeModifier> CODEC = RandomAttributeModifier.CODEC.xmap(AttributeModifier::new, AttributeModifier::modifier);

        @Override
        public Codec<? extends EntityModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(Mob mob, GenContext ctx) {
            AttributeInstance inst = mob.getAttribute(this.modifier.attribute());
            if (inst == null) {
                return;
            }
            this.modifier.apply(Apotheosis.loc("rm_ " + mob.getRandom().nextInt()), ctx.rand(), mob);
        }

    }

    /**
     * Applies a gear set to the target entity.
     */
    public static record GearSetModifier(List<SetPredicate> gearSets) implements EntityModifier {

        public static Codec<GearSetModifier> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                SetPredicate.CODEC.listOf().fieldOf("valid_gear_sets").forGetter(GearSetModifier::gearSets))
            .apply(inst, GearSetModifier::new));

        @Override
        public Codec<? extends EntityModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(Mob mob, GenContext ctx) {
            GearSet set = GearSetRegistry.INSTANCE.getRandomSet(ctx.rand(), ctx.luck(), this.gearSets);
            if (set != null) {
                set.apply(mob);
            }
        }

    }

    /**
     * Applies a random affix item to the target entity.
     */
    public static record RandomAffixItemModifier(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) implements EntityModifier {

        public static Codec<RandomAffixItemModifier> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
                PlaceboCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(a -> a.entries))
            .apply(inst, RandomAffixItemModifier::new));

        public RandomAffixItemModifier() {
            this(Set.of(), Set.of());
        }

        @Override
        public Codec<? extends EntityModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(Mob mob, GenContext ctx) {
            ItemStack stack = LootController.createAffixItemFromPools(this.rarities, this.entries, ctx);
            if (stack.isEmpty()) {
                return;
            }

            stack.set(Components.FROM_MOB, true);
            LootCategory cat = LootCategory.forItem(stack);
            EquipmentSlot slot = Arrays.stream(EquipmentSlot.values())
                .filter(eSlot -> cat.getSlots().test(EquipmentSlotCompat.fromVanilla(eSlot)))
                .findAny()
                .orElse(EquipmentSlot.MAINHAND);
            mob.setItemSlot(slot, stack);
            mob.setGuaranteedDrop(slot);
        }

    }
}
