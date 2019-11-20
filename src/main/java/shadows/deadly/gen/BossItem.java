package shadows.deadly.gen;

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

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.Apotheosis;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.gen.WorldFeature.WorldFeatureItem;
import shadows.deadly.loot.LootManager;
import shadows.deadly.loot.LootRarity;
import shadows.ench.asm.EnchHooks;
import shadows.placebo.util.AttributeHelper;
import shadows.util.ArmorSet;
import shadows.util.NameHelper;

/**
 * Setup information for bosses.
 * @author Shadows
 *
 */
public class BossItem extends WorldFeatureItem {

	//Default lists of boss potions/enchantments.
	public static final List<Potion> POTIONS = new ArrayList<>();

	//Default gear sets.
	public static final ArmorSet GOLD_GEAR = new ArmorSet(new ResourceLocation(Apotheosis.MODID, "gold"), 0, Items.GOLDEN_SWORD, Items.SHIELD, Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_HELMET).addExtraMains(Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE);
	public static final ArmorSet IRON_GEAR = new ArmorSet(new ResourceLocation(Apotheosis.MODID, "iron"), 1, Items.IRON_SWORD, Items.SHIELD, Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET).addExtraMains(Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_PICKAXE);
	public static final ArmorSet DIAMOND_GEAR = new ArmorSet(new ResourceLocation(Apotheosis.MODID, "diamond"), 2, Items.DIAMOND_SWORD, Items.SHIELD, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET).addExtraMains(Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE);

	static {
		ArmorSet.register(GOLD_GEAR);
		ArmorSet.register(IRON_GEAR);
		ArmorSet.register(DIAMOND_GEAR);
	}

	public static final Predicate<EntityAITaskEntry> IS_VILLAGER_ATTACK = a -> a.action instanceof EntityAINearestAttackableTarget && ((EntityAINearestAttackableTarget<?>) a.action).targetClass == EntityVillager.class;

	protected final EntityEntry entityEntry;
	protected AxisAlignedBB entityAABB;

	public BossItem(int weight, ResourceLocation entity) {
		super(weight);
		entityEntry = ForgeRegistries.ENTITIES.getValue(entity);
		Preconditions.checkNotNull(entityEntry, "Invalid BossItem (not an entity) created with reloc: " + entity);
		if (!EntityLiving.class.isAssignableFrom(entityEntry.getEntityClass())) throw new RuntimeException("Invalid BossItem (not an EntityLiving) created with class: " + entityEntry.getEntityClass());
	}

	public AxisAlignedBB getAABB(World world) {
		if (entityAABB == null) entityAABB = entityEntry.newInstance(world).getCollisionBoundingBox();
		if (entityAABB == null) entityAABB = Block.FULL_BLOCK_AABB;
		return entityAABB;
	}

	@Override
	public void place(World world, BlockPos pos) {
		place(world, pos, world.rand);
	}

	public void place(World world, BlockPos pos, Random rand) {
		EntityLiving entity = (EntityLiving) entityEntry.newInstance(world);
		initBoss(rand, entity);
		entity.setPositionAndRotation(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
		world.spawnEntity(entity);
		entity.tasks.taskEntries.removeIf(IS_VILLAGER_ATTACK);
		entity.enablePersistence();
		for (BlockPos p : BlockPos.getAllInBox(pos.add(-2, -1, -2), pos.add(2, 1, 2))) {
			world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
		}
		for (BlockPos p : BlockPos.getAllInBox(pos.add(-2, -2, -2), pos.add(2, -2, 2))) {
			world.setBlockState(p, Blocks.RED_SANDSTONE.getDefaultState(), 2);
		}
		WorldGenerator.debugLog(pos, "Boss " + entity.getName());
	}

	public static void initBoss(Random random, EntityLiving entity) {
		int duration = entity instanceof EntityCreeper ? 6000 : Integer.MAX_VALUE;
		int regen = DeadlyConfig.bossRegenLevel.generateInt(random) - 1;
		if (regen >= 0) entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, duration, regen));
		int res = DeadlyConfig.bossResistLevel.generateInt(random) - 1;
		if (res >= 0) entity.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, duration, res));
		if (random.nextFloat() < DeadlyConfig.bossFireRes) entity.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, duration));
		if (random.nextFloat() < DeadlyConfig.bossWaterBreathing) entity.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, duration));
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

		if (entity instanceof EntitySkeleton) entity.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.BOW));

		int guaranteed = random.nextInt(6);

		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.values()[guaranteed]);
		while (guaranteed == 1 || stack.isEmpty())
			stack = entity.getItemStackFromSlot(EntityEquipmentSlot.values()[guaranteed = random.nextInt(6)]);

		for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) {
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

		if (random.nextDouble() < DeadlyConfig.bossPotionChance) entity.addPotionEffect(new PotionEffect(POTIONS.get(random.nextInt(POTIONS.size())), duration, random.nextInt(3) + 1));
	}

	public static void initPotions() {
		for (Potion p : ForgeRegistries.POTIONS)
			if (p.beneficial && !p.isInstant()) POTIONS.add(p);
		POTIONS.removeIf(p -> DeadlyConfig.BLACKLISTED_POTIONS.contains(p.getRegistryName()));
	}

	public static ItemStack modifyBossItem(ItemStack stack, Random random, String bossName) {
		List<EnchantmentData> ench = EnchantmentHelper.buildEnchantmentList(random, stack, Apotheosis.enableEnch ? 60 : 30, true);
		EnchantmentHelper.setEnchantments(ench.stream().collect(Collectors.toMap(d -> d.enchantment, d -> d.enchantmentLevel, (a, b) -> b)), stack);
		String itemName = NameHelper.setItemName(random, stack, bossName);
		stack.setStackDisplayName(itemName);
		LootRarity rarity = LootRarity.random(random, 500);
		stack = LootManager.genLootItem(stack, random, rarity);
		stack.setStackDisplayName(rarity.getColor() + bossName + "'s " + stack.getDisplayName());
		Map<Enchantment, Integer> enchMap = new HashMap<>();
		for (Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			if (e.getKey() != null) enchMap.put(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey()), e.getValue() + random.nextInt(2)));
		}
		EnchantmentHelper.setEnchantments(enchMap, stack);
		return stack;
	}

	public static enum EquipmentType {
		SWORD(s -> EntityEquipmentSlot.MAINHAND),
		BOW(s -> EntityEquipmentSlot.MAINHAND),
		TOOL(s -> EntityEquipmentSlot.MAINHAND),
		ARMOR(s -> ((ItemArmor) s.getItem()).armorType),
		SHIELD(s -> EntityEquipmentSlot.OFFHAND);

		final Function<ItemStack, EntityEquipmentSlot> type;

		EquipmentType(Function<ItemStack, EntityEquipmentSlot> type) {
			this.type = type;
		}

		public EntityEquipmentSlot getSlot(ItemStack stack) {
			return this.type.apply(stack);
		}

		public static EquipmentType getTypeFor(ItemStack stack) {
			Item i = stack.getItem();
			if (i instanceof ItemSword) return SWORD;
			if (i instanceof ItemBow) return BOW;
			if (i instanceof ItemArmor) return ARMOR;
			if (i instanceof ItemShield) return SHIELD;
			return TOOL;
		}
	}
}