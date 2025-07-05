package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder.ValuedAffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

public class EnchantmentAffix extends Affix {

    public static Codec<EnchantmentAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            Enchantment.CODEC.fieldOf("enchantment").forGetter(a -> a.ench),
            Mode.CODEC.optionalFieldOf("mode", Mode.SINGLE).forGetter(a -> a.mode),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories))
        .apply(inst, EnchantmentAffix::new));

    protected final Holder<Enchantment> ench;
    protected final Mode mode;
    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> categories;

    public EnchantmentAffix(AffixDefinition def, Holder<Enchantment> ench, Mode mode, Map<LootRarity, StepFunction> values, Set<LootCategory> categories) {
        super(def);
        this.ench = ench;
        this.values = values;
        this.mode = mode;
        this.categories = categories;
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        int level = this.values.get(inst.getRarity()).getInt(inst.level());
        String desc = "bonus.apotheosis:enchantment.desc";
        if (this.mode == Mode.GLOBAL) {
            desc += ".global";
        }
        else if (this.mode == Mode.EXISTING) {
            desc += ".mustExist";
        }
        Component enchName = this.ench.value().description().plainCopy();
        return Component.translatable(desc, level, Component.translatable("misc.apotheosis.level" + (level > 1 ? ".many" : "")), enchName).withStyle(ChatFormatting.GREEN);
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);
        StepFunction value = this.values.get(inst.getRarity());
        return comp.append(valueBounds(Component.literal("" + (int) value.min()), Component.literal("" + (int) value.max())));
    }

    @Override
    public void getEnchantmentLevels(AffixInstance inst, GetEnchantmentLevelEvent event) {
        ItemEnchantments.Mutable enchantments = event.getEnchantments();
        int level = this.values.get(inst.getRarity()).getInt(inst.level());
        if (this.mode == Mode.GLOBAL) {
            for (Holder<Enchantment> e : enchantments.keySet()) {
                int current = enchantments.getLevel(e);
                if (current > 0) {
                    enchantments.upgrade(e, current + level);
                }
            }
        }
        else if (this.mode == Mode.EXISTING) {
            int current = enchantments.getLevel(this.ench);
            if (current > 0) {
                enchantments.upgrade(this.ench, current + level);
            }
        }
        else {
            enchantments.upgrade(this.ench, enchantments.getLevel(this.ench) + level);
        }
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) {
            return false;
        }
        return (this.categories.isEmpty() || this.categories.contains(cat)) && this.values.containsKey(rarity);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return this.values.get(inst.getRarity()).isConstant();
    }

    public static enum Mode {
        SINGLE,
        EXISTING,
        GLOBAL;

        public static final Codec<Mode> CODEC = PlaceboCodecs.enumCodec(Mode.class);
    }

    public static class Builder extends ValuedAffixBuilder<Builder> {
        protected final Holder<Enchantment> enchantment;
        protected final Mode mode;
        protected final Set<LootCategory> categories = new HashSet<>();

        public Builder(Holder<Enchantment> enchantment, Mode mode) {
            this.enchantment = enchantment;
            this.mode = mode;
        }

        public Builder categories(LootCategory... cats) {
            for (LootCategory cat : cats) {
                this.categories.add(cat);
            }
            return this;
        }

        public EnchantmentAffix build() {
            Preconditions.checkArgument(!this.values.isEmpty());
            return new EnchantmentAffix(this.definition, this.enchantment, this.mode, this.values, this.categories);
        }
    }

}
