package shadows.apotheosis.deadly.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.gen.WorldFeature.WorldFeatureItem;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.util.ArmorSet;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.util.AttributeHelper;
import shadows.placebo.util.PlaceboUtil;

/**
 * Setup information for bosses.
 * @author Shadows
 *
 */
public class BossItem extends WorldFeatureItem {

	//Default lists of boss potions/enchantments.
	public static final List<Effect> POTIONS = new ArrayList<>();
	public static final List<Enchantment> BOW_ENCHANTMENTS = PlaceboUtil.asList(Enchantments.LOOTING, Enchantments.UNBREAKING, Enchantments.POWER, Enchantments.PUNCH, Enchantments.FLAME, Enchantments.INFINITY);
	public static final List<Enchantment> SWORD_ENCHANTMENTS = PlaceboUtil.asList(Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.LOOTING);
	public static final List<Enchantment> TOOL_ENCHANTMENTS = PlaceboUtil.asList(Enchantments.EFFICIENCY, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING, Enchantments.FORTUNE);
	public static final List<Enchantment> ARMOR_ENCHANTMENTS = PlaceboUtil.asList(Enchantments.PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FEATHER_FALLING, Enchantments.BLAST_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.RESPIRATION, Enchantments.AQUA_AFFINITY, Enchantments.THORNS, Enchantments.UNBREAKING);

	//Default gear sets.
	public static final ArmorSet CHAIN_GEAR = new ArmorSet(0, Items.STONE_SWORD, Items.SHIELD, Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET).addExtraMains(Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_PICKAXE);
	public static final ArmorSet GOLD_GEAR = new ArmorSet(1, Items.GOLDEN_SWORD, Items.SHIELD, Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_HELMET).addExtraMains(Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE);
	public static final ArmorSet IRON_GEAR = new ArmorSet(2, Items.IRON_SWORD, Items.SHIELD, Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET).addExtraMains(Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_PICKAXE);
	public static final ArmorSet DIAMOND_GEAR = new ArmorSet(3, Items.DIAMOND_SWORD, Items.SHIELD, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET).addExtraMains(Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE);

	static {
		ArmorSet.LEVEL_TO_SETS.put(0, CHAIN_GEAR);
		ArmorSet.LEVEL_TO_SETS.put(1, GOLD_GEAR);
		ArmorSet.LEVEL_TO_SETS.put(2, IRON_GEAR);
		ArmorSet.LEVEL_TO_SETS.put(3, DIAMOND_GEAR);
	}

	public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal && ((NearestAttackableTargetGoal<?>) a).targetClass == VillagerEntity.class;

	protected final EntityType<?> entityEntry;
	protected AxisAlignedBB entityAABB;

	public BossItem(int weight, ResourceLocation entity) {
		super(weight);
		entityEntry = ForgeRegistries.ENTITIES.getValue(entity);
		Preconditions.checkNotNull(entityEntry, "Invalid BossItem (not an entity) created with reloc: " + entity);
	}

	public AxisAlignedBB getAABB(IWorld world) {
		if (entityAABB == null) entityAABB = entityEntry.create(world.getWorld()).getCollisionBoundingBox();
		if (entityAABB == null) entityAABB = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
		return entityAABB;
	}

	@Override
	public void place(IWorld world, BlockPos pos) {
		place(world, pos, world.getRandom());
	}

	public void place(IWorld world, BlockPos pos, Random rand) {
		MobEntity entity = (MobEntity) entityEntry.create(world.getWorld());
		initBoss(rand, entity);
		entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
		world.addEntity(entity);
		entity.goalSelector.goals.removeIf(IS_VILLAGER_ATTACK);
		entity.enablePersistence();
		for (BlockPos p : BlockPos.getAllInBoxMutable(pos.add(-2, -1, -2), pos.add(2, 1, 2))) {
			world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
		}
		for (BlockPos p : BlockPos.getAllInBoxMutable(pos.add(-2, -2, -2), pos.add(2, -2, 2))) {
			world.setBlockState(p, Blocks.RED_SANDSTONE.getDefaultState(), 2);
		}
		entity.getPersistentData().putBoolean("apoth_boss", true);
		WorldGenerator.debugLog(pos, "Boss " + entity.getName().getUnformattedComponentText());
	}

