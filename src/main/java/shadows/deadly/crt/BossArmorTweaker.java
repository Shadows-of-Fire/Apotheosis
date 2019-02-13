package shadows.deadly.crt;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.WeightedItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import shadows.util.ArmorSet;
import shadows.util.ArmorSet.WeightedRandomStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.apotheosis.BossArmor")
@ZenRegister
public class BossArmorTweaker {

	@ZenMethod
	public static void removeArmorSet(int level) {
		CraftTweakerAPI.apply(new IAction() {

			@Override
			public void apply() {
				ArmorSet.LEVEL_TO_SETS.remove(level);
			}

			@Override
			public String describe() {
				return String.format("Removed armor set for level %s.", level);
			}

		});
	}

	@ZenMethod
	public static void addArmorSet(int level, IItemStack mainhand, IItemStack offhand, IItemStack feet, IItemStack legs, IItemStack chest, IItemStack head) {
		CraftTweakerAPI.apply(new IAction() {

			@Override
			public void apply() {
				ArmorSet set = new ArmorSet(level, CraftTweakerMC.getItemStacks(mainhand, offhand, feet, legs, chest, head));
				ArmorSet.LEVEL_TO_SETS.put(level, set);
			}

			@Override
			public String describe() {
				return String.format("Added armor set for level %s.", level);
			}

		});
	}

	@ZenMethod
	public static void addPossibleWeapons(int level, WeightedItemStack... stacks) {
		if (ArmorSet.LEVEL_TO_SETS == null) {
			CraftTweakerAPI.logError(String.format("Attempted to add possible weapons for level %s, but there is no set at that level!", level));
			return;
		}
		CraftTweakerAPI.apply(new IAction() {

			@Override
			public void apply() {
				ArmorSet set = ArmorSet.LEVEL_TO_SETS.get(level);
				set.setupList();
				for (WeightedItemStack s : stacks) {
					set.getPossibleMainhands().add(new WeightedRandomStack(CraftTweakerMC.getItemStack(s.getStack()), (int) (s.getPercent() * 100)));
				}
			}

			@Override
			public String describe() {
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("Added potential mainhands to Armor set level %s.\n", level));
				for (WeightedItemStack s : stacks) {
					sb.append(String.format("Item %s, Weight %s; ", s.getStack().toCommandString(), (int) (s.getPercent() * 100)));
				}
				return sb.toString();
			}

		});
	}

}
