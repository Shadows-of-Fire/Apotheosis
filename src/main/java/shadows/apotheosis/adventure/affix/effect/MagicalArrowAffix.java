package shadows.apotheosis.adventure.affix.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public class MagicalArrowAffix extends Affix {

	public MagicalArrowAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		LootCategory cat = LootCategory.forItem(stack);
		return cat != null && cat.isRanged() && rarity.isAtLeast(LootRarity.EPIC);
	}

	// EventPriority.HIGH
	public void onHurt(LivingHurtEvent e) {
		if (e.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
			CompoundTag nbt = arrow.getPersistentData().getCompound("apoth.affixes");
			if (nbt.contains(this.getRegistryName().toString())) {
				e.getSource().setMagic();
			}
		}
	}

}
