package dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantmentBonus extends GemBonus {

    protected final Enchantment ench;
    protected final boolean mustExist;
    protected final boolean global;
    protected final Map<LootRarity, Integer> values;

    public static Codec<EnchantmentBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            ForgeRegistries.ENCHANTMENTS.getCodec().fieldOf("enchantment").forGetter(a -> a.ench),
            PlaceboCodecs.nullableField(Codec.BOOL, "must_exist", false).forGetter(a -> a.mustExist),
            PlaceboCodecs.nullableField(Codec.BOOL, "global", false).forGetter(a -> a.global),
            LootRarity.mapCodec(Codec.intRange(1, 127)).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, EnchantmentBonus::new));

    public EnchantmentBonus(GemClass gemClass, Enchantment ench, boolean mustExist, boolean global, Map<LootRarity, Integer> values) {
        super(Apotheosis.loc("enchantment"), gemClass);
        this.ench = ench;
        this.values = values;
        this.mustExist = mustExist;
        this.global = global;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        int level = this.values.get(rarity);
        String desc = "bonus." + this.getId() + ".desc";
        if (this.global) {
            desc += ".global";
        }
        else if (this.mustExist) {
            desc += ".mustExist";
        }
        var enchName = Component.translatable(this.ench.getDescriptionId());
        var style = this.ench.getFullname(0).getStyle();
        if (style.getColor() != null && style.getColor().getValue() != ChatFormatting.GRAY.getColor()) enchName.withStyle(style);
        return Component.translatable(desc, level, Component.translatable("misc.apotheosis.level" + (level > 1 ? ".many" : "")), enchName).withStyle(ChatFormatting.GREEN);
    }

    @Override
    public void getEnchantmentLevels(ItemStack gemStack, LootRarity rarity, Map<Enchantment, Integer> enchantments) {
        int level = this.values.get(rarity);
        if (this.global) {
            for (Enchantment e : enchantments.keySet()) {
                enchantments.computeIfPresent(e, (key, val) -> val > 0 ? val + level : 0);
            }
        }
        else if (this.mustExist) {
            enchantments.computeIfPresent(this.ench, (key, val) -> val > 0 ? val + level : 0);
        }
        else {
            enchantments.merge(this.ench, level, Integer::sum);
        }
    }

    @Override
    public GemBonus validate() {
        Preconditions.checkNotNull(this.ench, "Invalid DamageReductionBonus with null type");
        Preconditions.checkNotNull(this.values, "Invalid DamageReductionBonus with null values");
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

}
