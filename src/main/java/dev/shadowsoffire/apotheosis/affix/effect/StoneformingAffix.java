package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * The Stoneforming affix allows a breaker to convert any blocks from the candidate set to any other block in it.
 * <p>
 * If a block drops as a different item (i.e. stone -> cobblestone), both blocks should be in the candidate set.
 */
public class StoneformingAffix extends Affix {

    public static final Codec<StoneformingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("candidates").forGetter(a -> a.candidates))
        .apply(inst, StoneformingAffix::new));

    public static final Component TOOLTIP_MARKER = Component.literal("APOTH_STONEFORMING_MARKER");

    protected final Set<LootCategory> categories;
    protected final HolderSet<Block> candidates;

    public StoneformingAffix(AffixDefinition definition, Set<LootCategory> categories, HolderSet<Block> candidates) {
        super(definition);
        this.categories = categories;
        this.candidates = candidates;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return true;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return this.categories.contains(cat);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return TOOLTIP_MARKER.copy();
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        return super.getDescription(inst, ctx);
    }

    @Override
    public void modifyLoot(AffixInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {
        if (ctx.hasParam(LootContextParams.BLOCK_STATE)) {
            Block block = ctx.getParam(LootContextParams.BLOCK_STATE).getBlock();
            if (isCandidate(block)) {
                // If this action broke a candidate block, try to find that item in the loot list and do the replacement.
                for (int i = 0; i < loot.size(); i++) {
                    ItemStack stack = loot.get(i);
                    if (stack.getItem() instanceof BlockItem bi) {
                        Block lootBlock = bi.getBlock();
                        if (isCandidate(lootBlock)) {
                            loot.set(i, stack.transmuteCopy(this.getTarget(inst)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult onItemUse(AffixInstance inst, UseOnContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        Block block = state.getBlock();
        if (isCandidate(block) && getTarget(inst) != block && ctx.getPlayer().isShiftKeyDown()) {
            if (!ctx.getLevel().isClientSide) {
                inst.stack().set(Components.STONEFORMING_TARGET, block);
                ctx.getPlayer().sendSystemMessage(Apotheosis.lang("affix", "stoneforming.target_updated", block.getName()));
            }
            return InteractionResult.SUCCESS;
        }

        return super.onItemUse(inst, ctx);
    }

    protected boolean isCandidate(Block block) {
        return this.candidates.contains(BuiltInRegistries.BLOCK.wrapAsHolder(block));
    }

    public Block getTarget(AffixInstance inst) {
        Block target = inst.stack().get(Components.STONEFORMING_TARGET);
        if (target == null || !isCandidate(target)) {
            return this.candidates.size() > 0 ? this.candidates.get(0).value() : Blocks.AIR;
        }
        return target;
    }

    public HolderSet<Block> getCandidates() {
        return this.candidates;
    }

}
