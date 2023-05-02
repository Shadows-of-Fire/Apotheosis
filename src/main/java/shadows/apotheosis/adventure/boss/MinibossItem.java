package shadows.apotheosis.adventure.boss;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.boss.MinibossManager.IEntityMatch;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.apotheosis.util.GearSet;
import shadows.apotheosis.util.GearSet.SetPredicate;
import shadows.apotheosis.util.NameHelper;
import shadows.apotheosis.util.SupportingEntity;
import shadows.placebo.codec.PlaceboCodecs;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.RandomAttributeModifier;
import shadows.placebo.json.TypeKeyed.TypeKeyedBase;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

public final class MinibossItem extends TypeKeyedBase<MinibossItem> implements ILuckyWeighted, IDimensional, IStaged, IEntityMatch {

	public static final String NAME_GEN = "use_name_generation";

	//Formatter::off
	public static final Codec<MinibossItem> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("quality", 0F).forGetter(ILuckyWeighted::getQuality),
			ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance").forGetter(a -> a.chance),
			Codec.STRING.optionalFieldOf("name", NAME_GEN).forGetter(a -> a.name),
			PlaceboCodecs.setCodec(ForgeRegistries.ENTITY_TYPES.getCodec()).fieldOf("entities").forGetter(a -> a.entities),
			BossStats.CODEC.fieldOf("stats").forGetter(a -> a.stats),
			PlaceboCodecs.setCodec(Codec.STRING).optionalFieldOf("stages").forGetter(a -> Optional.ofNullable(a.stages)),
			PlaceboCodecs.setCodec(ResourceLocation.CODEC).fieldOf("dimensions").forGetter(a -> a.dimensions),
			Codec.BOOL.optionalFieldOf("affixed", false).forGetter(a -> a.affixed),
			SetPredicate.CODEC.listOf().fieldOf("valid_gear_sets").forGetter(a -> a.gearSets),
			NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(a -> Optional.ofNullable(a.nbt)),
			SupportingEntity.CODEC.listOf().optionalFieldOf("supporting_entities", Collections.emptyList()).forGetter(a -> a.support),
			SupportingEntity.CODEC.optionalFieldOf("mount").forGetter(a -> Optional.ofNullable(a.mount)),
			Exclusion.CODEC.listOf().optionalFieldOf("exclusions", Collections.emptyList()).forGetter(a -> a.exclusions))
		.apply(inst, MinibossItem::new)
	);
	//Formatter::on
	public static final PSerializer<MinibossItem> SERIALIZER = PSerializer.fromCodec("Apotheotic Miniboss", CODEC);

	/**
	 * Weight relative to other minibosses that may apply to the same entity.
	 */
	protected final int weight;

	/**
	 * Quality increases the weight by the quality value for every point of luck. 
	 */
	protected final float quality;

	/**
	 * Chance that this miniboss item is applied, if selected. Selection runs for every entity spawn.
	 * This chance is rolled after weight selection is completed.
	 */
	protected final float chance;

	/**
	 * Name of the miniboss. Can be a lang key. Empty or null will cause no name to be set. The special string "use_name_generation" will invoke NameHelper (like normal bosses).
	 */
	protected final String name;

	/**
	 * List of matching entities.
	 */
	protected final Set<EntityType<?>> entities;

	/**
	 * Stats that are applied to the miniboss.
	 */
	protected final BossStats stats;

	/**
	 * Game stages that this miniboss may spawn in.
	 * If omitted (null), it can always spawn. If empty, it can never spawn.
	 */
	@Nullable
	protected final Set<String> stages;

	/**
	 * Dimensions that this miniboss may spawn in.
	 */
	protected final Set<ResourceLocation> dimensions;

	/**
	 * If the miniboss will be given an affix item like a normal boss.
	 * Rarity selection follows the affix convert rarities of the dimension.
	 */
	protected final boolean affixed;

	/**
	 * Valid armor sets for this miniboss.
	 */
	protected final List<SetPredicate> gearSets;

	/**
	 * Entity NBT
	 */
	@Nullable
	protected final CompoundTag nbt;

	/**
	 * A list of supporting entities that will be spawned if this miniboss is activated.+
	 */
	protected final List<SupportingEntity> support;

	/**
	 * The entity the miniboss will mount.
	 */
	@Nullable
	protected final SupportingEntity mount;

	/**
	 * List of rules that may prevent this miniboss from being selected.
	 * @see {@link Exclusion}
	 */
	protected final List<Exclusion> exclusions;

	public MinibossItem(int weight, float quality, float chance, String name, Set<EntityType<?>> entities, BossStats stats, Optional<Set<String>> stages, Set<ResourceLocation> dimensions, boolean affixed, List<SetPredicate> gearSets, Optional<CompoundTag> nbt, List<SupportingEntity> support, Optional<SupportingEntity> mount, List<Exclusion> exclusions) {
		this.weight = weight;
		this.quality = quality;
		this.chance = chance;
		this.name = name;
		this.entities = entities;
		this.stats = stats;
		this.stages = stages.orElse(null);
		this.dimensions = dimensions;
		this.affixed = affixed;
		this.gearSets = gearSets;
		this.nbt = nbt.orElse(null);
		this.support = support;
		this.mount = mount.orElse(null);
		this.exclusions = exclusions;
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
	public Set<EntityType<?>> getEntities() {
		return this.entities;
	}

	/**
	 * Transforms a mob into this miniboss, spawning any supporting entities or mounts as needed.
	 * @param mob The mob being transformed.
	 * @param random A random, used for selection of boss stats.
	 * @return The newly created boss, or it's mount, if it had one.
	 */
	public void transformMiniboss(ServerLevelAccessor level, Mob mob, RandomSource random, float luck) {
		var pos = mob.getPosition(0);
		if (this.nbt != null) mob.load(this.nbt);
		mob.setPos(pos);
		this.initBoss(random, mob, luck);
		// Re-read here so we can apply certain things after the boss has been modified
		// But only mob-specific things, not a full load()
		if (this.nbt != null) mob.readAdditionalSaveData(this.nbt);

		if (this.mount != null) {
			Mob mountedEntity = this.mount.create(mob.getLevel(), mob.getX() + 0.5, mob.getY(), mob.getZ() + 0.5);
			mob.startRiding(mountedEntity);
			level.addFreshEntity(mountedEntity);
		}

		if (this.support != null) {
			for (var support : this.support) {
				Mob supportingMob = support.create(mob.getLevel(), mob.getX() + 0.5, mob.getY(), mob.getZ() + 0.5);
				level.addFreshEntity(supportingMob);
			}
		}
	}

	/**
	 * Initializes an entity as a boss, based on the stats of this BossItem.
	 * @param rand
	 * @param mob
	 */
	public void initBoss(RandomSource rand, Mob mob, float luck) {
		int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

		for (ChancedEffectInstance inst : stats.effects()) {
			if (rand.nextFloat() <= inst.getChance()) {
				mob.addEffect(inst.createInstance(rand, duration));
			}
		}

		for (RandomAttributeModifier modif : stats.modifiers()) {
			modif.apply(rand, mob);
		}

		mob.goalSelector.availableGoals.removeIf(BossItem.IS_VILLAGER_ATTACK);
		if (NAME_GEN.equals(this.name)) {
			NameHelper.setEntityName(rand, mob);
		} else if (!Strings.isNullOrEmpty(this.name)) {
			mob.setCustomName(Component.translatable(this.name));
		}

		mob.setCustomNameVisible(true);

		GearSet set = BossArmorManager.INSTANCE.getRandomSet(rand, luck, this.gearSets);
		Preconditions.checkNotNull(set, String.format("Failed to find a valid gear set for the miniboss %s.", this.getId()));
		set.apply(mob);

		int guaranteed = -1;
		if (this.affixed) {
			boolean anyValid = false;

			for (EquipmentSlot t : EquipmentSlot.values()) {
				ItemStack s = mob.getItemBySlot(t);
				if (!s.isEmpty() && !LootCategory.forItem(s).isNone()) {
					anyValid = true;
					break;
				}
			}

			if (!anyValid) throw new RuntimeException("Attempted to apply boss gear set " + set.getId() + " but it had no valid affix loot items generated.");

			guaranteed = rand.nextInt(6);

			ItemStack temp = mob.getItemBySlot(EquipmentSlot.values()[guaranteed]);
			while (temp.isEmpty() || LootCategory.forItem(temp) == LootCategory.NONE) {
				guaranteed = rand.nextInt(6);
				temp = mob.getItemBySlot(EquipmentSlot.values()[guaranteed]);
			}

			var rarity = LootRarity.random(rand, luck, AdventureConfig.AFFIX_CONVERT_RARITIES.get(mob.level.dimension().location()));
			this.modifyBossItem(temp, rand, name, luck, rarity);
			mob.setCustomName(((MutableComponent) mob.getCustomName()).withStyle(Style.EMPTY.withColor(rarity.color())));
			mob.setDropChance(EquipmentSlot.values()[guaranteed], 2F);
		}

		for (EquipmentSlot s : EquipmentSlot.values()) {
			ItemStack stack = mob.getItemBySlot(s);
			if (s.ordinal() != guaranteed && rand.nextFloat() < stats.enchantChance()) {
				enchantBossItem(rand, stack, Apotheosis.enableEnch ? stats.enchLevels()[0] : stats.enchLevels()[1], true);
				mob.setItemSlot(s, stack);
			}
		}
		mob.getPersistentData().putBoolean("apoth.miniboss", true);
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
		enchantBossItem(rand, stack, Apotheosis.enableEnch ? stats.enchLevels()[2] : stats.enchLevels()[3], true);

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
		Preconditions.checkArgument(this.weight >= 0, "Miniboss Item " + this.id + " has a negative weight!");
		Preconditions.checkArgument(this.quality >= 0, "Miniboss Item " + this.id + " has a negative quality!");
		Preconditions.checkNotNull(this.entities, "Miniboss Item " + this.id + " has null entity match list!");
		Preconditions.checkNotNull(this.stats, "Miniboss Item " + this.id + " has no stats!");
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

	@Override
	public PSerializer<? extends MinibossItem> getSerializer() {
		return SERIALIZER;
	}

	public boolean requiresNbtAccess() {
		return this.exclusions.stream().anyMatch(Exclusion::requiresNbtAccess);
	}

	public boolean isExcluded(Mob mob, ServerLevelAccessor level, MobSpawnType type) {
		CompoundTag tag = requiresNbtAccess() ? mob.saveWithoutId(new CompoundTag()) : null;
		return this.exclusions.stream().anyMatch(ex -> ex.isExcluded(mob, level, type, tag));
	}

}