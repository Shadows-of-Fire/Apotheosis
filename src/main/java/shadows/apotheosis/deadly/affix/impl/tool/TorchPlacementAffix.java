package shadows.apotheosis.deadly.affix.impl.tool;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Allows the user to place torches from the tool, for a durability cost.
 */
public class TorchPlacementAffix extends RangedAffix {

	public TorchPlacementAffix(LootRarity rarity, int min, int max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		return Math.round(super.generateLevel(stack, rand, modifier));
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return Math.min(curLvl, newLvl);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) { return lootCategory == LootCategory.BREAKER; }

	@Override
	public boolean isPrefix() {
		return false;
	}

	@Override
	public InteractionResult onItemUse(UseOnContext ctx, float level) {
		Player player = ctx.getPlayer();
		if (Items.TORCH.useOn(ctx).consumesAction()) {
			ctx.getItemInHand().grow(1);
			player.getItemInHand(ctx.getHand()).hurtAndBreak((int) level, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
			return InteractionResult.SUCCESS;
		}
		return null;
	}

}