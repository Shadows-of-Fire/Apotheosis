package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.Collections;
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
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.Apotheosis;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.gen.WorldFeature.WorldFeatureItem;
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

	//Formatter::off
	public static final Map<IAttribute, RandomValueRange> SWORD_ATTR = ImmutableMap.of(
			SharedMonsterAttributes.ATTACK_DAMAGE, new RandomValueRange(0.5F, 5.0F),
			EntityPlayer.REACH_DISTANCE, new RandomValueRange(0.5F, 2.0F),
			SharedMonsterAttributes.ATTACK_SPEED, new RandomValueRange(0.25F, 1.0F),
			SharedMonsterAttributes.MOVEMENT_SPEED, new RandomValueRange(0.05F, 0.25F));

	public static final Map<IAttribute, RandomValueRange> BOW_ATTR = ImmutableMap.of(
			SharedMonsterAttributes.KNOCKBACK_RESISTANCE, new RandomValueRange(0.25F, 2.0F),
			SharedMonsterAttributes.LUCK, new RandomValueRange(0.1F, 1.0F),
			SharedMonsterAttributes.MOVEMENT_SPEED, new RandomValueRange(0.05F, 0.4F));

	public static final Map<IAttribute, RandomValueRange> TOOL_ATTR = ImmutableMap.of(
			EntityPlayer.REACH_DISTANCE, new RandomValueRange(0.5F, 3.0F),
			SharedMonsterAttributes.LUCK, new RandomValueRange(0.5F, 2.0F));

	public static final Map<IAttribute, RandomValueRange> ARMOR_ATTR = ImmutableMap.<IAttribute, RandomValueRange>builder()
			.put(SharedMonsterAttributes.ARMOR, new RandomValueRange(0.2F, 2.0F))
			.put(SharedMonsterAttributes.ARMOR_TOUGHNESS, new RandomValueRange(0.1F, 0.5F))
			.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE, new RandomValueRange(0.2F, 0.8F))
			.put(SharedMonsterAttributes.MAX_HEALTH, new RandomValueRange(3F, 15F))
			.put(EntityLivingBase.SWIM_SPEED, new RandomValueRange(0.2F, 0.8F)
			//.put(EntityLivingBase.ENTITY_GRAVITY, new RandomValueRange(-0.08F, 0.08F)
			).build();

	public static final Map<IAttribute, RandomValueRange> SHIELD_ATTR = ImmutableMap.of(
			SharedMonsterAttributes.ARMOR, new RandomValueRange(5F, 10F),
			SharedMonsterAttributes.ARMOR_TOUGHNESS, new RandomValueRange(1F, 4F),
			SharedMonsterAttributes.KNOCKBACK_RESISTANCE, new RandomValueRange(0.3F, 1F),
			SharedMonsterAttributes.MAX_HEALTH, new RandomValueRange(5F, 25F));

	//Formatter::on

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
			else entity.setDropChance(s, ThreadLocalRandom.current().nextFloat());
			if (s.ordinal() == guaranteed) {
				modifyBossItem(stack, random, name);
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
			if (p.isBeneficial() && !p.isInstant()) POTIONS.add(p);
		POTIONS.removeIf(p -> DeadlyConfig.BLACKLISTED_POTIONS.contains(p.getRegistryName()));
	}

	public static void modifyBossItem(ItemStack stack, Random random, String bossName) {
		List<EnchantmentData> ench = EnchantmentHelper.buildEnchantmentList(random, stack, Apotheosis.enableEnch ? 60 : 30, true);
		EnchantmentHelper.setEnchantments(ench.stream().collect(Collectors.toMap(d -> d.enchantment, d -> d.enchantmentLevel)), stack);
		NameHelper.setItemName(random, stack, bossName, ench.get(random.nextInt(ench.size())).enchantment);
		EquipmentType.getTypeForStack(stack).apply(stack, random);
		Map<Enchantment, Integer> enchMap = new HashMap<>();
		for (Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			enchMap.put(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey()), e.getValue() + random.nextInt(2)));
		}
		EnchantmentHelper.setEnchantments(enchMap, stack);
	}

	static enum EquipmentType {
		SWORD(SWORD_ATTR, s -> EntityEquipmentSlot.MAINHAND),
		BOW(BOW_ATTR, s -> EntityEquipmentSlot.MAINHAND),
		TOOL(TOOL_ATTR, s -> EntityEquipmentSlot.MAINHAND),
		ARMOR(ARMOR_ATTR, s -> ((ItemArmor) s.getItem()).getEquipmentSlot()),
		SHIELD(SHIELD_ATTR, s -> EntityEquipmentSlot.OFFHAND);

		final Map<IAttribute, RandomValueRange> attributes;
		final Function<ItemStack, EntityEquipmentSlot> type;

		EquipmentType(Map<IAttribute, RandomValueRange> attributes, Function<ItemStack, EntityEquipmentSlot> type) {
			this.attributes = attributes;
			this.type = type;
		}

		public void apply(ItemStack stack, Random rand) {
			int numAttributes = Math.min(attributes.size(), 1 + rand.nextInt(3));
			List<AttributeModifier> modifiers = new ArrayList<>();
			List<IAttribute> attr = new ArrayList<>(attributes.keySet());
			Collections.shuffle(attr, rand);
			for (int i = 0; i < numAttributes; i++)
				modifiers.add(new AttributeModifier(attr.get(i).getName(), attributes.get(attr.get(i)).generateFloat(rand), 0));
			modifiers.forEach(m -> stack.addAttributeModifier(m.getName(), m, type.apply(stack)));
		}

		public static EquipmentType getTypeForStack(ItemStack stack) {
			Item i = stack.getItem();
			if (i instanceof ItemSword) return SWORD;
			if (i instanceof ItemBow) return BOW;
			if (i instanceof ItemArmor) return ARMOR;
			if (i instanceof ItemShield) return SHIELD;
			return TOOL;
		}
	}
}