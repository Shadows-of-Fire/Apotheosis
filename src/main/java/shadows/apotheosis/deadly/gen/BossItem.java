package shadows.apotheosis.deadly.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.gen.WorldFeature.WorldFeatureItem;
import shadows.apotheosis.deadly.loot.LootManager;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.util.ArmorSet;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.util.AttributeHelper;

/**
 * Setup information for bosses.
 * @author Shadows
 *
 */
public class BossItem extends WorldFeatureItem {

	//Default lists of boss potions/enchantments.
	public static final List<Effect> POTIONS = new ArrayList<>();

	//Default gear sets.
	public static final ArmorSet GOLD_GEAR = new ArmorSet(new ResourceLocation(Apotheosis.MODID, "gold"), 0, Items.GOLDEN_SWORD, Items.SHIELD, Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_HELMET).addExtraMains(Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE);
	public static final ArmorSet IRON_GEAR = new ArmorSet(new ResourceLocation(Apotheosis.MODID, "iron"), 1, Items.IRON_SWORD, Items.SHIELD, Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET).addExtraMains(Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_PICKAXE);
	public static final ArmorSet DIAMOND_GEAR = new ArmorSet(new ResourceLocation(Apotheosis.MODID, "diamond"), 2, Items.DIAMOND_SWORD, Items.SHIELD, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET).addExtraMains(Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE);

	static {
		ArmorSet.register(GOLD_GEAR);
		ArmorSet.register(IRON_GEAR);
		ArmorSet.register(DIAMOND_GEAR);
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
		WorldGenerator.debugLog(pos, "Boss " + entity.getName().getUnformattedComponentText());
	}

	public static void initBoss(Random random, MobEntity entity) {
		int duration = entity instanceof CreeperEntity ? 6000 : Integer.MAX_VALUE;
		int regen = DeadlyConfig.bossRegenLevel.generateInt(random) - 1;
		if (regen >= 0) entity.addPotionEffect(new EffectInstance(Effects.REGENERATION, duration, regen));
		int res = DeadlyConfig.bossResistLevel.generateInt(random) - 1;
		if (res >= 0) entity.addPotionEffect(new EffectInstance(Effects.RESISTANCE, duration, res));
		if (random.nextFloat() < DeadlyConfig.bossFireRes) entity.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, duration));
		if (random.nextFloat() < DeadlyConfig.bossWaterBreathing) entity.addPotionEffect(new EffectInstance(Effects.WATER_BREATHING, duration));
		AttributeHelper.multiplyFinal(entity, SharedMonsterAttributes.ATTACK_DAMAGE, "boss_damage_bonus", DeadlyConfig.bossHealthMultiplier.generateFloat(random) - 1);
		AttributeHelper.multiplyFinal(entity, SharedMonsterAttributes.MAX_HEALTH, "boss_health_mult", DeadlyConfig.bossHealthMultiplier.generateFloat(random) - 1);
		AttributeHelper.addToBase(entity, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, "boss_knockback_resist", DeadlyConfig.bossKnockbackResist.generateFloat(random));
		AttributeHelper.multiplyFinal(entity, SharedMonsterAttributes.MOVEMENT_SPEED, "boss_speed_mult", DeadlyConfig.bossSpeedMultiplier.generateFloat(random) - 1);
		entity.setHealth(entity.getMaxHealth());
		String name = NameHelper.setEntityName(random, entity);
		entity.enablePersistence();

		int level = 0;
		while (random.nextDouble() <= DeadlyConfig.bossLevelUpChance && level <= ArmorSet.getMaxLevel())
			level++;
		ArmorSet.getSetFor(level, random).apply(entity);

		if (entity instanceof SkeletonEntity) entity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.BOW));

		int guaranteed = random.nextInt(6);

		ItemStack stack = entity.getItemStackFromSlot(EquipmentSlotType.values()[guaranteed]);
		while (guaranteed == 1 || stack.isEmpty())
			stack = entity.getItemStackFromSlot(EquipmentSlotType.values()[guaranteed = random.nextInt(6)]);

		for (EquipmentSlotType s : EquipmentSlotType.values()) {
			if (s.ordinal() == guaranteed) entity.setDropChance(s, 2F);
			else entity.setDropChance(s, ThreadLocalRandom.current().nextFloat() / 2);
			if (s.ordinal() == guaranteed) {
				entity.setItemStackToSlot(s, modifyBossItem(stack, random, name));
			} else if (random.nextDouble() < DeadlyConfig.bossEnchantChance) {
				List<EnchantmentData> ench = EnchantmentHelper.buildEnchantmentList(random, stack, 30 + random.nextInt(Apotheosis.enableEnch ? 20 : 10), true);
				EnchantmentHelper.setEnchantments(ench.stream().collect(Collectors.toMap(d -> d.enchantment, d -> d.enchantmentLevel, (v1, v2) -> v1 > v2 ? v1 : v2, HashMap::new)), stack);
			}
		}

		if (POTIONS.isEmpty()) initPotions();

		if (random.nextDouble() < DeadlyConfig.bossPotionChance) entity.addPotionEffect(new EffectInstance(POTIONS.get(random.nextInt(POTIONS.size())), duration, random.nextInt(3) + 1));
	}

	public static void initPotions() {
		for (Effect p : ForgeRegistries.POTIONS)
			if (p.isBeneficial() && !p.isInstant()) POTIONS.add(p);
		POTIONS.removeIf(p -> DeadlyConfig.BLACKLISTED_POTIONS.contains(p.getRegistryName()));
	}

	public static ItemStack modifyBossItem(ItemStack stack, Random random, String bossName) {
		List<EnchantmentData> ench = EnchantmentHelper.buildEnchantmentList(random, stack, Apotheosis.enableEnch ? 60 : 30, true);
		EnchantmentHelper.setEnchantments(ench.stream().collect(Collectors.toMap(d -> d.enchantment, d -> d.enchantmentLevel, (a, b) -> a > b ? a : b)), stack);
		String itemName = NameHelper.setItemName(random, stack, bossName);
		stack.setDisplayName(new StringTextComponent(itemName));
		LootRarity rarity = LootRarity.random(random, 400);
		stack = LootManager.genLootItem(stack, random, rarity);
		stack.setDisplayName(new StringTextComponent(rarity.getColor() + bossName + "'s " + stack.getDisplayName()));
		Map<Enchantment, Integer> enchMap = new HashMap<>();
		for (Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			if (e.getKey() != null) enchMap.put(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey()), e.getValue() + random.nextInt(2)));
		}
		EnchantmentHelper.setEnchantments(enchMap, stack);
		return stack;
	}

	public static enum EquipmentType {
		SWORD(s -> EquipmentSlotType.MAINHAND),
		BOW(s -> EquipmentSlotType.MAINHAND),
		TOOL(s -> EquipmentSlotType.MAINHAND),
		ARMOR(s -> ((ArmorItem) s.getItem()).getEquipmentSlot()),
		SHIELD(s -> EquipmentSlotType.OFFHAND);

		final Function<ItemStack, EquipmentSlotType> type;

		EquipmentType(Function<ItemStack, EquipmentSlotType> type) {
			this.type = type;
		}

		public EquipmentSlotType getSlot(ItemStack stack) {
			return this.type.apply(stack);
		}

		public static EquipmentType getTypeFor(ItemStack stack) {
			Item i = stack.getItem();
			if (i instanceof SwordItem) return SWORD;
			if (i instanceof BowItem) return BOW;
			if (i instanceof ArmorItem) return ARMOR;
			if (i instanceof ShieldItem) return SHIELD;
			return TOOL;
		}
	}
}