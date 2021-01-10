package shadows.apotheosis.deadly.gen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IServerWorld;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.LootRarity;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.deadly.reload.BossArmorManager;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.NameHelper;
import shadows.apotheosis.util.RandomAttributeModifier;

public class BossItem extends WeightedRandom.Item {

	public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal && ((NearestAttackableTargetGoal<?>) a).targetClass == VillagerEntity.class;

	@Expose(deserialize = false)
	protected ResourceLocation id;
	protected final EntityType<?> entity;
	protected final AxisAlignedBB size;
	@SerializedName("enchant_chance")
	protected final float enchantChance;
	protected final List<ChancedEffectInstance> effects;
	@SerializedName("valid_gear_sets")
	protected final List<ResourceLocation> armorSets;
	@SerializedName("attribute_modifiers")
	protected final List<RandomAttributeModifier> modifiers;

	public BossItem(int weight, EntityType<?> entity, AxisAlignedBB size, float enchantChance, List<ChancedEffectInstance> effects, List<ResourceLocation> armorSets, List<RandomAttributeModifier> modifiers) {
		super(weight);
		this.entity = entity;
		this.size = size;
		this.enchantChance = enchantChance;
		this.effects = effects;
		this.armorSets = armorSets;
		this.modifiers = modifiers;
	}

	public void setId(ResourceLocation id) {
		if (this.id == null) {
			this.id = id;
		} else throw new IllegalStateException("Cannot set the id of this boss item, it is already set!");
	}

	public ResourceLocation getId() {
		return id;
	}

	public AxisAlignedBB getSize() {
		return size;
	}

	/**
	 * Spawns the result of this BossItem.
	 * @param world The world to create the entity in.
	 * @param pos The location to place the entity.  Will be centered (+0.5, +0.5).
	 * @param rand A random, used for selection of boss stats.
	 * @return The newly created boss.
	 */
	public MobEntity spawnBoss(IServerWorld world, BlockPos pos, Random rand) {
		MobEntity entity = (MobEntity) this.entity.create(world.getWorld());
		initBoss(rand, entity);
		entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
		world.addEntity(entity);
		return entity;
	}

	/**
	 * Initializes an entity as a boss, based on the stats of this BossItem.
	 * @param rand
	 * @param entity
	 */
	public void initBoss(Random rand, MobEntity entity) {
		int duration = entity instanceof CreeperEntity ? 6000 : Integer.MAX_VALUE;

		for (ChancedEffectInstance inst : this.effects) {
			if (rand.nextFloat() <= inst.getChance()) {
				entity.addPotionEffect(inst.createInstance(rand, duration));
			}
		}

		for (RandomAttributeModifier modif : this.modifiers) {
			modif.apply(rand, entity);
		}

		entity.setHealth(entity.getMaxHealth());
		entity.goalSelector.goals.removeIf(IS_VILLAGER_ATTACK);
		String name = NameHelper.setEntityName(rand, entity);

		BossArmorManager.INSTANCE.getRandomSet(rand, armorSets).apply(entity);

		int guaranteed = rand.nextInt(6);

		ItemStack stack = entity.getItemStackFromSlot(EquipmentSlotType.values()[guaranteed]);
		while (stack.isEmpty())
			stack = entity.getItemStackFromSlot(EquipmentSlotType.values()[guaranteed = rand.nextInt(6)]);

		for (EquipmentSlotType s : EquipmentSlotType.values()) {
			if (s.ordinal() == guaranteed) entity.setDropChance(s, 2F);
			else entity.setDropChance(s, ThreadLocalRandom.current().nextFloat() / 2);
			if (s.ordinal() == guaranteed) {
				entity.setItemStackToSlot(s, modifyBossItem(stack, rand, name));
			} else if (rand.nextFloat() < enchantChance) {
				List<EnchantmentData> ench = EnchantmentHelper.buildEnchantmentList(rand, stack, 30 + rand.nextInt(Apotheosis.enableEnch ? 20 : 10), true);
				EnchantmentHelper.setEnchantments(ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.enchantmentLevel, (v1, v2) -> Math.max(v1, v2), HashMap::new)), stack);
			}
		}

	}

	public static ItemStack modifyBossItem(ItemStack stack, Random random, String bossName) {
		List<EnchantmentData> ench = EnchantmentHelper.buildEnchantmentList(random, stack, Apotheosis.enableEnch ? 80 : 40, true);
		EnchantmentHelper.setEnchantments(ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.enchantmentLevel, (a, b) -> Math.max(a, b))), stack);
		LootRarity rarity = LootRarity.random(random, DeadlyConfig.bossRarityOffset);
		NameHelper.setItemName(random, stack, bossName);
		stack = AffixLootManager.genLootItem(stack, random, rarity);
		stack.setDisplayName(new TranslationTextComponent("%s %s", TextFormatting.RESET + rarity.getColor().toString() + String.format(NameHelper.ownershipFormat, bossName), stack.getDisplayName()).mergeStyle(rarity.getColor()));
		Map<Enchantment, Integer> enchMap = new HashMap<>();
		for (Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			if (e.getKey() != null) enchMap.put(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey()), e.getValue() + random.nextInt(2)));
		}
		EnchantmentHelper.setEnchantments(enchMap, stack);
		stack.getTag().putBoolean("apoth_boss", true);
		return stack;
	}

}