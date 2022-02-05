package shadows.apotheosis.spawn.enchantment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.spawn.SpawnerModule;

public class CapturingEnchant extends Enchantment {

	public CapturingEnchant() {
		super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public int getMinCost(int level) {
		return 28 + (level - 1) * 15;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 15;
	}

	public void handleCapturing(LivingDropsEvent e) {
		Entity killer = e.getSource().getEntity();
		if (killer instanceof LivingEntity) {
			int level = EnchantmentHelper.getItemEnchantmentLevel(Apoth.Enchantments.CAPTURING, ((LivingEntity) killer).getMainHandItem());
			LivingEntity killed = e.getEntityLiving();
			if (SpawnerModule.bannedMobs.contains(killed.getType().getRegistryName())) return;
			if (killed.level.random.nextFloat() < level / 250F) {
				ItemStack egg = new ItemStack(SpawnEggItem.BY_ID.get(killed.getType()));
				e.getDrops().add(new ItemEntity(killed.level, killed.getX(), killed.getY(), killed.getZ(), egg));
			}
		}
	}

}