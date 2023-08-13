package shadows.apotheosis.adventure.boss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

public final class BossItem extends TypeKeyedBase<BossItem> implements ILuckyWeighted, IDimensional, LootRarity.Clamped, IStaged {

    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.DOUBLE.fieldOf("width").forGetter(a -> Math.abs(a.maxX - a.minX)),
            Codec.DOUBLE.fieldOf("height").forGetter(a -> Math.abs(a.maxY - a.minY)))
        .apply(inst, (width, height) -> new AABB(0, 0, 0, width, height, width)));

    public static final Codec<BossItem> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
            Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("quality", 0F).forGetter(ILuckyWeighted::getQuality),
            ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(a -> a.entity),
            AABB_CODEC.fieldOf("size").forGetter(a -> a.size),
            LootRarity.mapCodec(BossStats.CODEC).fieldOf("stats").forGetter(a -> a.stats),
            PlaceboCodecs.setOf(Codec.STRING).optionalFieldOf("stages").forGetter(a -> Optional.ofNullable(a.stages)),
            SetPredicate.CODEC.listOf().fieldOf("valid_gear_sets").forGetter(a -> a.gearSets),
            NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(a -> Optional.ofNullable(a.nbt)),
            PlaceboCodecs.setOf(ResourceLocation.CODEC).fieldOf("dimensions").forGetter(a -> a.dimensions),
            LootRarity.CODEC.optionalFieldOf("min_rarity", LootRarity.COMMON).forGetter(a -> a.minRarity),
            LootRarity.CODEC.optionalFieldOf("max_rarity", LootRarity.MYTHIC).forGetter(a -> a.maxRarity),
            SupportingEntity.CODEC.optionalFieldOf("mount").forGetter(a -> Optional.ofNullable(a.mount)))
        .apply(inst, BossItem::new));

    public static final PSerializer<BossItem> SERIALIZER = PSerializer.fromCodec("Apotheotic Boss", CODEC);

    public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal && ((NearestAttackableTargetGoal<?>) a).targetType == Villager.class;

    protected final int weight;
    protected final float quality;
    protected final EntityType<?> entity;
    protected final AABB size;
    protected final Map<LootRarity, BossStats> stats;

    @Nullable
    protected final Set<String> stages;

    @SerializedName("valid_gear_sets")
    protected final List<SetPredicate> gearSets;

    @Nullable
    protected final CompoundTag nbt;

    protected final Set<ResourceLocation> dimensions;

    @SerializedName("min_rarity")
    protected final LootRarity minRarity;

    @SerializedName("max_rarity")
    protected final LootRarity maxRarity;

    @Nullable
    protected final SupportingEntity mount;

    public BossItem(int weight, float quality, EntityType<?> entity, AABB size, Map<LootRarity, BossStats> stats, Optional<Set<String>> stages, List<SetPredicate> armorSets, Optional<CompoundTag> nbt, Set<ResourceLocation> dimensions,
        LootRarity minRarity, LootRarity maxRarity, Optional<SupportingEntity> mount) {
        this.weight = weight;
        this.quality = quality;
        this.entity = entity;
        this.size = size;
        this.stats = stats;
        this.stages = stages.orElse(null);
        this.gearSets = armorSets;
        this.nbt = nbt.orElse(null);
        this.dimensions = dimensions;
        this.minRarity = minRarity;
        this.maxRarity = maxRarity;
        this.mount = mount.orElse(null);
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
     * @see #createBoss(ServerLevelAccessor, BlockPos, RandomSource, float, LootRarity)
     */
    public Mob createBoss(ServerLevelAccessor world, BlockPos pos, RandomSource random, float luck) {
        return this.createBoss(world, pos, random, luck, null);
    }

    /**
     * Generates (but does not spawn) the result of this BossItem.
     *
     * @param world  The world to create the entity in.
     * @param pos    The location to place the entity. Will be centered (+0.5, +0.5).
     * @param random A random, used for selection of boss stats.
     * @param luck   The player's luck value.
     * @param rarity A rarity override. This will be clamped to a valid rarity, and randomly generated if null.
     * @return The newly created boss, or it's mount, if it had one.
     */
    public Mob createBoss(ServerLevelAccessor world, BlockPos pos, RandomSource random, float luck, @Nullable LootRarity rarity) {
        CompoundTag fakeNbt = this.nbt == null ? new CompoundTag() : this.nbt;
        fakeNbt.putString("id", EntityType.getKey(this.entity).toString());
        Mob entity = (Mob) EntityType.loadEntityRecursive(fakeNbt, world.getLevel(), Function.identity());
        if (this.nbt != null) entity.load(this.nbt);
        this.initBoss(random, entity, luck, rarity);
        // Re-read here so we can apply certain things after the boss has been modified
        // But only mob-specific things, not a full load()
        if (this.nbt != null) entity.readAdditionalSaveData(this.nbt);

        if (this.mount != null) {
            Mob mountedEntity = this.mount.create(world.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.startRiding(mountedEntity, true);
            entity = mountedEntity;
        }

        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        return entity;
    }

    /**
     * Initializes an entity as a boss, based on the stats of this BossItem.
     *
     * @param rand
     * @param entity
     */
    public void initBoss(RandomSource rand, Mob entity, float luck, @Nullable LootRarity rarity) {
        if (rarity == null) rarity = LootRarity.random(rand, luck, this);
        rarity = this.clamp(rarity);
        BossStats stats = this.stats.get(rarity);
        int duration = entity instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : stats.effects()) {
            if (rand.nextFloat() <= inst.getChance()) {
                entity.addEffect(inst.createInstance(rand, duration));
            }
        }

        for (RandomAttributeModifier modif : stats.modifiers()) {
            modif.apply(rand, entity);
        }

        entity.goalSelector.availableGoals.removeIf(IS_VILLAGER_ATTACK);
        String name = NameHelper.setEntityName(rand, entity);

        GearSet set = BossArmorManager.INSTANCE.getRandomSet(rand, luck, this.gearSets);
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
            if (stack.isEmpty()) continue;
            if (s.ordinal() == guaranteed) entity.setDropChance(s, 2F);
            if (s.ordinal() == guaranteed) {
                entity.setItemSlot(s, modifyBossItem(stack, rand, name, luck, rarity, stats));
                entity.setCustomName(((MutableComponent) entity.getCustomName()).withStyle(Style.EMPTY.withColor(rarity.color())));
            }
            else if (rand.nextFloat() < stats.enchantChance()) {
                enchantBossItem(rand, stack, Apotheosis.enableEnch ? stats.enchLevels()[0] : stats.enchLevels()[1], true);
                entity.setItemSlot(s, stack);
            }
        }
        entity.getPersistentData().putBoolean("apoth.boss", true);
        entity.getPersistentData().putString("apoth.rarity", rarity.id());
        entity.setHealth(entity.getMaxHealth());
        if (AdventureConfig.bossGlowOnSpawn) entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 3600));
    }

    public static void enchantBossItem(RandomSource rand, ItemStack stack, int level, boolean treasure) {
        List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(rand, stack, level, treasure);
        var map = ench.stream().filter(d -> !d.enchantment.isCurse()).collect(Collectors.toMap(d -> d.enchantment, d -> d.level, Math::max));
        map.putAll(EnchantmentHelper.getEnchantments(stack));
        EnchantmentHelper.setEnchantments(map, stack);
    }

    public static ItemStack modifyBossItem(ItemStack stack, RandomSource rand, String bossName, float luck, LootRarity rarity, BossStats stats) {
        enchantBossItem(rand, stack, Apotheosis.enableEnch ? stats.enchLevels()[2] : stats.enchLevels()[3], true);
        NameHelper.setItemName(rand, stack);
        stack = LootController.createLootItem(stack, LootCategory.forItem(stack), rarity, rand);

        String bossOwnerName = String.format(NameHelper.ownershipFormat, bossName);
        Component name = AffixHelper.getName(stack);
        if (!bossName.isEmpty() && name.getContents() instanceof TranslatableContents tc) {
            String oldKey = tc.getKey();
            String newKey = "misc.apotheosis.affix_name.two".equals(oldKey) ? "misc.apotheosis.affix_name.three" : "misc.apotheosis.affix_name.four";
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
            final ItemStack stk = stack; // Lambda rules require this instead of a direct reference to stack
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
     *
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

    @Override
    public PSerializer<? extends BossItem> getSerializer() {
        return SERIALIZER;
    }

}
