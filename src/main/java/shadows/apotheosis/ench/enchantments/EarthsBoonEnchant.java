package shadows.apotheosis.ench.enchantments;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import shadows.apotheosis.ench.EnchModule;

public class EarthsBoonEnchant extends Enchantment {

	public EarthsBoonEnchant() {
		super(Rarity.VERY_RARE, EnchModule.PICKAXE, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinCost(int level) {
		return 60 + (level - 1) * 20;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return 200;
	}

	@Override
	public ITextComponent getFullname(int level) {
		return ((IFormattableTextComponent) super.getFullname(level)).withStyle(TextFormatting.DARK_GREEN);
	}

	public void provideBenefits(BreakEvent e) {
		PlayerEntity player = e.getPlayer();
		ItemStack stack = player.getMainHandItem();
		int level = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
		if (player.level.isClientSide) return;
		if (e.getState().is(Tags.Blocks.STONE) && level > 0 && player.random.nextFloat() <= 0.01F * level) {
			ItemStack newDrop = new ItemStack(EnchModule.BOON_DROPS.getRandomElement(player.random));
			Block.popResource(player.level, e.getPos(), newDrop);
		}
	}
}