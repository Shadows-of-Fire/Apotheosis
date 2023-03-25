package shadows.apotheosis.adventure.boss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.GearSet;
import shadows.apotheosis.util.GearSet.SetPredicate;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;
import shadows.placebo.json.RandomAttributeModifier;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

public final class MinibossItem extends TypeKeyedBase<MinibossItem> implements ILuckyWeighted, IDimensional, IStaged {

	protected int weight;
	protected float quality;
	protected String name;
	protected Set<EntityType<?>> entities;
	protected BossStats stats;
	protected @Nullable Set<String> stages;
	protected Set<ResourceLocation> dimensions;
	protected boolean affixed;

	@SerializedName("valid_gear_sets")
	protected List<SetPredicate> armorSets;

	@SerializedName("nbt")
	protected CompoundTag customNbt;

	@SerializedName("supporting_entities")
	protected List<SupportingEntity> supportingEntities;

	protected SupportingEntity mount;

	public MinibossItem() {
		// No ctor, not meant to be created via code
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

	public Set<EntityType<?>> getEntities() {
		return this.entities;
	}

	/**
	 * Generates (but does not spawn) the result of this BossItem.
	 * @param world The world to create the entity in.
	 * @param pos The location to place the entity.  Will be centered (+0.5, +0.5).
	 * @param random A random, used for selection of boss stats.
	 * @return The newly created boss.
	 */
	public void transform(Mob mob, RandomSource random, float luck) {
		if (this.customNbt != null) mob.load(this.customNbt);
		this.initBoss(random, mob, luck);
		// Re-read here so we can apply certain things after the boss has been modified
		// But only mob-specific things, not a full load()
		if (this.customNbt != null) mob.readAdditionalSaveData(this.customNbt);
	}

	/**
	 * Initializes an entity as a boss, based on the stats of this BossItem.
	 * @param rand
	 * @param mob
	 */
	public void initBoss(RandomSource rand, Mob mob, float luck) {
		int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

		for (ChancedEffectInstance inst : stats.effects) {
			if (rand.nextFloat() <= inst.getChance()) {
				mob.addEffect(inst.createInstance(rand, duration));
			}
		}

		for (RandomAttributeModifier modif : stats.modifiers) {
			modif.apply(rand, mob);
		}

		mob.goalSelector.availableGoals.removeIf(BossItem.IS_VILLAGER_ATTACK);
		String name = NameHelper.setEntityName(rand, mob);

		GearSet set = BossArmorManager.INSTANCE.getRandomSet(rand, luck, this.armorSets);
		set.apply(mob);

		boolean anyValid = false;

		for (EquipmentSlot t : EquipmentSlot.values()) {
			ItemStack s = mob.getItemBySlot(t);
			if (!s.isEmpty() && !LootCategory.forItem(s).isNone()) {
				anyValid = true;
				break;
			}
		}

		if (!anyValid) throw new RuntimeException("Attempted to apply boss gear set " + set.getId() + " but it had no valid affix loot items generated.");

		int guaranteed = rand.nextInt(6);

		ItemStack temp = mob.getItemBySlot(EquipmentSlot.values()[guaranteed]);
		while (temp.isEmpty() || LootCategory.forItem(temp) == LootCategory.NONE) {
			guaranteed = rand.nextInt(6);
			temp = mob.getItemBySlot(EquipmentSlot.values()[guaranteed]);
		}

		for (EquipmentSlot s : EquipmentSlot.values()) {
			ItemStack stack = mob.getItemBySlot(s);
			if (s.ordinal() == guaranteed) mob.setDropChance(s, 2F);
			if (s.ordinal() == guaranteed) {
				//mob.setItemSlot(s, this.modifyBossItem(stack, rand, name, luck, rarity));
				//mob.setCustomName(((MutableComponent) mob.getCustomName()).withStyle(Style.EMPTY.withColor(rarity.color())));
			} else if (rand.nextFloat() < stats.enchantChance) {
				enchantBossItem(rand, stack, Apotheosis.enableEnch ? stats.enchLevels[0] : stats.enchLevels[1], true);
				mob.setItemSlot(s, stack);
			}
		}
		mob.getPersistentData().putBoolean("apoth.boss", true);
		//mob.getPersistentData().putString("apoth.rarity", rarity.id());
		mob.setHealth(mob.getMaxHealth());
		mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 2400));
	}

	public void enchantBossItem(RandomSource rand, ItemStack stack, int level, boolean treasure) {
		List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(rand, stack, level, treasure);
		var map = ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.level, Math::max));
		map.putAll(EnchantmentHelper.getEnchantments(stack));
		EnchantmentHelper.setEnchantments(map, stack);
	}

	public ItemStack modifyBossItem(ItemStack stack, RandomSource rand, String bossName, float luck, LootRarity rarity) {
		enchantBossItem(rand, stack, Apotheosis.enableEnch ? stats.enchLevels[2] : stats.enchLevels[3], true);

		NameHelper.setItemName(rand, stack);
		stack = LootController.createLootItem(stack, LootCategory.forItem(stack), rarity, rand);

		String bossOwnerName = String.format(NameHelper.ownershipFormat, bossName) + " ";
		Component name = AffixHelper.getName(stack);
		if (name.getContents() instanceof TranslatableContents tc) {
			Component copy = Component.translatable(bossOwnerName + tc.getKey(), tc.getArgs()).withStyle(name.getStyle());
			AffixHelper.setName(stack, copy);
		}

		Map<Enchantment, Integer> enchMap = new HashMap<>();
		for (Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			if (e.getKey() != null) enchMap.put(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey()), e.getValue() + rand.nextInt(2)));
		}

		if (AdventureConfig.curseBossItems) {
			final ItemStack stk = stack; //Lambda rules require this instead of a direct reference to stack
			List<Enchantment> curses = ForgeRegistries.ENCHANTMENTS.getValues().stream().filter(e -> e.canApplyAtEnchantingTable(stk) && e.isCurse()).collect(Collectors.toList());
			if (!curses.isEmpty()) {
				Enchantment curse = curses.get(rand.nextInt(curses.size()));
				enchMap.put(curse, Mth.nextInt(rand, 1, EnchHooks.getMaxLevel(curse)));
			}
		}

		EnchantmentHelper.setEnchantments(enchMap, stack);
		stack.getTag().putBoolean("apoth_boss", true);
		return stack;
	}

	/**
	 * Ensures that this boss item does not have null or empty fields that would cause a crash.
	 * @return this
	 */
	public MinibossItem validate() {
		/*
		Preconditions.checkArgument(this.weight >= 0, "Boss Item " + this.id + " has a negative weight!");
		Preconditions.checkArgument(this.quality >= 0, "Boss Item " + this.id + " has a negative quality!");
		Preconditions.checkNotNull(this.entity, "Boss Item " + this.id + " has null entity type!");
		Preconditions.checkNotNull(this.size, "Boss Item " + this.id + " has no size!");
		if (this.minRarity != null) {
			Preconditions.checkArgument(this.maxRarity == null || this.maxRarity.isAtLeast(this.minRarity));
		}
		if (this.maxRarity != null) {
			Preconditions.checkArgument(this.minRarity == null || this.maxRarity.isAtLeast(this.minRarity));
		}
		LootRarity r = LootRarity.COMMON.max(this.minRarity);
		while (r != LootRarity.ANCIENT) {
			Preconditions.checkNotNull(this.stats.get(r));
			if (r == this.maxRarity) break;
			r = LootRarity.LIST.get(r.ordinal() + 1);
		}
		*/
		return this;
	}

	@Override
	public Set<ResourceLocation> getDimensions() {
		return this.dimensions;
	}

	@Override
	public Set<String> getStages() {
		return this.stages;
	}

	public static class SupportingEntity {
		EntityType<?> entity;
		CompoundTag nbt;
		double x, y, z;

		public Entity create(Level level, double x, double y, double z) {
			Entity ent = this.entity.create(level);
			if (this.nbt != null) ent.deserializeNBT(this.nbt);
			ent.setPos(this.x + x, this.y + y, this.z + z);
			return ent;
		}
	}

}