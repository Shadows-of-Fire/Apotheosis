package shadows.apotheosis.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.ench.EnchantedTrigger;

public class AdvancementTriggers {

	public static final EnchantedTrigger ENCHANTED_ITEM = new EnchantedTrigger();
	public static final SplittingTrigger SPLIT_BOOK = new SplittingTrigger();
	public static final ModifierTrigger SPAWNER_MODIFIER = new ModifierTrigger();

	public static void init() {
		CriteriaTriggers.REGISTRY.remove(new ResourceLocation("inventory_changed"));
		CriteriaTriggers.INVENTORY_CHANGED = CriteriaTriggers.register(new ExtendedInvTrigger());
		CriteriaTriggers.register(AdvancementTriggers.SPAWNER_MODIFIER);
		CriteriaTriggers.register(AdvancementTriggers.ENCHANTED_ITEM);
		CriteriaTriggers.register(AdvancementTriggers.SPLIT_BOOK);
	}

}