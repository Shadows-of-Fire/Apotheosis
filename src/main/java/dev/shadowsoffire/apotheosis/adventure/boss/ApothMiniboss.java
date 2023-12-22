package dev.shadowsoffire.apotheosis.adventure.boss;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.boss.MinibossRegistry.IEntityMatch;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apotheosis.util.SupportingEntity;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.GearSet;
import dev.shadowsoffire.placebo.json.GearSet.SetPredicate;
import dev.shadowsoffire.placebo.json.GearSetRegistry;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class ApothMiniboss implements CodecProvider<ApothMiniboss>, ILuckyWeighted, IDimensional, IStaged, IEntityMatch {

    public static final String NAME_GEN = "use_name_generation";

    public static final Codec<ApothMiniboss> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
            PlaceboCodecs.nullableField(Codec.floatRange(0, Float.MAX_VALUE), "quality", 0F).forGetter(ILuckyWeighted::getQuality),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance").forGetter(a -> a.chance),
            PlaceboCodecs.nullableField(Codec.STRING, "name", "").forGetter(a -> a.name),
            PlaceboCodecs.setOf(ForgeRegistries.ENTITY_TYPES.getCodec()).fieldOf("entities").forGetter(a -> a.entities),
            BossStats.CODEC.fieldOf("stats").forGetter(a -> a.stats),
            PlaceboCodecs.nullableField(PlaceboCodecs.setOf(Codec.STRING), "stages").forGetter(a -> Optional.ofNullable(a.stages)),
            PlaceboCodecs.setOf(ResourceLocation.CODEC).fieldOf("dimensions").forGetter(a -> a.dimensions),
            PlaceboCodecs.nullableField(Codec.BOOL, "affixed", false).forGetter(a -> a.affixed),
            PlaceboCodecs.nullableField(SetPredicate.CODEC.listOf(), "valid_gear_sets", Collections.emptyList()).forGetter(a -> a.gearSets),
            PlaceboCodecs.nullableField(NBTAdapter.EITHER_CODEC, "nbt").forGetter(a -> Optional.ofNullable(a.nbt)),
            PlaceboCodecs.nullableField(SupportingEntity.CODEC.listOf(), "supporting_entities", Collections.emptyList()).forGetter(a -> a.support),
            PlaceboCodecs.nullableField(SupportingEntity.CODEC, "mount").forGetter(a -> Optional.ofNullable(a.mount)),
            PlaceboCodecs.nullableField(Exclusion.CODEC.listOf(), "exclusions", Collections.emptyList()).forGetter(a -> a.exclusions),
            PlaceboCodecs.nullableField(Codec.BOOL, "finalize", false).forGetter(a -> a.finalize))
        .apply(inst, ApothMiniboss::new));

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
     * Name of the miniboss. Can be a lang key. Empty or null will cause no name to be set. The special string "use_name_generation" will invoke NameHelper (like
     * normal bosses).
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
     *
     * @see {@link Exclusion}
     */
    protected final List<Exclusion> exclusions;

    /**
     * If true, the SpecialSpawn/FinalizeSpawn event is not cancelled, and {@link Mob#finalizeSpawn} will still be called.<br>
     * Finalization will happen before the miniboss data is applied, since miniboss data is delayed until {@link EntityJoinLevelEvent}.
     */
    protected final boolean finalize;

    public ApothMiniboss(int weight, float quality, float chance,
        String name, Set<EntityType<?>> entities, BossStats stats,
        Optional<Set<String>> stages, Set<ResourceLocation> dimensions, boolean affixed,
        List<SetPredicate> gearSets, Optional<CompoundTag> nbt, List<SupportingEntity> support,
        Optional<SupportingEntity> mount, List<Exclusion> exclusions, boolean finalize) {
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
        this.finalize = finalize;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public float getQuality() {
        return this.quality;
    }

    public float getChance() {
        return this.chance;
    }

    @Override
    public Set<EntityType<?>> getEntities() {
        return this.entities;
    }

    /**
     * Transforms a mob into this miniboss, spawning any supporting entities or mounts as needed.
     *
     * @param mob    The mob being transformed.
     * @param random A random, used for selection of boss stats.
     * @return The newly created boss, or it's mount, if it had one.
     */
    public void transformMiniboss(ServerLevelAccessor level, Mob mob, RandomSource random, float luck) {
        var pos = mob.getPosition(0);
        if (this.nbt != null) {
            if (this.nbt.contains(Entity.PASSENGERS_TAG)) {
                ListTag passengers = this.nbt.getList(Entity.PASSENGERS_TAG, 10);
                for (int i = 0; i < passengers.size(); ++i) {
                    Entity entity = EntityType.loadEntityRecursive(passengers.getCompound(i), level.getLevel(), Function.identity());
                    if (entity != null) {
                        entity.startRiding(mob, true);
                    }
                }
            }
        }
        mob.setPos(pos);
        this.initBoss(random, mob, luck);
        // readAdditionalSaveData should leave unchanged any tags that are not in the NBT data.
        if (this.nbt != null) mob.readAdditionalSaveData(this.nbt);

        if (this.mount != null) {
            Mob mountedEntity = this.mount.create(mob.level(), mob.getX() + 0.5, mob.getY(), mob.getZ() + 0.5);
            mob.startRiding(mountedEntity, true);
            level.addFreshEntity(mountedEntity);
        }

        if (this.support != null) {
            for (var support : this.support) {
                Mob supportingMob = support.create(mob.level(), mob.getX() + 0.5, mob.getY(), mob.getZ() + 0.5);
                level.addFreshEntity(supportingMob);
            }
        }
    }

    /**
     * Initializes an entity as a boss, based on the stats of this BossItem.
     *
     * @param rand
     * @param mob
     */
    public void initBoss(RandomSource rand, Mob mob, float luck) {
        mob.getPersistentData().putBoolean("apoth.miniboss", true);

        int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : this.stats.effects()) {
            if (rand.nextFloat() <= inst.chance()) {
                mob.addEffect(inst.create(rand, duration));
            }
        }

        for (RandomAttributeModifier modif : this.stats.modifiers()) {
            modif.apply(rand, mob);
        }

        if (NAME_GEN.equals(this.name)) {
            NameHelper.setEntityName(rand, mob);
        }
        else if (!Strings.isNullOrEmpty(this.name)) {
            mob.setCustomName(Component.translatable(this.name));
        }

        if (mob.hasCustomName()) mob.setCustomNameVisible(true);

        if (!this.gearSets.isEmpty()) {
            GearSet set = GearSetRegistry.INSTANCE.getRandomSet(rand, luck, this.gearSets);
            Preconditions.checkNotNull(set, String.format("Failed to find a valid gear set for the miniboss %s.", MinibossRegistry.INSTANCE.getKey(this)));
            set.apply(mob);
        }

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

            if (!anyValid) {
                AdventureModule.LOGGER.error("Attempted to affix a miniboss with ID " + MinibossRegistry.INSTANCE.getKey(this) + " but it is not wearing any affixable items!");
                return;
            }

            guaranteed = rand.nextInt(6);

            ItemStack temp = mob.getItemBySlot(EquipmentSlot.values()[guaranteed]);
            while (temp.isEmpty() || LootCategory.forItem(temp) == LootCategory.NONE) {
                guaranteed = rand.nextInt(6);
                temp = mob.getItemBySlot(EquipmentSlot.values()[guaranteed]);
            }

            var rarity = LootRarity.random(rand, luck, AdventureConfig.AFFIX_CONVERT_RARITIES.get(mob.level().dimension().location()));
            ApothBoss.modifyBossItem(temp, rand, mob.hasCustomName() ? mob.getCustomName().getString() : "", luck, rarity, this.stats);
            mob.setCustomName(((MutableComponent) mob.getCustomName()).withStyle(Style.EMPTY.withColor(rarity.getColor())));
            mob.setDropChance(EquipmentSlot.values()[guaranteed], 2F);
        }

        for (EquipmentSlot s : EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(s);
            if (!stack.isEmpty() && s.ordinal() != guaranteed && rand.nextFloat() < this.stats.enchantChance()) {
                ApothBoss.enchantBossItem(rand, stack, Apotheosis.enableEnch ? this.stats.enchLevels()[0] : this.stats.enchLevels()[1], true);
                mob.setItemSlot(s, stack);
            }
        }
        mob.setHealth(mob.getMaxHealth());
    }

    /**
     * Ensures that this boss item does not have null or empty fields that would cause a crash.
     *
     * @return this
     */
    public ApothMiniboss validate(ResourceLocation key) {
        Preconditions.checkArgument(this.weight >= 0, "Miniboss Item " + key + " has a negative weight!");
        Preconditions.checkArgument(this.quality >= 0, "Miniboss Item " + key + " has a negative quality!");
        Preconditions.checkNotNull(this.entities, "Miniboss Item " + key + " has null entity match list!");
        Preconditions.checkNotNull(this.stats, "Miniboss Item " + key + " has no stats!");
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
    public Codec<? extends ApothMiniboss> getCodec() {
        return CODEC;
    }

    public boolean requiresNbtAccess() {
        return this.exclusions.stream().anyMatch(Exclusion::requiresNbtAccess);
    }

    public boolean isExcluded(Mob mob, ServerLevelAccessor level, MobSpawnType type) {
        CompoundTag tag = this.requiresNbtAccess() ? mob.saveWithoutId(new CompoundTag()) : null;
        return this.exclusions.stream().anyMatch(ex -> ex.isExcluded(mob, level, type, tag));
    }

    public boolean shouldFinalize() {
        return this.finalize;
    }

}
