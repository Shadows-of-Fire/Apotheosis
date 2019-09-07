package shadows.potion;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class InvisCharmItem extends Item {

	public InvisCharmItem() {
		super(new Item.Properties().maxStackSize(1).maxDamage(64).group(ItemGroup.MISC));
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		if (entity instanceof ServerPlayerEntity && ((ServerPlayerEntity) entity).getActivePotionEffect(Effects.INVISIBILITY) == null) {
			((ServerPlayerEntity) entity).addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 200));
			if (stack.attemptDamageItem(1, world.rand, ((ServerPlayerEntity) entity))) stack.shrink(1);
		}
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

}
