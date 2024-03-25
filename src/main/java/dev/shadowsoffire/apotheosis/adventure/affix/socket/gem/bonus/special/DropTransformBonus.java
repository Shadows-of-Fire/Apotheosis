package dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.special;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.IngredientCodec;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import dev.shadowsoffire.placebo.util.StepFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class DropTransformBonus extends GemBonus {

    public static Codec<DropTransformBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            PlaceboCodecs.nullableField(TagKey.codec(Registries.BLOCK), "blocks").forGetter(a -> a.tag),
            IngredientCodec.INSTANCE.fieldOf("inputs").forGetter(a -> a.inputs),
            ItemAdapter.CODEC.fieldOf("output").forGetter(a -> a.output),
            VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.descKey))
        .apply(inst, DropTransformBonus::new));

    /**
     * Input blocks this transformation triggers on.<br>
     * If no tag is provided, this works on all blocks, as long as a block was broken.
     */
    protected final Optional<TagKey<Block>> tag;
    /**
     * List of input items merged as an ingredient.
     */
    protected final Ingredient inputs;
    /**
     * Output item. Each replaced stack will be cloned with this stack, with the same size as the original.
     */
    protected final ItemStack output;
    /**
     * Rarity -> Chance map.
     */
    protected final Map<LootRarity, StepFunction> values;
    protected final String descKey;

    public DropTransformBonus(GemClass gemClass, Optional<TagKey<Block>> tag, Ingredient inputs, ItemStack output, Map<LootRarity, StepFunction> values, String descKey) {
        super(Apotheosis.loc("drop_transform"), gemClass);
        this.tag = tag;
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
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        float chance = this.values.get(rarity).min();
        return Component.translatable(this.descKey, Affix.fmt(chance * 100)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void modifyLoot(ItemStack gem, LootRarity rarity, ObjectArrayList<ItemStack> loot, LootContext ctx) {
        if (ctx.hasParam(LootContextParams.BLOCK_STATE)) {
            BlockState state = ctx.getParam(LootContextParams.BLOCK_STATE);
            if (this.tag.isPresent() && !state.is(this.tag.get())) return;
            if (ctx.getRandom().nextFloat() <= this.values.get(rarity).min()) {
                for (int i = 0; i < loot.size(); i++) {
                    ItemStack stack = loot.get(i);
                    if (this.inputs.test(stack)) {
                        ItemStack outCopy = this.output.copy();
                        outCopy.setCount(stack.getCount());
                        loot.set(i, outCopy);
                    }
                }
            }
        }
    }

    @Override
    public DropTransformBonus validate() {
        Preconditions.checkNotNull(this.values);
        this.values.forEach((k, v) -> {
            Preconditions.checkNotNull(k);
            Preconditions.checkNotNull(v);
        });
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
}
