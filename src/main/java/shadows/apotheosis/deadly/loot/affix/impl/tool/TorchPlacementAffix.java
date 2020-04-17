package shadows.apotheosis.deadly.loot.affix.impl.tool;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;

/**
 * Allows the user to place torches from the tool, for a durability cost.
 */
public class TorchPlacementAffix extends Affix {

	public TorchPlacementAffix(int weight) {
		super(weight);
	}

	@Override
	public float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		int duraCost = 4 + rand.nextInt(5);
		if (modifier != null) duraCost = (int) modifier.editLevel(this, duraCost);
		AffixHelper.addLore(stack, new TranslationTextComponent("affix." + this.getRegistryName() + ".desc", 9 - duraCost));
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
	public boolean canApply(EquipmentType type) {
		return type == EquipmentType.PICKAXE || type == EquipmentType.SHOVEL;
	}

	@Override
	public boolean onBlockClicked(PlayerEntity user, World world, BlockPos pos, Direction dir, Hand hand, float level) {
		ItemUseContext ctx = new ItemUseContext(world, user, Hand.MAIN_HAND, new ItemStack(Items.TORCH), new BlockRayTraceResult(new Vec3d(0.5, 0.5, 0.5), dir, pos, false)) {
		};
		if (Items.TORCH.onItemUse(ctx).shouldSwingHand()) {
			user.getHeldItem(hand).damageItem((int) level, user, (p_220042_0_) -> {
				p_220042_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);
			});
			return true;
		}
		return false;
	}

}
