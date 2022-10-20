package shadows.apotheosis.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.loot.AffixLootEntry;
import shadows.apotheosis.adventure.loot.AffixLootManager;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public class MiscDatagenCode {

	public static void genAffixLootItems() {
		Gson gson = AffixLootManager.GSON;
		Set<ResourceLocation> overworld = ImmutableSet.of(new ResourceLocation("overworld"));
		Set<ResourceLocation> nether = ImmutableSet.of(new ResourceLocation("the_nether"));
		Set<ResourceLocation> end = ImmutableSet.of(new ResourceLocation("the_end"));

		BiConsumer<String, AffixLootEntry> writerFunc = (dim, entry) -> {
			if (entry.getType() == LootCategory.NONE) return;
			File file = new File(FMLPaths.GAMEDIR.get().toFile(), "datagen/" + dim + "/" + ForgeRegistries.ITEMS.getKey(entry.getStack().getItem()).getPath() + ".json");
			file.getParentFile().mkdirs();
			try (FileWriter writer = new FileWriter(file)) {
				gson.toJson(entry, writer);
			} catch (IOException ex) {

			}
		};

		for (Item i : ForgeRegistries.ITEMS) {
			if (i instanceof TieredItem t) {
				Tiers tier = (Tiers) t.getTier();
				if (tier != Tiers.WOOD && tier.ordinal() <= Tiers.IRON.ordinal()) {
					AffixLootEntry entry = new AffixLootEntry(100 - 15 * (tier.ordinal() - 1), 1 + 3 * (tier.ordinal() - 1), new ItemStack(i), LootCategory.forItem(new ItemStack(i)), overworld, null, LootRarity.RARE);
					writerFunc.accept("overworld", entry);
				}

				if (tier.ordinal() >= Tiers.IRON.ordinal() && tier.ordinal() <= Tiers.DIAMOND.ordinal() || tier == Tiers.GOLD) {
					int weight = tier == Tiers.GOLD ? 85 : 100 - 30 * (tier.ordinal() - 2);
					int quality = tier == Tiers.GOLD ? 5 : 1 + 5 * (tier.ordinal() - 2);
					AffixLootEntry entry = new AffixLootEntry(weight, quality, new ItemStack(i), LootCategory.forItem(new ItemStack(i)), nether, LootRarity.UNCOMMON, LootRarity.EPIC);
					writerFunc.accept("the_nether", entry);
				}

				if (tier == Tiers.DIAMOND || tier == Tiers.NETHERITE) {
					int weight = tier == Tiers.DIAMOND ? 100 : 70;
					int quality = tier == Tiers.DIAMOND ? 5 : 10;
					AffixLootEntry entry = new AffixLootEntry(weight, quality, new ItemStack(i), LootCategory.forItem(new ItemStack(i)), end, LootRarity.RARE, LootRarity.MYTHIC);
					writerFunc.accept("the_end", entry);
				}
			} else if (i instanceof ArmorItem a && a.getMaterial() instanceof ArmorMaterials) {
				ArmorMaterials mat = (ArmorMaterials) a.getMaterial();
				if (mat.ordinal() <= ArmorMaterials.IRON.ordinal()) {
					AffixLootEntry entry = new AffixLootEntry(100 - 15 * (mat.ordinal()), 1 + 2 * (mat.ordinal()), new ItemStack(i), LootCategory.ARMOR, overworld, null, LootRarity.RARE);
					writerFunc.accept("overworld", entry);
				}

				if (mat.ordinal() >= ArmorMaterials.IRON.ordinal() && mat.ordinal() <= ArmorMaterials.DIAMOND.ordinal()) {
					AffixLootEntry entry = new AffixLootEntry(100 - 15 * (mat.ordinal() - 2), 1 + 2 * (mat.ordinal() - 2), new ItemStack(i), LootCategory.ARMOR, nether, LootRarity.UNCOMMON, LootRarity.EPIC);
					writerFunc.accept("the_nether", entry);
				}

				if (mat == ArmorMaterials.DIAMOND || mat == ArmorMaterials.NETHERITE) {
					int weight = mat == ArmorMaterials.DIAMOND ? 100 : 70;
					int quality = mat == ArmorMaterials.DIAMOND ? 5 : 10;
					AffixLootEntry entry = new AffixLootEntry(weight, quality, new ItemStack(i), LootCategory.ARMOR, end, LootRarity.RARE, LootRarity.MYTHIC);
					writerFunc.accept("the_end", entry);
				}
			}
		}
	}

}
