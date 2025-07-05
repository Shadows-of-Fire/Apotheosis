package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.util.RadialUtil;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialData;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.level.BlockEvent;

public class RadialAffix extends Affix {

    public static final Codec<RadialAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(Codec.list(RadialData.CODEC)).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, RadialAffix::new));

    public static final ResourceLocation AFFIX_RADIAL_DATA_CACHED_OBJECT = Apotheosis.loc("afx_radial_data");

    protected final Set<LootCategory> categories;
    protected final Map<LootRarity, List<RadialData>> values;

    public RadialAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, List<RadialData>> values) {
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
        RadialData data = this.getTrueLevel(inst);
        return Component.translatable("affix." + this.id() + ".desc", data.x(), data.y());
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);
        RadialData min = this.getTrueLevel(inst.getRarity(), 0);
        RadialData max = this.getTrueLevel(inst.getRarity(), 1);

        if (min != max) {
            Component minComp = Component.translatable("%sx%s", min.x(), min.y());
            Component maxComp = Component.translatable("%sx%s", max.x(), max.y());
            comp.append(valueBounds(minComp, maxComp));
        }

        return comp;
    }

    // EventPriority.LOW
    public static void onBreak(BlockEvent.BreakEvent e) {
        Player player = e.getPlayer();
        RadialData data = getRadialData(player.getMainHandItem());
        if (data != null) {
            RadialUtil.attemptRadialMining(e, data);
        }
    }

    @Nullable
    public static RadialData getRadialData(ItemStack tool) {
        return CachedObjectSource.getOrCreate(tool, AFFIX_RADIAL_DATA_CACHED_OBJECT, RadialAffix::getRadialDataImpl, CachedObject.hashComponents(Apoth.Components.AFFIXES));
    }

    @Nullable
    private static RadialData getRadialDataImpl(ItemStack tool) {
        if (tool.has(Components.AFFIXES)) {
            AffixInstance inst = AffixHelper.streamAffixes(tool).filter(i -> i.getAffix() instanceof RadialAffix).findFirst().orElse(null);
            if (inst != null && inst.isValid()) {
                return ((RadialAffix) inst.getAffix()).getTrueLevel(inst.rarity().get(), inst.level());
            }
        }
        return null;
    }

    private RadialData getTrueLevel(AffixInstance inst) {
        return this.getTrueLevel(inst.getRarity(), inst.level());
    }

    private RadialData getTrueLevel(LootRarity rarity, float level) {
        var list = this.values.get(rarity);
        return list.get(Math.min(list.size() - 1, (int) Mth.lerp(level, 0, list.size())));
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return this.values.get(inst.getRarity()).size() == 1;
    }

    public static class Builder extends AffixBuilder<Builder> {

        protected final Map<LootRarity, List<RadialData>> values = new HashMap<>();
        protected final Set<LootCategory> categories = new LinkedHashSet<>();

        public Builder value(LootRarity rarity, UnaryOperator<DataListBuilder> config) {
            List<RadialData> list = new ArrayList<>();
            config.apply(new DataListBuilder(){

                @Override
                public DataListBuilder radii(int x, int y, int xOffset, int yOffset) {
                    list.add(new RadialData(x, y, xOffset, yOffset));
                    return this;
                }

            });

            this.values.put(rarity, list);
            return this;
        }

        public Builder categories(LootCategory... cats) {
            for (LootCategory cat : cats) {
                this.categories.add(cat);
            }
            return this;
        }

        public RadialAffix build() {
            Preconditions.checkNotNull(this.definition);
            Preconditions.checkArgument(this.values.size() > 0);
            Preconditions.checkArgument(this.categories.size() > 0);
            return new RadialAffix(this.definition, this.categories, this.values);
        }

        public static interface DataListBuilder {

            DataListBuilder radii(int x, int y, int xOffset, int yOffset);

            default DataListBuilder radii(int x, int y) {
                return this.radii(x, y, 0, 0);
            }
        }
    }

}
