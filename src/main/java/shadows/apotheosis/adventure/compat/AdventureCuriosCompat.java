package shadows.apotheosis.adventure.compat;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class AdventureCuriosCompat {

    private static final Predicate<ItemStack> HAS_LIFE_MEND = stack -> stack.getEnchantmentLevel(Apoth.Enchantments.LIFE_MENDING.get()) > 0;

    public static List<ItemStack> getLifeMendingCurios(LivingEntity entity) {
        List<SlotResult> slots = CuriosApi.getCuriosHelper().findCurios(entity, HAS_LIFE_MEND);
        return slots.stream().map(SlotResult::stack).toList();
    }

}
