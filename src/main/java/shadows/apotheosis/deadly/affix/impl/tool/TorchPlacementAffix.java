package shadows.apotheosis.deadly.affix.impl.tool;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

/**
 * Allows the user to place torches from the tool, for a durability cost.
 */
public class TorchPlacementAffix extends Affix {

	public TorchPlacementAffix(int weight) {
		super(weight);
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		int duraCost = 4 + rand.nextInt(5);
		if (modifier != null) duraCost = (int) modifier.editLevel(this, duraCost);
		return 9 - duraCost;
	}

	@Override
	public float getMin() {
		return 4;
	}

	@Override
	public float getMax() {
		return 8;
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		return Math.min(curLvl, newLvl);
	}

	@Override
	public float obliterateLevel(float level) {
		return Math.min(9, level * 2);
	}

	@Override
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.PICKAXE || type == EquipmentType.SHOVEL;
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