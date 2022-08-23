package shadows.apotheosis.adventure.boss;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.GearSet;
import shadows.apotheosis.util.GearSet.SetPredicate;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.json.DimWeightedJsonReloadListener.IDimWeighted;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;
import shadows.placebo.json.RandomAttributeModifier;

public class BossItem extends TypeKeyedBase<BossItem> implements IDimWeighted {

	public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal && ((NearestAttackableTargetGoal<?>) a).targetType == Villager.class;

	protected final int weight;
	protected final float quality;
	protected final EntityType<?> entity;
	protected final AABB size;
	@SerializedName("enchant_chance")
	protected final float enchantChance;
	@SerializedName("rarity_offset")
	protected final int rarityOffset;
	/**
	 * The enchantment levels for a specific boss.  Order is {<Generic with EnchModule>, <Generic without>, <Affix with>, <Affix without>}.
	 */
	@SerializedName("enchantment_levels")
	protected final int[] enchLevels;
	protected final List<ChancedEffectInstance> effects;
	@SerializedName("valid_gear_sets")
	protected final List<SetPredicate> armorSets;
	@SerializedName("attribute_modifiers")
	protected final List<RandomAttributeModifier> modifiers;
	@SerializedName("nbt")
	protected final CompoundTag customNbt;
	protected final Set<ResourceLocation> dimensions;

