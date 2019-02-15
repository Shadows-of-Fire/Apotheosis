package shadows.deadly.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.gen.WorldFeature.WorldFeatureItem;
import shadows.placebo.util.AttributeHelper;
import shadows.placebo.util.PlaceboUtil;
import shadows.util.ArmorSet;
import shadows.util.NameHelper;

/**
 * Setup information for bosses.
 * TODO: Make configurable.
 * @author Shadows
 *
 */
public class BossItem extends WorldFeatureItem {

	//Default lists of boss potions/enchantments.
	public static final List<Potion> POTIONS = new ArrayList<>();
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
		entity.enablePersistence();
		world.spawnEntity(entity);
	}

	public static void initBoss(Random random, EntityLiving entity) {
		if (DeadlyConfig.bossRegenLevel > 0) entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, DeadlyConfig.bossRegenLevel));
		if (DeadlyConfig.bossResistLevel > 0) entity.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, Integer.MAX_VALUE, DeadlyConfig.bossResistLevel));
		if (DeadlyConfig.bossFireRes) entity.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE));
		if (DeadlyConfig.bossWaterBreathing) entity.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, Integer.MAX_VALUE));
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

		if (entity instanceof EntitySkeleton) entity.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.BOW));

		int guaranteed = ThreadLocalRandom.current().nextInt(6);

		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.values()[guaranteed]);
		while (guaranteed == 1 || stack.isEmpty())
			stack = entity.getItemStackFromSlot(EntityEquipmentSlot.values()[guaranteed = ThreadLocalRandom.current().nextInt(6)]);

		for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) {
			if (s.ordinal() == guaranteed) entity.setDropChance(s, 2F);
			else entity.setDropChance(s, ThreadLocalRandom.current().nextFloat());

			stack = entity.getItemStackFromSlot(s);

			if (s.ordinal() == guaranteed) {
				List<Enchantment> enchants = EquipmentType.getTypeForStack(stack).getEnchants();
				Enchantment enchantment = enchants.get(random.nextInt(enchants.size()));
				NameHelper.setItemName(random, stack, name, enchantment);

				for (int i = 0; i < 5; i++)
					EnchantmentHelper.addRandomEnchantment(random, stack, 28 + 3 * i, true);

				Map<Enchantment, Integer> enchantMap = EnchantmentHelper.getEnchantments(stack);
				for (Enchantment e : enchantMap.keySet())
					enchantMap.put(e, e.getMaxLevel());
				EnchantmentHelper.setEnchantments(enchantMap, stack);
			} else if (random.nextDouble() < DeadlyConfig.bossEnchantChance) EnchantmentHelper.addRandomEnchantment(random, stack, 30 + random.nextInt(30), true);
		}

		if (POTIONS.isEmpty()) for (Potion p : ForgeRegistries.POTIONS)
			if (p.beneficial) POTIONS.add(p);

		if (random.nextDouble() < DeadlyConfig.bossPotionChance) entity.addPotionEffect(new PotionEffect(POTIONS.get(random.nextInt(POTIONS.size())), Integer.MAX_VALUE, random.nextInt(3) + 1));
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
			if (i instanceof ItemSword) return SWORD;
			if (i instanceof ItemBow) return BOW;
			if (i instanceof ItemArmor || i instanceof ISpecialArmor) return ARMOR;
			return TOOL;
		}
	}
}