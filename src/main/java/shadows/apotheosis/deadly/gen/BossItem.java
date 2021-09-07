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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.LootRarity;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.deadly.reload.BossArmorManager;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.GearSet;
import shadows.apotheosis.util.GearSet.SetPredicate;
import shadows.apotheosis.util.NameHelper;
import shadows.apotheosis.util.RandomAttributeModifier;

public class BossItem extends WeightedRandom.Item {

	public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal && ((NearestAttackableTargetGoal<?>) a).targetType == VillagerEntity.class;

	@Expose(deserialize = false)
	protected ResourceLocation id;

	protected final EntityType<?> entity;

	protected final AxisAlignedBB size;

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

	@SerializedName("custom_nbt")
	protected final CompoundNBT customNbt;

	public BossItem(int weight, EntityType<?> entity, AxisAlignedBB size, float enchantChance, int rarityOffset, int[] enchLevels, List<ChancedEffectInstance> effects, List<SetPredicate> armorSets, List<RandomAttributeModifier> modifiers, CompoundNBT customNbt) {
		super(weight);
		this.entity = entity;
		this.size = size;
		this.enchantChance = enchantChance;
		this.rarityOffset = rarityOffset;
		this.enchLevels = enchLevels;
		this.effects = effects;
		this.armorSets = armorSets;
		this.modifiers = modifiers;
		this.customNbt = customNbt;
	}

	public void setId(ResourceLocation id) {
		if (this.id == null) {
			this.id = id;
		} else throw new IllegalStateException("Cannot set the id of this boss item, it is already set!");
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public AxisAlignedBB getSize() {
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
	public MobEntity createBoss(IServerWorld world, BlockPos pos, Random rand) {
		MobEntity entity = (MobEntity) this.entity.create(world.getLevel());
		entity.readAdditionalSaveData(this.customNbt == null ? new CompoundNBT() : this.customNbt);
		this.initBoss(rand, entity);
		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
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
				entity.addEffect(inst.createInstance(rand, duration));
			}
		}

		for (RandomAttributeModifier modif : this.modifiers) {
			modif.apply(rand, entity);
		}

		entity.setHealth(entity.getMaxHealth());
		entity.goalSelector.availableGoals.removeIf(IS_VILLAGER_ATTACK);
		String name = NameHelper.setEntityName(rand, entity);

		GearSet set = BossArmorManager.INSTANCE.getRandomSet(rand, this.armorSets);
		set.apply(entity);

		boolean anyValid = false;

		for (EquipmentSlotType t : EquipmentSlotType.values()) {
			ItemStack s = entity.getItemBySlot(t);
			if (!s.isEmpty() && EquipmentType.getTypeFor(s) != null) {
				anyValid = true;
				break;
			}
		}

		if (!anyValid) throw new RuntimeException("Attempted to apply boss gear set " + set.getId() + " but it had no valid affix loot items generated.");

		int guaranteed = rand.nextInt(6);

		ItemStack temp = entity.getItemBySlot(EquipmentSlotType.values()[guaranteed]);
		while (temp.isEmpty() || EquipmentType.getTypeFor(temp) == null) {
			guaranteed = rand.nextInt(6);
			temp = entity.getItemBySlot(EquipmentSlotType.values()[guaranteed]);
		}

		for (EquipmentSlotType s : EquipmentSlotType.values()) {
			ItemStack stack = entity.getItemBySlot(s);
			if (s.ordinal() == guaranteed) entity.setDropChance(s, 2F);
			else entity.setDropChance(s, ThreadLocalRandom.current().nextFloat() / 2);
			if (s.ordinal() == guaranteed) {
				entity.setItemSlot(s, this.modifyBossItem(stack, rand, name));
			} else if (rand.nextFloat() < this.enchantChance) {
				List<EnchantmentData> ench = EnchantmentHelper.selectEnchantment(rand, stack, Apotheosis.enableEnch ? this.enchLevels[0] : this.enchLevels[1], true);
				EnchantmentHelper.setEnchantments(ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.level, Math::max, HashMap::new)), stack);
				entity.setItemSlot(s, stack);
			}
		}

	}

	public ItemStack modifyBossItem(ItemStack stack, Random random, String bossName) {
		List<EnchantmentData> ench = EnchantmentHelper.selectEnchantment(random, stack, Apotheosis.enableEnch ? this.enchLevels[2] : this.enchLevels[3], true);
		EnchantmentHelper.setEnchantments(ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.level, Math::max)), stack);
		LootRarity rarity = LootRarity.random(random, this.rarityOffset);
		NameHelper.setItemName(random, stack, bossName);
		stack = AffixLootManager.genLootItem(stack, random, EquipmentType.getTypeFor(stack), rarity);
		Color color = rarity.getColor();
		stack.setHoverName(new TranslationTextComponent("%s %s", new StringTextComponent(String.format(NameHelper.ownershipFormat, bossName)).withStyle(Style.EMPTY), stack.getHoverName()).withStyle(Style.EMPTY).withStyle(Style.EMPTY.withColor(color)));
		Map<Enchantment, Integer> enchMap = new HashMap<>();
		for (Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			if (e.getKey() != null) enchMap.put(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey()), e.getValue() + random.nextInt(2)));
		}

		if (DeadlyConfig.curseBossItems) {
			final ItemStack stk = stack; //Lambda rules require this instead of a direct reference to stack
			List<Enchantment> curses = ForgeRegistries.ENCHANTMENTS.getValues().stream().filter(e -> e.canApplyAtEnchantingTable(stk) && e.isCurse()).collect(Collectors.toList());
			if (!curses.isEmpty()) {
				Enchantment curse = curses.get(random.nextInt(curses.size()));
				enchMap.put(curse, MathHelper.nextInt(random, 1, EnchHooks.getMaxLevel(curse)));
			}
		}

		EnchantmentHelper.setEnchantments(enchMap, stack);
		stack.getTag().putBoolean("apoth_boss", true);
		return stack;
	}

}