	public static void initBoss(Random random, MobEntity entity) {
		int duration = entity instanceof CreeperEntity ? 6000 : Integer.MAX_VALUE;
		if (DeadlyConfig.bossRegenLevel > 0) entity.addPotionEffect(new EffectInstance(Effects.REGENERATION, duration, DeadlyConfig.bossRegenLevel));
		if (DeadlyConfig.bossResistLevel > 0) entity.addPotionEffect(new EffectInstance(Effects.RESISTANCE, duration, DeadlyConfig.bossResistLevel));
		if (DeadlyConfig.bossFireRes) entity.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, duration));
		if (DeadlyConfig.bossWaterBreathing) entity.addPotionEffect(new EffectInstance(Effects.WATER_BREATHING, duration));
		AttributeHelper.addToBase(entity, SharedMonsterAttributes.ATTACK_DAMAGE, "boss_damage_bonus", DeadlyConfig.bossDamageBonus);
		AttributeHelper.multiplyFinal(entity, SharedMonsterAttributes.MAX_HEALTH, "boss_health_mult", DeadlyConfig.bossHealthMultiplier - 1);
		AttributeHelper.max(entity, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, "boss_knockback_resist", DeadlyConfig.bossKnockbackResist);
		AttributeHelper.multiplyFinal(entity, SharedMonsterAttributes.MOVEMENT_SPEED, "boss_speed_mult", DeadlyConfig.bossSpeedMultiplier - 1);
		entity.setHealth(entity.getMaxHealth());
		String name = NameHelper.setEntityName(random, entity);
		entity.enablePersistence();

		int level = 0;
		for (int i = 0; i < ArmorSet.SORTED_SETS.size() - 1; i++)
			if (random.nextDouble() < DeadlyConfig.bossLevelUpChance) level++;

		ArmorSet.SORTED_SETS.get(level).apply(entity);

		if (entity instanceof SkeletonEntity) entity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.BOW));

		int guaranteed = random.nextInt(6);

		ItemStack stack = entity.getItemStackFromSlot(EquipmentSlotType.values()[guaranteed]);
		while (guaranteed == 1 || stack.isEmpty())
			stack = entity.getItemStackFromSlot(EquipmentSlotType.values()[guaranteed = random.nextInt(6)]);

		for (EquipmentSlotType s : EquipmentSlotType.values()) {
			if (s.ordinal() == guaranteed) entity.setDropChance(s, 2F);
			else entity.setDropChance(s, ThreadLocalRandom.current().nextFloat());
			ItemStack enchantedItem = stack;

			if (s.ordinal() == guaranteed) {
				List<Enchantment> enchants = EquipmentType.getTypeForStack(stack).getEnchants();
				Enchantment enchantment = enchants.get(random.nextInt(enchants.size()));
				if (enchants.stream().anyMatch(e -> e.canApply(enchantedItem))) while (!enchantment.canApply(stack))
					enchantment = enchants.get(random.nextInt(enchants.size()));
				NameHelper.setItemName(random, stack, name, enchantment);

				for (int i = 0; i < 5; i++)
					addSingleEnchantment(stack, random, 28 + (Apotheosis.enableEnch ? 10 : 3) * i, true);

				addSingleEnchantment(stack, random, Apotheosis.enableEnch ? 150 : 60, true);
				Map<Enchantment, Integer> enchMap = new HashMap<>();
				for (Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
					enchMap.put(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey()), e.getValue() + random.nextInt(2)));
				}
				EnchantmentHelper.setEnchantments(enchMap, stack);
			} else if (random.nextDouble() < DeadlyConfig.bossEnchantChance) {
				EnchantmentHelper.addRandomEnchantment(random, stack, 30 + random.nextInt(Apotheosis.enableEnch ? 50 : 25), true);
			}
		}

		if (POTIONS.isEmpty()) initPotions();

		if (random.nextDouble() < DeadlyConfig.bossPotionChance) entity.addPotionEffect(new EffectInstance(POTIONS.get(random.nextInt(POTIONS.size())), duration, random.nextInt(3) + 1));
	}

	public static void addSingleEnchantment(ItemStack stack, Random rand, int level, boolean treasure) {
		List<EnchantmentData> datas = EnchantmentHelper.buildEnchantmentList(rand, stack, level, treasure);
		if (datas.isEmpty()) return;
		EnchantmentData d = datas.get(rand.nextInt(datas.size()));
		stack.addEnchantment(d.enchantment, d.enchantmentLevel);
	}

	public static void initPotions() {
		for (Effect p : ForgeRegistries.POTIONS)
			if (p.isBeneficial() && !p.isInstant()) POTIONS.add(p);
		POTIONS.removeIf(p -> DeadlyConfig.BLACKLISTED_POTIONS.contains(p.getRegistryName()));
	}

	public static enum EquipmentType {
		SWORD(SWORD_ENCHANTMENTS),
		BOW(BOW_ENCHANTMENTS),
		TOOL(TOOL_ENCHANTMENTS),
		ARMOR(ARMOR_ENCHANTMENTS);

		final List<Enchantment> enchants;

		EquipmentType(List<Enchantment> enchants) {
			this.enchants = enchants;
		}

		public List<Enchantment> getEnchants() {
			return enchants;
		}

		public static EquipmentType getTypeForStack(ItemStack stack) {
			Item i = stack.getItem();
			if (i instanceof SwordItem) return SWORD;
			if (i instanceof BowItem) return BOW;
			if (i instanceof ArmorItem) return ARMOR;
			return TOOL;
		}
	}
}