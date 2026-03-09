package dev.shadowsoffire.apotheosis.socket.gem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.BuiltInRegs;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.ExtraGemBonusRegistry.ExtraGemBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class Gem implements CodecProvider<Gem>, Weighted, Constrained {

    public static final Codec<Gem> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        TieredWeights.CODEC.fieldOf("weights").forGetter(Weighted::weights),
        Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(Constrained::constraints),
        Purity.CODEC.optionalFieldOf("min_purity", Purity.CRACKED).forGetter(Gem::getMinPurity),
        GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(Gem::getBonuses),
        Codec.BOOL.optionalFieldOf("unique", false).forGetter(Gem::isUnique))
        .apply(inst, Gem::new));

    protected final TieredWeights weights;
    protected final Constraints constraints;
    protected final Purity minPurity;
    protected final List<GemBonus> bonuses;
    protected final boolean unique;

    protected transient final Map<LootCategory, GemBonus> bonusMap = new IdentityHashMap<>();
    protected transient final List<GemBonus> extraBonuses = new ArrayList<>();

    public Gem(TieredWeights weights, Constraints constraints, Purity minPurity, List<GemBonus> bonuses, boolean unique) {
        this.weights = weights;
        this.constraints = constraints;
        this.minPurity = minPurity;
        this.bonuses = bonuses;
        this.unique = unique;
        Preconditions.checkArgument(!bonuses.isEmpty(), "No bonuses were provided.");
        for (GemBonus bonus : this.bonuses) {
            validateBonus(bonus);
            for (Holder<LootCategory> category : bonus.getGemClass().types()) {
                this.bonusMap.put(category.value(), bonus);
            }
        }
    }

    /**
     * Adds all tooltip data from this gem to the gem stack.
     *
     * @param gem      The gem stack.
     * @param purity   The purity of this gem.
     * @param tooltips The destination for tooltips.
     */
    public void addInformation(GemView gem, Consumer<Component> list, AttributeTooltipContext ctx) {
        if (this.isUnique()) {
            list.accept(Component.translatable("text.apotheosis.unique").withStyle(Style.EMPTY.withColor(0xC73912)));
            list.accept(CommonComponents.EMPTY);
        }
        Style style = Style.EMPTY.withColor(0x0AFF0A);
        list.accept(Component.translatable("text.apotheosis.socketable_into").withStyle(style));
        addTypeInfo(list, this.bonusMap.keySet().toArray());
        list.accept(CommonComponents.EMPTY);

        list.accept(Component.translatable("text.apotheosis.when_socketed_in").withStyle(ChatFormatting.GOLD));
        Consumer<GemBonus> appendBonusToTooltip = bonus -> {
            if (bonus.supports(gem.purity())) {
                Component modifComp = bonus.getSocketBonusTooltip(gem, ctx);
                Component sum = Component.translatable("text.apotheosis.dot_prefix", Component.translatable("%s: %s", Component.translatable("gem_class." + bonus.getGemClass().key()), modifComp)).withStyle(ChatFormatting.GOLD);
                list.accept(sum);
            }
        };

        this.bonuses.forEach(appendBonusToTooltip);
        this.extraBonuses.forEach(appendBonusToTooltip);
    }

    /**
     * Checks if this gem can be applied to an item, preventing more than one unique.
     *
     * @param socketed The target item.
     * @param rarity   The rarity of the gem.
     * @param gem      The gem
     * @return If this gem can be socketed into the item.
     */
    public boolean canApplyTo(ItemStack socketed, ItemStack gem, Purity purity) {
        if (this.isUnique()) {
            List<Gem> gems = SocketHelper.getGems(socketed).streamValidGems().map(GemInstance::gem).map(DynamicHolder::get).toList();
            if (gems.contains(this)) {
                return false;
            }
        }
        return this.isValidIn(socketed, gem, purity);
    }

    /**
     * Checks if this gem is legally socketed into an item. Does not validate uniques
     *
     * @param socketed The target item.
     * @param rarity   The rarity of the gem.
     * @param gem      The gem
     * @return If this gem can be socketed into the item.
     */
    public boolean isValidIn(ItemStack socketed, ItemStack gem, Purity purity) {
        LootCategory cat = LootCategory.forItem(socketed);
        return !cat.isNone() && this.bonusMap.containsKey(cat) && this.bonusMap.get(cat).supports(purity);
    }

    /**
     * Optionally returns this gem's bonus for the given category/rarity combination.
     *
     * @param cat    The category
     * @param rarity The rarity
     * @return If a bonus exists for the inputs, an {@link Optional} holding it, otherwise {@link Optional#empty()}.
     */
    public Optional<GemBonus> getBonus(LootCategory cat, Purity purity) {
        return Optional.ofNullable(this.bonusMap.get(cat)).filter(b -> b.supports(purity));
    }

    @Nullable
    public GemBonus getBonus(LootCategory cat) {
        return this.bonusMap.get(cat);
    }

    @Override
    public String toString() {
        return String.format("Gem: %s", this.getId());
    }

    @Override
    public TieredWeights weights() {
        return this.weights;
    }

    @Override
    public Constraints constraints() {
        return this.constraints;
    }

    public Purity getMinPurity() {
        return this.minPurity;
    }

    /**
     * @deprecated The internal structure of gem bonuses is no longer represented by this list.
     *             Gems may have additional bonuses stapled to them by {@link ExtraGemBonus}.
     *             Use {@link #getBonus(LootCategory, Purity)}
     */
    @Deprecated(forRemoval = true)
    public List<GemBonus> getBonuses() {
        return this.bonuses;
    }

    public boolean isUnique() {
        return this.unique;
    }

    @Override
    public Codec<? extends Gem> getCodec() {
        return CODEC;
    }

    public final ResourceLocation getId() {
        return GemRegistry.INSTANCE.getKey(this);
    }

    /**
     * Creates a new {@link ItemStack} containing this {@link Gem}.
     * <p>
     * The provided purity will be automatically clamped based on {@link Gem#getMinPurity()}.
     */
    public ItemStack toStack(Purity purity) {
        ItemStack stack = new ItemStack(Items.GEM);
        GemItem.setGem(stack, this);
        GemItem.setPurity(stack, Purity.max(purity, this.getMinPurity()));
        return stack;
    }

    public static String fmt(float f) {
        return Affix.fmt(f);
    }

    public static void addTypeInfo(Consumer<Component> list, Object... types) {
        Arrays.sort(types, (c1, c2) -> ((LootCategory) c1).getKey().compareTo(((LootCategory) c2).getKey()));
        Style style = Style.EMPTY.withColor(0x0AFF0A);
        if (types.length < BuiltInRegs.LOOT_CATEGORY.size() - 1) {
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

    /**
     * Checks if a bonus can be added to this Gem.
     *
     * @throws IllegalArgumentException if the bonus cannot be added.
     */
    private void validateBonus(GemBonus bonus) {
        for (Holder<LootCategory> category : bonus.getGemClass().types()) {
            if (this.bonusMap.containsKey(category.value())) {
                GemBonus conflict = this.bonusMap.get(category.value());
                throw new IllegalArgumentException("Gem Bonus for class %s conflicts with existing bonus for class %s (categories overlap)".formatted(bonus.getGemClass().key(), conflict.getGemClass().key()));
            }
        }
    }

    void appendExtraBonus(GemBonus bonus) {
        validateBonus(bonus);
        this.extraBonuses.add(bonus);
        for (Holder<LootCategory> category : bonus.getGemClass().types()) {
            this.bonusMap.put(category.value(), bonus);
        }
    }

    public static class Builder {

        protected final TieredWeights weights;
        protected Constraints constraints = Constraints.EMPTY;
        protected Purity minPurity = Purity.CRACKED;
        protected List<GemBonus> bonuses = new ArrayList<>();
        protected boolean unique = false;

        public Builder(TieredWeights weights) {
            this.weights = weights;
        }

        public Builder contstraints(Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public Builder minPurity(Purity purity) {
            this.minPurity = purity;
            return this;
        }

        public Builder bonus(LootCategory cat, GemBonus.Builder builder) {
            return this.bonus(new GemClass(cat), builder);
        }

        public Builder bonus(GemClass gClass, GemBonus.Builder builder) {
            this.bonuses.add(builder.build(gClass));
            return this;
        }

        public Builder bonus(GemBonus bonus) {
            this.bonuses.add(bonus);
            return this;
        }

        public Builder unique() {
            this.unique = true;
            return this;
        }

        public Gem build() {
            return new Gem(this.weights, this.constraints, this.minPurity, this.bonuses, this.unique);
        }
    }

}
