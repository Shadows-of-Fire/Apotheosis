package shadows.deadly.crt;

import java.util.List;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.enchantments.IEnchantmentDefinition;
import net.minecraft.enchantment.Enchantment;
import shadows.deadly.gen.BossItem;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.apotheosis.Boss")
@ZenRegister
public class BossTweaker {

	@ZenMethod
	public static void addBowEnchantment(IEnchantmentDefinition enchant) {
		Enchantment e = checkError(BossItem.BOW_ENCHANTMENTS, enchant);
		if (e != null) CraftTweakerAPI.apply(new AddEnchAction("Bow Enchantments", BossItem.BOW_ENCHANTMENTS, e));
	}

	@ZenMethod
	public static void addSwordEnchantment(IEnchantmentDefinition enchant) {
		Enchantment e = checkError(BossItem.SWORD_ENCHANTMENTS, enchant);
		if (e != null) CraftTweakerAPI.apply(new AddEnchAction("Sword Enchantments", BossItem.SWORD_ENCHANTMENTS, e));
	}

	@ZenMethod
	public static void addToolEnchantment(IEnchantmentDefinition enchant) {
		Enchantment e = checkError(BossItem.TOOL_ENCHANTMENTS, enchant);
		if (e != null) CraftTweakerAPI.apply(new AddEnchAction("Tool Enchantments", BossItem.TOOL_ENCHANTMENTS, e));
	}

	@ZenMethod
	public static void addArmorEnchantment(IEnchantmentDefinition enchant) {
		Enchantment e = checkError(BossItem.ARMOR_ENCHANTMENTS, enchant);
		if (e != null) CraftTweakerAPI.apply(new AddEnchAction("Armor Enchantments", BossItem.ARMOR_ENCHANTMENTS, e));
	}

	private static Enchantment checkError(List<Enchantment> target, IEnchantmentDefinition ench) {
		Enchantment e = ench == null ? null : (Enchantment) ench.getInternal();
		if (e == null) CraftTweakerAPI.logError("Attempted to add a null enchantment to a Boss Enchantment List!");
		if (e != null && target.contains(e)) {
			CraftTweakerAPI.logError("Attempted to add a duplicate enchantment to a Boss Enchantment List!");
			return null;
		}
		return e;
	}

	private static class AddEnchAction implements IAction {

		String listName;
		List<Enchantment> target;
		Enchantment toAdd;

		AddEnchAction(String listName, List<Enchantment> target, Enchantment toAdd) {
			this.listName = listName;
			this.target = target;
			this.toAdd = toAdd;
		}

		@Override
		public void apply() {
			target.add(toAdd);
		}

		@Override
		public String describe() {
			return String.format("Added enchantment %s to Boss Enchantment List %s", toAdd.getRegistryName(), listName);
		}

	}

}
