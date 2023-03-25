package shadows.apotheosis.adventure.boss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import shadows.apotheosis.adventure.boss.MinibossItem.SupportingEntity;
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

public final class BossItem extends TypeKeyedBase<BossItem> implements ILuckyWeighted, IDimensional, LootRarity.Clamped, IStaged {

	public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal && ((NearestAttackableTargetGoal<?>) a).targetType == Villager.class;

	protected int weight;
	protected float quality;
	protected EntityType<?> entity;
	protected AABB size;
	protected Map<LootRarity, BossStats> stats;
	protected @Nullable Set<String> stages;

	@SerializedName("valid_gear_sets")
	protected List<SetPredicate> armorSets;

	@SerializedName("nbt")
	protected CompoundTag customNbt;

	protected Set<ResourceLocation> dimensions;

	@SerializedName("min_rarity")
	protected LootRarity minRarity;

	@SerializedName("max_rarity")
	protected LootRarity maxRarity;

	protected SupportingEntity mount;

	public BossItem() {
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

	@Override
	public LootRarity getMinRarity() {
		return this.minRarity;
	}

	@Override
	public LootRarity getMaxRarity() {
		return this.maxRarity;
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
	 * @param random A random, used for selection of boss stats.
	 * @return The newly created boss, or it's mount, if it had one.
	 */
	public Mob createBoss(ServerLevelAccessor world, BlockPos pos, RandomSource random, float luck) {
		Mob entity = (Mob) this.entity.create(world.getLevel());
		if (this.customNbt != null) entity.load(this.customNbt);
		this.initBoss(random, entity, luck);
		// Re-read here so we can apply certain things after the boss has been modified
		// But only mob-specific things, not a full load()
		if (this.customNbt != null) entity.readAdditionalSaveData(this.customNbt);

		if (this.mount != null) {
			Mob mountedEntity = (Mob) this.mount.create(world.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			entity.startRiding(mountedEntity);
			entity = mountedEntity;
		}

		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
		return entity;
	}

	/**
	 * Initializes an entity as a boss, based on the stats of this BossItem.
	 * @param rand
	 * @param entity
	 */
	public void initBoss(RandomSource rand, Mob entity, float luck) {
		LootRarity rarity = LootRarity.random(rand, luck, this);
		BossStats stats = this.stats.get(rarity);
		int duration = entity instanceof Creeper ? 6000 : Integer.MAX_VALUE;

		for (ChancedEffectInstance inst : stats.effects) {
			if (rand.nextFloat() <= inst.getChance()) {
				entity.addEffect(inst.createInstance(rand, duration));
			}
		}

		for (RandomAttributeModifier modif : stats.modifiers) {
			modif.apply(rand, entity);
		}

		entity.goalSelector.availableGoals.removeIf(IS_VILLAGER_ATTACK);
		String name = NameHelper.setEntityName(rand, entity);

		GearSet set = BossArmorManager.INSTANCE.getRandomSet(rand, luck, this.armorSets);
		set.apply(entity);

		boolean anyValid = false;

		for (EquipmentSlot t : EquipmentSlot.values()) {
			ItemStack s = entity.getItemBySlot(t);
			if (!s.isEmpty() && !LootCategory.forItem(s).isNone()) {
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
			if (s.ordinal() == guaranteed) {
				entity.setItemSlot(s, this.modifyBossItem(stack, rand, name, luck, rarity));
				entity.setCustomName(((MutableComponent) entity.getCustomName()).withStyle(Style.EMPTY.withColor(rarity.color())));
			} else if (rand.nextFloat() < stats.enchantChance) {
				enchantBossItem(rand, stack, Apotheosis.enableEnch ? stats.enchLevels[0] : stats.enchLevels[1], true);
				entity.setItemSlot(s, stack);
			}
		}
		entity.getPersistentData().putBoolean("apoth.boss", true);
		entity.getPersistentData().putString("apoth.rarity", rarity.id());
		entity.setHealth(entity.getMaxHealth());
		entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 2400));
	}

	public void enchantBossItem(RandomSource rand, ItemStack stack, int level, boolean treasure) {
		List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(rand, stack, level, treasure);
		var map = ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.level, Math::max));
		map.putAll(EnchantmentHelper.getEnchantments(stack));
		EnchantmentHelper.setEnchantments(map, stack);
	}

	public ItemStack modifyBossItem(ItemStack stack, RandomSource rand, String bossName, float luck, LootRarity rarity) {
		BossStats stats = this.stats.get(rarity);
		enchantBossItem(rand, stack, Apotheosis.enableEnch ? stats.enchLevels[2] : stats.enchLevels[3], true);

		NameHelper.setItemName(rand, stack);
		stack = LootController.createLootItem(stack, LootCategory.forItem(stack), rarity, rand);

		String bossOwnerName = String.format(NameHelper.ownershipFormat, bossName);
		Component name = AffixHelper.getName(stack);
		if (name.getContents() instanceof TranslatableContents tc) {
			String oldKey = tc.getKey();
			String newKey = oldKey.equals("misc.apotheosis.affix_name.two") ? "misc.apotheosis.affix_name.three" : "misc.apotheosis.affix_name.four";
			Object[] newArgs = new Object[tc.getArgs().length + 1];
			newArgs[0] = bossOwnerName;
			for (int i = 1; i < newArgs.length; i++) {
				newArgs[i] = tc.getArgs()[i - 1];
			}
			Component copy = Component.translatable(newKey, newArgs).withStyle(name.getStyle().withItalic(false));
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
	public BossItem validate() {
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
		if (this.mount != null) {
			Preconditions.checkNotNull(this.mount.entity, "Boss Item " + this.id + " has an invalid mount");
		}
		LootRarity r = LootRarity.max(LootRarity.COMMON, this.minRarity);
		while (r != LootRarity.ANCIENT) {
			Preconditions.checkNotNull(this.stats.get(r));
			if (r == this.maxRarity) break;
			r = LootRarity.LIST.get(r.ordinal() + 1);
		}
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

}