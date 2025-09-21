package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.util.OmneticUtil;
import dev.shadowsoffire.apotheosis.util.OmneticUtil.OmneticData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;

public class OmneticAffix extends Affix {

    public static final Codec<OmneticAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(OmneticData.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, OmneticAffix::new));

    protected final Set<LootCategory> categories;
    protected final Map<LootRarity, OmneticData> values;

    public OmneticAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, OmneticData> values) {
        super(def);
        this.categories = categories;
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return this.categories.contains(cat) && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", Component.translatable("misc.apotheosis." + this.values.get(inst.getRarity()).name()));
    }

    public static void harvest(HarvestCheck e) {
        ItemStack stack = e.getEntity().getMainHandItem();
        if (!stack.isEmpty()) {
            AffixInstance inst = AffixHelper.streamAffixes(stack).filter(i -> i.getAffix() instanceof OmneticAffix).findFirst().orElse(null);
            if (inst != null && inst.isValid()) {
                OmneticData data = ((OmneticAffix) inst.getAffix()).values.get(inst.rarity().get());
                OmneticUtil.applyOmneticData(e, data);
            }
        }
    }

    // EventPriority.HIGHEST
    public static void speed(BreakSpeed e) {
        ItemStack stack = e.getEntity().getMainHandItem();
        if (!stack.isEmpty()) {
            AffixInstance inst = AffixHelper.streamAffixes(stack).filter(i -> i.getAffix() instanceof OmneticAffix).findFirst().orElse(null);
            if (inst != null && inst.isValid()) {
                OmneticData data = ((OmneticAffix) inst.getAffix()).values.get(inst.rarity().get());
                OmneticUtil.applyOmneticData(e, data);
            }
        }
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return true;
    }

    public static class Builder extends AffixBuilder<Builder> {

        protected final Set<LootCategory> categories = new LinkedHashSet<>();
        private final Map<LootRarity, OmneticData> values = new HashMap<>();

        public Builder categories(LootCategory... cats) {
            for (LootCategory cat : cats) {
                this.categories.add(cat);
            }
            return this;
        }

        public Builder value(LootRarity rarity, String name, Item... items) {
            OmneticData data = new OmneticData(name, Arrays.stream(items).map(Item::getDefaultInstance).toArray(ItemStack[]::new));
            this.values.put(rarity, data);
            return this;
        }

        public OmneticAffix build() {
            Preconditions.checkNotNull(this.definition);
            return new OmneticAffix(this.definition, this.categories, this.values);
        }

    }

}
