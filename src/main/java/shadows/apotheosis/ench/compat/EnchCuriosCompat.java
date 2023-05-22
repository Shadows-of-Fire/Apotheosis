package shadows.apotheosis.ench.compat;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import shadows.apotheosis.Apoth;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class EnchCuriosCompat {

	private static final Predicate<ItemStack> HAS_LIFE_MEND = stack -> EnchantmentHelper.getItemEnchantmentLevel(Apoth.Enchantments.LIFE_MENDING, stack) > 0;

	public static List<ItemStack> getLifeMendingCurios(LivingEntity entity) {
		List<SlotResult> slots = CuriosApi.getCuriosHelper().findCurios(entity, HAS_LIFE_MEND);
		return slots.stream().map(SlotResult::stack).toList();
	}

}