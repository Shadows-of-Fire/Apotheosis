package shadows.apotheosis.deadly;

import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.AffixLootEntry;
import shadows.apotheosis.deadly.affix.LootRarity;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.ench.EnchModule;
import shadows.placebo.loot.LootSystem;
import shadows.placebo.loot.PoolBuilder;
import shadows.placebo.loot.StackLootEntry;
import shadows.placebo.util.ChestBuilder;
import shadows.placebo.util.ChestBuilder.EnchantedEntry;

/**
 * Loot entries for deadly module
 * TODO: Make configurable.
 * @author Shadows
 *
 */
public class DeadlyLoot {

	public static final ResourceLocation BRUTAL = new ResourceLocation(Apotheosis.MODID, "spawner_brutal");
	public static final ResourceLocation BRUTAL_ROTATE = new ResourceLocation(Apotheosis.MODID, "spawner_brutal_rotate");
	public static final ResourceLocation SWARM = new ResourceLocation(Apotheosis.MODID, "spawner_swarm");
	public static final ResourceLocation VALUABLE = new ResourceLocation(Apotheosis.MODID, "chest_valuable");
	public static final ResourceLocation TOME_TOWER = new ResourceLocation(Apotheosis.MODID, "tome_tower");

	public static void init() {
		PoolBuilder build = new PoolBuilder(5, 8);
		build.bonusRolls(1, 3);
		build.addEntries(ChestBuilder.loot(Items.SKELETON_SKULL, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.WITHER_SKELETON_SKULL, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.CREEPER_HEAD, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.ZOMBIE_HEAD, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.PLAYER_HEAD, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Blocks.TNT, 1, 1, 2, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 1, 3, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.EMERALD, 1, 3, 3, 6));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 1, 5, 10, 3));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 1, 5, 10, 4));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 1, 1, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 1, 1, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 1, 3, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.ANVIL, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.ENCHANTING_TABLE, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.IRON_BLOCK, 1, 1, 3, 0));
		build.addEntries(new EnchantedEntry(Items.ENCHANTED_BOOK, 3));
		build.addEntries(new AffixEntry(8, 5));
		LootSystem.registerLootTable(BRUTAL, LootSystem.tableBuilder().addLootPool(build).build());

		build = new PoolBuilder(5, 8);
		build.bonusRolls(1, 3);
		build.addEntries(ChestBuilder.loot(Items.SKELETON_SKULL, 1, 2, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.WITHER_SKELETON_SKULL, 1, 2, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.CREEPER_HEAD, 1, 2, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.ZOMBIE_HEAD, 1, 2, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.PLAYER_HEAD, 1, 2, 1, 2));
		build.addEntries(ChestBuilder.loot(Blocks.TNT, 1, 2, 2, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 1, 4, 3, 5));
		build.addEntries(ChestBuilder.loot(Items.EMERALD, 1, 4, 3, 6));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 1, 7, 10, 3));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 1, 7, 10, 4));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 1, 2, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 1, 2, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 1, 2, 5, 0));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 1, 2, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 1, 2, 1, 5));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 1, 5, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.ANVIL, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.ENCHANTING_TABLE, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.IRON_BLOCK, 1, 3, 3, 0));
		build.addEntries(new EnchantedEntry(Items.BOOK, 3));
		build.addEntries(new AffixEntry(8, 5));
		build.addEntries(new AffixEntry(8, 8));
		LootSystem.registerLootTable(BRUTAL_ROTATE, LootSystem.tableBuilder().addLootPool(build).build());

		build = new PoolBuilder(5, 6);
		build.bonusRolls(1, 4);
		build.addEntries(ChestBuilder.loot(egg("creeper"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("skeleton"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("spider"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("zombie"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("slime"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("enderman"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("cave_spider"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(egg("silverfish"), 1, 3, 1, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 1, 3, 3, 4));
		build.addEntries(ChestBuilder.loot(Items.EMERALD, 1, 3, 3, 4));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 1, 5, 10, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 1, 5, 10, 3));
		build.addEntries(ChestBuilder.loot(Items.GOLDEN_APPLE, 1, 1, 1, 2));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 1, 1, 5, 1));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 1, 1, 3, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 1, 1, 1, 3));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 1, 3, 3, 0));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.ANVIL, 1, 1, 3, 0));
		build.addEntries(ChestBuilder.loot(Blocks.OBSIDIAN, 3, 8, 3, 0));
		build.addEntries(new EnchantedEntry(Items.BOOK, 3));
		build.addEntries(new AffixEntry(8, 5));
		LootSystem.registerLootTable(SWARM, LootSystem.tableBuilder().addLootPool(build).build());

		build = new PoolBuilder(6, 12);
		build.bonusRolls(2, 5);
		build.addEntries(ChestBuilder.loot(potion(Potions.STRONG_REGENERATION), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(Potions.STRONG_SWIFTNESS), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(Potions.LONG_FIRE_RESISTANCE), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(Items.SPLASH_POTION, Potions.STRONG_HEALING), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(Potions.LONG_NIGHT_VISION), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(Potions.LONG_STRENGTH), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(Potions.LONG_INVISIBILITY), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(potion(Potions.LONG_WATER_BREATHING), 1, 1, 20, 10));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND, 1, 3, 30, 4));
		build.addEntries(ChestBuilder.loot(Items.EMERALD, 1, 3, 30, 4));
		build.addEntries(ChestBuilder.loot(Items.IRON_INGOT, 1, 5, 100, 1));
		build.addEntries(ChestBuilder.loot(Items.GOLD_INGOT, 1, 5, 100, 3));
		build.addEntries(ChestBuilder.loot(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1, 15));
		build.addEntries(ChestBuilder.loot(Items.NAME_TAG, 1, 2, 50, 1));
		build.addEntries(ChestBuilder.loot(Items.LEAD, 1, 2, 50, 1));
		build.addEntries(ChestBuilder.loot(Items.SADDLE, 1, 2, 40, 1));
		build.addEntries(ChestBuilder.loot(Items.DIAMOND_HORSE_ARMOR, 1, 1, 40, 3));
		build.addEntries(ChestBuilder.loot(Items.SLIME_BALL, 3, 6, 50, 0));
		build.addEntries(ChestBuilder.loot(Items.BUCKET, 1, 1, 50, 0));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_SWORD, 30));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_AXE, 30));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_PICKAXE, 30));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_BOOTS, 20));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_LEGGINGS, 20));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_HELMET, 20));
		build.addEntries(new EnchantedEntry(Items.DIAMOND_CHESTPLATE, 20));
		build.addEntries(new EnchantedEntry(Items.BOOK, 20));
		build.addEntries(new AffixEntry(20, 15));
		LootSystem.registerLootTable(VALUABLE, LootSystem.tableBuilder().addLootPool(build).build());

		if (Apotheosis.enableEnch) {
			build = new PoolBuilder(6, 9);
			build.bonusRolls(0, 3);
			for (Item i : EnchModule.TYPED_BOOKS)
				build.addEntries(new TomeEntry(i, 5));
			build.addEntries(new EnchantedEntry(Items.BOOK, 5));
			for (int i = 0; i < 5; i++)
				build.addEntries(ChestBuilder.loot(DeadlyModule.RARITY_TOMES.get(LootRarity.values()[i]), 1, 1, 16 - 3 * i, 10));
			build.addEntries(new AffixEntry(20, 35));
			LootSystem.registerLootTable(TOME_TOWER, LootSystem.tableBuilder().addLootPool(build).build());
		}
	}

	private static ItemStack egg(String mob) {
		return new ItemStack(SpawnEggItem.EGGS.get(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mob))));
	}

	private static ItemStack potion(Potion type) {
		return potion(Items.POTION, type);
	}

	private static ItemStack potion(Item pot, Potion type) {
		ItemStack s = new ItemStack(pot);
		PotionUtils.addPotionToItemStack(s, type);
		return s;
	}

	public static class AffixEntry extends StackLootEntry {

		public static final Serializer SERIALIZER = new Serializer();
		public static final LootPoolEntryType AFFIX_TYPE = Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(Apotheosis.MODID, "affix_entry"), new LootPoolEntryType(SERIALIZER));

		public AffixEntry(int weight, int quality) {
			super(ItemStack.EMPTY, 1, 1, weight, quality);
		}

		@Override
		protected void func_216154_a(Consumer<ItemStack> list, LootContext ctx) {
			LootRarity rarity = LootRarity.random(ctx.getRandom());
			AffixLootEntry entry = AffixLootManager.getRandomEntry(ctx.getRandom());
			ItemStack stack = entry.getStack().copy();
			stack.getTag().putBoolean("apoth_rchest", true);
			list.accept(AffixLootManager.genLootItem(stack, ctx.getRandom(), entry.getType(), rarity));
		}

		public static class Serializer extends StandaloneLootEntry.Serializer<AffixEntry> {

			@Override
			protected AffixEntry deserialize(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, ILootCondition[] lootConditions, ILootFunction[] lootFunctions) {
				return new AffixEntry(weight, quality);
			}

		}
	}

	public static class TomeEntry extends EnchantedEntry {

		public TomeEntry(Item i, int weight) {
			super(i, weight);
		}

		@Override
		protected void func_216154_a(Consumer<ItemStack> list, LootContext ctx) {
			ItemStack enchTome = this.func.apply(new ItemStack(this.i), ctx);
			ItemStack ench = new ItemStack(Items.ENCHANTED_BOOK);
			EnchantmentHelper.getEnchantments(enchTome).entrySet().stream().map(e -> new EnchantmentData(e.getKey(), e.getValue())).forEach(d -> EnchantedBookItem.addEnchantment(ench, d));
			list.accept(ench);
		}

	}

}