	public BossItem(int weight, float quality, EntityType<?> entity, AABB size, float enchantChance, int rarityOffset, int[] enchLevels, List<ChancedEffectInstance> effects, List<SetPredicate> armorSets, List<RandomAttributeModifier> modifiers, CompoundTag customNbt, Set<ResourceLocation> dimensions) {
		this.weight = weight;
		this.quality = quality;
		this.entity = entity;
		this.size = size;
		this.enchantChance = enchantChance;
		this.rarityOffset = rarityOffset;
		this.enchLevels = enchLevels;
		this.effects = effects;
		this.armorSets = armorSets;
		this.modifiers = modifiers;
		this.customNbt = customNbt;
		this.dimensions = dimensions;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

	public AABB getSize() {
		return this.size;
	}

	public EntityType<?> getEntity() {
		return this.entity;
	}

	/**
	 * Generates (but does not spawn) the result of this BossItem.
	 * @param world The world to create the entity in.
	 * @param pos The location to place the entity.  Will be centered (+0.5, +0.5).
	 * @param rand A random, used for selection of boss stats.
	 * @return The newly created boss.
	 */
	public Mob createBoss(ServerLevelAccessor world, BlockPos pos, Random rand) {
		Mob entity = (Mob) this.entity.create(world.getLevel());
		if (this.customNbt != null) entity.load(this.customNbt);
		this.initBoss(rand, entity);
		// Re-read here so we can apply certain things after the boss has been modified
		// But only mob-specific things, not a full load()
		if (this.customNbt != null) entity.readAdditionalSaveData(this.customNbt);
		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
		return entity;
	}

	/**
	 * Initializes an entity as a boss, based on the stats of this BossItem.
	 * @param rand
	 * @param entity
	 */
	public void initBoss(Random rand, Mob entity) {
		int duration = entity instanceof Creeper ? 6000 : Integer.MAX_VALUE;

		for (ChancedEffectInstance inst : this.effects) {
			if (rand.nextFloat() <= inst.getChance()) {
				entity.addEffect(inst.createInstance(rand, duration));
			}
		}

		for (RandomAttributeModifier modif : this.modifiers) {
			modif.apply(rand, entity);
		}

		entity.goalSelector.availableGoals.removeIf(IS_VILLAGER_ATTACK);
		String name = NameHelper.setEntityName(rand, entity);

		GearSet set = BossArmorManager.INSTANCE.getRandomSet(rand, this.armorSets);
		set.apply(entity);

		boolean anyValid = false;

		for (EquipmentSlot t : EquipmentSlot.values()) {
			ItemStack s = entity.getItemBySlot(t);
			if (!s.isEmpty() && LootCategory.forItem(s) != LootCategory.NONE) {
				anyValid = true;
				break;
			}
		}

		if (!anyValid) throw new RuntimeException("Attempted to apply boss gear set " + set.getId() + " but it had no valid affix loot items generated.");

		int guaranteed = rand.nextInt(6);

		ItemStack temp = entity.getItemBySlot(EquipmentSlot.values()[guaranteed]);
		while (temp.isEmpty() || LootCategory.forItem(temp) == LootCategory.NONE) {
			guaranteed = rand.nextInt(6);
			temp = entity.getItemBySlot(EquipmentSlot.values()[guaranteed]);
		}

		for (EquipmentSlot s : EquipmentSlot.values()) {
			ItemStack stack = entity.getItemBySlot(s);
			if (s.ordinal() == guaranteed) entity.setDropChance(s, 2F);
			else entity.setDropChance(s, 0.03F);
			if (s.ordinal() == guaranteed) {
				entity.setItemSlot(s, this.modifyBossItem(stack, rand, name));
				LootRarity rarity = AffixHelper.getRarity(stack);
				entity.setCustomName(((MutableComponent) entity.getCustomName()).withStyle(Style.EMPTY.withColor(rarity.color())));
			} else if (rand.nextFloat() < this.enchantChance) {
				List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(rand, stack, Apotheosis.enableEnch ? this.enchLevels[0] : this.enchLevels[1], true);
				EnchantmentHelper.setEnchantments(ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.level, Math::max, HashMap::new)), stack);
				entity.setItemSlot(s, stack);
			}
		}
		entity.getPersistentData().putBoolean("apoth.boss", true);
		entity.setHealth(entity.getMaxHealth());
	}

	public ItemStack modifyBossItem(ItemStack stack, Random random, String bossName) {
		List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(random, stack, Apotheosis.enableEnch ? this.enchLevels[2] : this.enchLevels[3], true);
		EnchantmentHelper.setEnchantments(ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.level, Math::max)), stack);
		LootRarity rarity = LootRarity.random(random, this.rarityOffset);
		NameHelper.setItemName(random, stack);
		stack = LootController.createLootItem(stack, LootCategory.forItem(stack), rarity, random);

		String bossOwnerName = String.format(NameHelper.ownershipFormat, bossName) + " ";
		Component name = AffixHelper.getName(stack);
		if (name instanceof TranslatableComponent tc) {
			Component copy = new TranslatableComponent(bossOwnerName + tc.getKey(), tc.getArgs()).withStyle(tc.getStyle());
			AffixHelper.setName(stack, copy);
		}

		Map<Enchantment, Integer> enchMap = new HashMap<>();
		for (Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			if (e.getKey() != null) enchMap.put(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey()), e.getValue() + random.nextInt(2)));
		}

		if (AdventureConfig.curseBossItems) {
			final ItemStack stk = stack; //Lambda rules require this instead of a direct reference to stack
			List<Enchantment> curses = ForgeRegistries.ENCHANTMENTS.getValues().stream().filter(e -> e.canApplyAtEnchantingTable(stk) && e.isCurse()).collect(Collectors.toList());
			if (!curses.isEmpty()) {
				Enchantment curse = curses.get(random.nextInt(curses.size()));
				enchMap.put(curse, Mth.nextInt(random, 1, EnchHooks.getMaxLevel(curse)));
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
	public BossItem validate() {
		Preconditions.checkNotNull(this.entity, "Boss Item " + this.id + " has null entity type!");
		Preconditions.checkNotNull(this.size, "Boss Item " + this.id + " has no size!");
		Preconditions.checkArgument(this.rarityOffset >= 0 && this.rarityOffset < 1000, "Boss Item " + this.id + " has an invalid rarity offset: " + this.rarityOffset);
		Preconditions.checkArgument(this.enchLevels != null && this.enchLevels.length == 4 && Arrays.stream(this.enchLevels).allMatch(i -> i >= 0), "Boss Item " + this.id + " has invalid ench levels: " + this.enchLevels);
		return this;
	}

	@Override
	public Set<ResourceLocation> getDimensions() {
		return this.dimensions;
	}

}