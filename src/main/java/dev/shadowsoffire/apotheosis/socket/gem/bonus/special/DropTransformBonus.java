package dev.shadowsoffire.apotheosis.socket.gem.bonus.special;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class DropTransformBonus extends GemBonus {

    public static Codec<DropTransformBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            ContextAwarePredicate.CODEC.fieldOf("conditions").forGetter(a -> a.conditions),
            Ingredient.CODEC.fieldOf("inputs").forGetter(a -> a.inputs),
            ItemStackTemplate.CODEC.fieldOf("output").forGetter(a -> a.output),
            Purity.mapCodec(Codec.floatRange(0, 1)).fieldOf("values").forGetter(a -> a.values),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.descKey))
        .apply(inst, DropTransformBonus::new));

    protected final ContextAwarePredicate conditions;

    /**
     * List of input items merged as an ingredient.
     */
    protected final Ingredient inputs;

    /**
     * Output item. Each replaced stack will be cloned with this template, with the same size as the original.
     */
    protected final ItemStackTemplate output;

    /**
     * Rarity -> Chance map.
     */
    protected final Map<Purity, Float> values;
    protected final String descKey;

    public DropTransformBonus(GemClass gemClass, ContextAwarePredicate conditions, Ingredient inputs, ItemStackTemplate output, Map<Purity, Float> values, String descKey) {
        super(gemClass);
        this.conditions = conditions;
        this.inputs = inputs;
        this.output = output;
        this.values = values;
        this.descKey = descKey;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
        float chance = this.values.get(inst.purity());
        return Component.translatable(this.descKey, Affix.fmt(chance * 100)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void modifyLoot(GemInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {
        if (this.conditions.matches(ctx)) {
            if (ctx.getRandom().nextFloat() <= this.values.get(inst.purity())) {
                for (int i = 0; i < loot.size(); i++) {
                    ItemStack stack = loot.get(i);
                    if (this.inputs.test(stack)) {
                        ItemStack outCopy = this.output.create();
                        outCopy.setCount(stack.getCount());
                        loot.set(i, outCopy);
                    }
                }
            }
        }
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GemBonus.Builder {
        private final List<LootItemCondition> conditions = new ArrayList<>();
        private final Map<Purity, Float> values = new HashMap<>();
        private Ingredient inputs;
        private ItemStackTemplate output;
        private String descKey;

        public Builder condition(LootItemCondition condition) {
            this.conditions.add(condition);
            return this;
        }

        public Builder inputs(Ingredient inputs) {
            this.inputs = inputs;
            return this;
        }

        public Builder output(ItemStackTemplate output) {
            this.output = output;
            return this;
        }

        public Builder output(net.minecraft.world.level.ItemLike item) {
            this.output = new ItemStackTemplate(item.asItem());
            return this;
        }

        public Builder value(Purity purity, float chance) {
            if (chance < 0 || chance > 1) {
                throw new IllegalArgumentException("Chance must be between 0 and 1");
            }
            this.values.put(purity, chance);
            return this;
        }

        public Builder desc(String descKey) {
            this.descKey = descKey;
            return this;
        }

        @Override
        public DropTransformBonus build(GemClass gemClass) {
            ContextAwarePredicate predicate = ContextAwarePredicate.create(this.conditions.toArray(new LootItemCondition[0]));
            return new DropTransformBonus(gemClass, predicate, this.inputs, this.output, this.values, this.descKey);
        }
    }

}
