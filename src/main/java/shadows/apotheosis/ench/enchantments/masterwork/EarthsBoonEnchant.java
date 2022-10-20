package shadows.apotheosis.ench.enchantments.masterwork;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.ench.EnchModule;

public class EarthsBoonEnchant extends Enchantment {

	public EarthsBoonEnchant() {
		super(Rarity.VERY_RARE, EnchModule.PICKAXE, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
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
	public Component getFullname(int level) {
		return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_GREEN);
	}

	public void provideBenefits(BreakEvent e) {
		Player player = e.getPlayer();
		ItemStack stack = player.getMainHandItem();
		int level = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
		if (player.level.isClientSide) return;
		if (e.getState().is(Tags.Blocks.STONE) && level > 0 && player.random.nextFloat() <= 0.01F * level) {
			ItemStack newDrop = new ItemStack(ForgeRegistries.ITEMS.tags().getTag(Apoth.Tags.BOON_DROPS).getRandomElement(player.random).orElse(Items.AIR));
			Block.popResource(player.level, e.getPos(), newDrop);
		}
	}
}