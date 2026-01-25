package dev.shadowsoffire.apotheosis.loot;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;

public interface LootRule extends CodecProvider<LootRule> {

    public static final CodecMap<LootRule> CODEC = new CodecMap<>("Loot Rule");

    /**
     * Executes this loot rule, applying the changes to the passed item stack.
     *
     * @param stack  The item stack being modified.
     * @param rand   The random source.
     * @param rarity The current loot rarity (which owns this rule).
     * @param tier   The player's world tier.
     * @param luck   The player's luck.
     */
    void execute(ItemStack stack, LootRarity rarity, GenContext ctx);

    public static void initCodecs() {
        register("component", ComponentLootRule.CODEC);
        register("affix", AffixLootRule.CODEC);
        register("socket", SocketLootRule.CODEC);
        register("durability", DurabilityLootRule.CODEC);
        register("chanced", ChancedLootRule.CODEC);
        register("combined", CombinedLootRule.CODEC);
        register("select", SelectLootRule.CODEC);
    }

    private static void register(String id, Codec<? extends LootRule> codec) {
        CODEC.register(Apotheosis.loc(id), codec);
    }

    /**
     * Applies one or more data components to the target item stack.
     */
    public static record ComponentLootRule(DataComponentPatch components) implements LootRule {

        public static Codec<ComponentLootRule> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            DataComponentPatch.CODEC.fieldOf("components").forGetter(ComponentLootRule::components))
            .apply(inst, ComponentLootRule::new));

        @Override
        public Codec<ComponentLootRule> getCodec() {
            return CODEC;
        }

        @Override
        public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
            stack.applyComponents(this.components);
        }

    }

    /**
     * Applies one affix from the specified category to the item stack.
     */
    public static record AffixLootRule(AffixType type) implements LootRule {

        public static Codec<AffixLootRule> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            AffixType.CODEC.fieldOf("affix_type").forGetter(AffixLootRule::type))
            .apply(inst, AffixLootRule::new));

        @Override
        public Codec<AffixLootRule> getCodec() {
            return CODEC;
        }

        @Override
        public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
            List<WeightedEntry.Wrapper<Affix>> available = LootController.getWeightedAffixes(stack, rarity, this.type, ctx);
            int weight = WeightedRandom.getTotalWeight(available);
            if (available.size() == 0 || weight == 0) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                Apotheosis.LOGGER.error("Failed to execute AffixLootRule (no affixes available) {}/{}/{}/{}!", id, RarityRegistry.INSTANCE.getKey(rarity), this.type, LootCategory.forItem(stack));
                return;
            }
            Affix selected = WeightedRandom.getRandomItem(ctx.rand(), available, weight).get().data();
            ItemAffixes.Builder builder = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY).toBuilder();
            builder.upgrade(AffixRegistry.INSTANCE.holder(selected), ctx.rand().nextFloat());
            AffixHelper.setAffixes(stack, builder.build());
        }
    }

    /**
     * Sets the number of sockets on the target item stack to a random value in the given range.
     * <p>
     * Will not reduce the number of sockets already on the item.
     */
    public static record SocketLootRule(int min, int max) implements LootRule {

        public static Codec<SocketLootRule> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.intRange(0, 16).fieldOf("min").forGetter(SocketLootRule::min),
            Codec.intRange(1, 16).fieldOf("max").forGetter(SocketLootRule::max))
            .apply(inst, SocketLootRule::new));

        @Override
        public Codec<SocketLootRule> getCodec() {
            return CODEC;
        }

        @Override
        public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
            int sockets = SocketHelper.getSockets(stack);
            int newSockets = ctx.rand().nextIntBetweenInclusive(this.min, this.max);
            if (newSockets > sockets) {
                SocketHelper.setSockets(stack, newSockets);
            }
        }

    }

    /**
     * Sets the {@link Components#DURABILITY_BONUS} value to a random value within a range.
     * <p>
     * Overwrites any existing durability bonus percentage.
     */
    public static record DurabilityLootRule(float min, float max) implements LootRule {

        public static Codec<DurabilityLootRule> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.floatRange(0, 1).fieldOf("min").forGetter(DurabilityLootRule::min),
            Codec.floatRange(0, 1).fieldOf("max").forGetter(DurabilityLootRule::max))
            .apply(inst, DurabilityLootRule::new));

        @Override
        public Codec<DurabilityLootRule> getCodec() {
            return CODEC;
        }

        @Override
        public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
            stack.set(Components.DURABILITY_BONUS, Mth.lerp(ctx.rand().nextFloat(), this.min, this.max));
        }

    }

    /**
     * Wraps a loot rule with a random chance applied to it.
     */
    public static record ChancedLootRule(float chance, LootRule rule) implements LootRule {

        public static Codec<ChancedLootRule> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.FLOAT.fieldOf("chance").forGetter(ChancedLootRule::chance),
                LootRule.CODEC.fieldOf("rule").forGetter(ChancedLootRule::rule))
            .apply(inst, ChancedLootRule::new));

        @Override
        public Codec<ChancedLootRule> getCodec() {
            return CODEC;
        }

        @Override
        public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
            if (ctx.rand().nextFloat() <= this.chance) {
                this.rule.execute(stack, rarity, ctx);
            }
        }

    }

    /**
     * Wraps multiple loot rules into a single loot rule.
     * Can be combined with {@link ChancedLootRule} to bind multiple rules to the same chance.
     */
    public static record CombinedLootRule(List<LootRule> rules) implements LootRule {

        public static Codec<CombinedLootRule> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                LootRule.CODEC.listOf().fieldOf("rules").forGetter(CombinedLootRule::rules))
            .apply(inst, CombinedLootRule::new));

        @Override
        public Codec<CombinedLootRule> getCodec() {
            return CODEC;
        }

        @Override
        public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
            for (LootRule rule : this.rules) {
                rule.execute(stack, rarity, ctx);
            }
        }

    }

    /**
     * Implements a select loot rule, which rolls a chance and picks one rule if true, and another if false.
     */
    public static record SelectLootRule(float chance, LootRule ifTrue, LootRule ifFalse) implements LootRule {

        public static Codec<SelectLootRule> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.FLOAT.fieldOf("chance").forGetter(SelectLootRule::chance),
                LootRule.CODEC.fieldOf("if_true").forGetter(SelectLootRule::ifTrue),
                LootRule.CODEC.fieldOf("if_false").forGetter(SelectLootRule::ifFalse))
            .apply(inst, SelectLootRule::new));

        @Override
        public Codec<SelectLootRule> getCodec() {
            return CODEC;
        }

        @Override
        public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
            if (ctx.rand().nextFloat() <= this.chance) {
                this.ifTrue.execute(stack, rarity, ctx);
            }
            else {
                this.ifFalse.execute(stack, rarity, ctx);
            }
        }

    }

}
