package shadows.apotheosis.deadly.loot.affix;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class AffixHelper {

	public static final String AFFIXES = "Affixes";

	public static void applyAffix(ItemStack stack, Affix affix, float level) {
		NBTTagCompound tag = stack.getOrCreateSubCompound(AFFIXES);
		tag.setFloat(affix.getRegistryName().toString(), level);
	}

	public static Map<Affix, Float> getAffixes(ItemStack stack) {
		Map<Affix, Float> map = new HashMap<>();
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(AFFIXES)) {
			NBTTagCompound tag = stack.getTagCompound().getCompoundTag(AFFIXES);
			for (String key : tag.getKeySet()) {
				Affix affix = Affix.REGISTRY.getValue(new ResourceLocation(key));
				float lvl = tag.getFloat(key);
				map.put(affix, lvl);
			}
		}
		return map;
	}

	public static void addLore(ItemStack stack, String lore) {
		NBTTagCompound display = stack.getOrCreateSubCompound("display");
		NBTTagList tag = display.getTagList("Lore", 8);
		tag.appendTag(new NBTTagString(lore));
		display.setTag("Lore", tag);
	}

}
