package shadows.apotheosis.adventure.affix.socket.gem.bonus.special;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.socket.gem.GemClass;
import shadows.apotheosis.adventure.affix.socket.gem.GemManager;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.codec.PlaceboCodecs.IngredientCodec;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.util.StepFunction;

public class DropTransformBonus extends GemBonus {

    public static Codec<DropTransformBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            TagKey.codec(Registry.BLOCK_REGISTRY).fieldOf("blocks").forGetter(a -> a.tag),
            IngredientCodec.INSTANCE.fieldOf("inputs").forGetter(a -> a.inputs),
            ItemAdapter.CODEC.fieldOf("output").forGetter(a -> a.output),
            VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.descKey))
        .apply(inst, DropTransformBonus::new));

    /**
     * Input blocks this transformation triggers on.<br>
     * If the tag is empty, this works on all blocks, as long as a block was broken.<br>
     * If none of the builtin tags are sufficient, you will have to make a new tag.
     */
    protected final TagKey<Block> tag;
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

    protected final transient List<Block> blocks;

    public DropTransformBonus(GemClass gemClass, TagKey<Block> tag, Ingredient inputs, ItemStack output, Map<LootRarity, StepFunction> values, String descKey) {
        super(Apotheosis.loc("drop_transform"), gemClass);
        this.tag = tag;
        this.inputs = inputs;
        this.output = output;
        this.values = values;
        this.descKey = descKey;
        if (EffectiveSide.get().isServer()) {
            this.blocks = GemManager.INSTANCE._getContext().getTag(tag).stream().map(Holder::get).toList();
        }
        else this.blocks = Collections.emptyList();
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
            Block block = ctx.getParam(LootContextParams.BLOCK_STATE).getBlock();
            if (!this.blocks.isEmpty() && !this.blocks.contains(block)) return;
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
