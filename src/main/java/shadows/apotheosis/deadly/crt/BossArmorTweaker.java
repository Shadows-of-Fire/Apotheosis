package shadows.apotheosis.deadly.crt;
/*
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.actions.IAction;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;

import crafttweaker.api.item.WeightedItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.util.ArmorSet;
import shadows.apotheosis.util.ArmorSet.WeightedRandomStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.apotheosis.BossArmor")
@ZenRegister
public class BossArmorTweaker {

	@ZenMethod
	public static void removeSet(String name) {
		CraftTweakerAPI.apply(new IAction() {

			@Override
			public void apply() {
				ArmorSet.unregister(new ResourceLocation(name));
			}

			@Override
			public String describe() {
				return String.format("Attempting to remove armor set with name %s.", name);
			}

		});
	}

	@ZenMethod
	public static void addArmorSet(int level, String name, IItemStack mainhand, IItemStack offhand, IItemStack feet, IItemStack legs, IItemStack chest, IItemStack head) {
		CraftTweakerAPI.apply(new IAction() {

			@Override
			public void apply() {
				ArmorSet set = new ArmorSet(new ResourceLocation(name), level, CraftTweakerMC.getItemStacks(mainhand, offhand, feet, legs, chest, head));
				ArmorSet.register(set);
			}

			@Override
			public String describe() {
				return String.format("Registered armor set %s.", name);
			}

		});
	}

	@ZenMethod
	public static void addPossibleWeapons(String name, WeightedItemStack... stacks) {
		ArmorSet set = ArmorSet.getByName(new ResourceLocation(name));
		if (set == null) {
			CraftTweakerAPI.logError(String.format("Attempted to add possible weapons for set %s, but it does not exist.", name));
			return;
		}
		CraftTweakerAPI.apply(new IAction() {

			@Override
			public void apply() {
				set.setupList();
				for (WeightedItemStack s : stacks) {
					set.getPossibleMainhands().add(new WeightedRandomStack(CraftTweakerMC.getItemStack(s.getStack()), (int) (s.getPercent() * 100)));
				}
			}

			@Override
			public String describe() {
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("Added potential mainhands to ArmorSet %s.\n", name));
				for (WeightedItemStack s : stacks) {
					sb.append(String.format("Item %s, Weight %s; ", s.getStack().toCommandString(), (int) (s.getPercent() * 100)));
				}
				return sb.toString();
			}

		});
	}

}
*/