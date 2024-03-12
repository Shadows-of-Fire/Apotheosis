package dev.shadowsoffire.apotheosis.adventure.affix.socket.gem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class Gem implements CodecProvider<Gem>, ILuckyWeighted, IDimensional, RarityClamp, IStaged {

    public static final Codec<Gem> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
        PlaceboCodecs.nullableField(Codec.floatRange(0, Float.MAX_VALUE), "quality", 0F).forGetter(ILuckyWeighted::getQuality),
        PlaceboCodecs.nullableField(PlaceboCodecs.setOf(ResourceLocation.CODEC), "dimensions", Collections.emptySet()).forGetter(IDimensional::getDimensions),
        PlaceboCodecs.nullableField(LootRarity.CODEC, "min_rarity").forGetter(g -> Optional.of(g.getMinRarity())),
        PlaceboCodecs.nullableField(LootRarity.CODEC, "max_rarity").forGetter(g -> Optional.of(g.getMaxRarity())),
        GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(Gem::getBonuses),
        PlaceboCodecs.nullableField(Codec.BOOL, "unique", false).forGetter(Gem::isUnique),
        PlaceboCodecs.nullableField(PlaceboCodecs.setOf(Codec.STRING), "stages").forGetter(gem -> Optional.ofNullable(gem.getStages())))
        .apply(inst, Gem::new));

    protected final int weight;
    protected final float quality;
    protected final Set<ResourceLocation> dimensions;
    protected final List<GemBonus> bonuses;
    protected final boolean unique;
    protected final @Nullable Set<String> stages;

    protected transient final Map<LootCategory, GemBonus> bonusMap;
    protected transient final int uuidsNeeded;
    protected transient final LootRarity minRarity, maxRarity;

    public Gem(int weight, float quality, Set<ResourceLocation> dimensions, Optional<LootRarity> minRarity, Optional<LootRarity> maxRarity, List<GemBonus> bonuses, boolean unique, Optional<Set<String>> stages) {
        this.weight = weight;
        this.quality = quality;
        this.dimensions = dimensions;
        this.bonuses = bonuses;
        this.unique = unique;
        this.stages = stages.orElse(null);
        Preconditions.checkArgument(!bonuses.isEmpty(), "No bonuses were provided.");
        this.bonusMap = bonuses.stream().<Pair<LootCategory, GemBonus>>mapMulti((gemData, mapper) -> {
            for (LootCategory c : gemData.getGemClass().types()) {
                mapper.accept(Pair.of(c, gemData));
            }
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        this.uuidsNeeded = this.bonuses.stream().mapToInt(GemBonus::getNumberOfUUIDs).max().orElse(0);

        if (minRarity.isPresent()) {
            this.minRarity = minRarity.get();
        }
        else {
            this.minRarity = RarityRegistry.INSTANCE.getValues().stream().filter(bonuses.get(0)::supports).min(LootRarity::compareTo).get();
        }

        if (maxRarity.isPresent()) {
            this.maxRarity = maxRarity.get();
        }
        else {
            this.maxRarity = RarityRegistry.INSTANCE.getValues().stream().filter(bonuses.get(0)::supports).max(LootRarity::compareTo).get();
        }

        Preconditions.checkArgument(this.minRarity.ordinal() <= this.maxRarity.ordinal(), "The min rarity must be <= the max rarity.");
    }

    /**
     * Returns the number of UUIDs that need to be generated for this Gem to operate properly.<br>
     * This should be equal to the maximum amount of attribute modifiers that need to be generated for proper usage.
     */
    public int getNumberOfUUIDs() {
        return this.uuidsNeeded;
    }

    /**
     * Adds all tooltip data from this gem to the gem stack.
     *
     * @param gem      The gem stack.
     * @param purity   The purity of this gem.
     * @param tooltips The destination for tooltips.
     */
    public void addInformation(ItemStack gem, LootRarity rarity, Consumer<Component> list) {
        if (this.isUnique()) list.accept(Component.translatable("text.apotheosis.unique").withStyle(Style.EMPTY.withColor(0xC73912)));
        list.accept(CommonComponents.EMPTY);
        Style style = Style.EMPTY.withColor(0x0AFF0A);
        list.accept(Component.translatable("text.apotheosis.socketable_into").withStyle(style));
        addTypeInfo(list, this.bonusMap.keySet().toArray());
        list.accept(CommonComponents.EMPTY);
        if (this.bonuses.size() == 1) {
            list.accept(Component.translatable("item.modifiers.socket").withStyle(ChatFormatting.GOLD));
            list.accept(this.bonuses.get(0).getSocketBonusTooltip(gem, rarity));
        }
        else {
            list.accept(Component.translatable("item.modifiers.socket_in").withStyle(ChatFormatting.GOLD));
            for (GemBonus bonus : this.bonuses) {
                if (!bonus.supports(rarity)) continue;
                Component modifComp = bonus.getSocketBonusTooltip(gem, rarity);
                Component sum = Component.translatable("text.apotheosis.dot_prefix", Component.translatable("%s: %s", Component.translatable("gem_class." + bonus.getGemClass().key()), modifComp)).withStyle(ChatFormatting.GOLD);
                list.accept(sum);
            }
        }
    }

    /**
     * Checks if this gem can be applied to an item, preventing more than one unique.
     *
     * @param socketed The target item.
     * @param rarity   The rarity of the gem.
     * @param gem      The gem
     * @return If this gem can be socketed into the item.
     */
    public boolean canApplyTo(ItemStack socketed, ItemStack gem, LootRarity rarity) {
        if (this.isUnique()) {
            List<Gem> gems = SocketHelper.getGemInstances(socketed).map(GemInstance::gem).map(DynamicHolder::get).toList();
            if (gems.contains(this)) return false;
        }
        return this.isValidIn(socketed, gem, rarity);
    }

    /**
     * Checks if this gem is legally socketed into an item. Does not validate uniques
     *
     * @param socketed The target item.
     * @param rarity   The rarity of the gem.
     * @param gem      The gem
     * @return If this gem can be socketed into the item.
     */
    public boolean isValidIn(ItemStack socketed, ItemStack gem, LootRarity rarity) {
        LootCategory cat = LootCategory.forItem(socketed);
        return !cat.isNone() && this.bonusMap.containsKey(cat) && this.bonusMap.get(cat).supports(rarity);
    }

    /**
     * Optionally returns this gem's bonus for the given category/rarity combination.
     * 
     * @param cat    The category
     * @param rarity The rarity
     * @return If a bonus exists for the inputs, an {@link Optional} holding it, otherwise {@link Optional#empty()}.
     */
    public Optional<GemBonus> getBonus(LootCategory cat, LootRarity rarity) {
        return Optional.ofNullable(this.bonusMap.get(cat)).filter(b -> b.supports(rarity));
    }

    @Override
    public String toString() {
        return String.format("Gem: %s", this.getId());
    }

    public static String fmt(float f) {
        return Affix.fmt(f);
    }

    @Override
    public float getQuality() {
        return this.quality;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public Set<ResourceLocation> getDimensions() {
        return this.dimensions;
    }

    @Override
    public LootRarity getMaxRarity() {
        return this.maxRarity;
    }

    @Override
    public LootRarity getMinRarity() {
        return this.minRarity;
    }

    public List<GemBonus> getBonuses() {
        return this.bonuses;
    }

    public boolean isUnique() {
        return this.unique;
    }

    public Gem validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.dimensions);
        Preconditions.checkArgument(this.maxRarity.ordinal() >= this.minRarity.ordinal());
        RarityRegistry.INSTANCE.getValues().stream().filter(r -> r.isAtLeast(this.minRarity) && r.isAtMost(this.maxRarity)).forEach(r -> {
            Preconditions.checkArgument(this.bonuses.stream().allMatch(b -> b.supports(r)));
        });
        return this;
    }

    @Override
    public Set<String> getStages() {
        return this.stages;
    }

    @Override
    public Codec<? extends Gem> getCodec() {
        return CODEC;
    }

    public static void addTypeInfo(Consumer<Component> list, Object... types) {
        Arrays.sort(types, (c1, c2) -> ((LootCategory) c1).getName().compareTo(((LootCategory) c2).getName()));
        Style style = Style.EMPTY.withColor(0x0AFF0A);
        if (types.length != LootCategory.BY_ID.size() - 1) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < types.length) {
                int rem = Math.min(3, types.length - i);
                Object[] args = new Object[rem];
                for (int r = 0; r < rem; r++) {
                    sb.append("%s, ");
                    args[r] = Component.translatable(((LootCategory) types[i + r]).getDescIdPlural());
                }
                list.accept(Component.translatable("text.apotheosis.dot_prefix", Component.translatable(sb.substring(0, sb.length() - 2), args)).withStyle(style));
                sb.setLength(0);
                i += rem;
            }
        }
        else {
            list.accept(Component.translatable("text.apotheosis.dot_prefix", Component.translatable("text.apotheosis.anything")).withStyle(style));
        }
    }

    public final ResourceLocation getId() {
        return GemRegistry.INSTANCE.getKey(this);
    }

}
