package dev.shadowsoffire.apotheosis.mobs.types;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry.IEntityMatch;
import dev.shadowsoffire.apotheosis.mobs.util.AffixData;
import dev.shadowsoffire.apotheosis.mobs.util.BasicBossData;
import dev.shadowsoffire.apotheosis.mobs.util.BossStats;
import dev.shadowsoffire.apotheosis.mobs.util.SupportingEntity;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;

public record Elite(BasicBossData basicData, float chance, HolderSet<EntityType<?>> entities, BossStats stats, AffixData afxData) implements CodecProvider<Elite>, Constrained, Weighted, IEntityMatch {

    /**
     * NBT key for a boolean value applied to entity persistent data to indicate a mob is a miniboss.
     */
    public static final String MINIBOSS_KEY = "apoth.miniboss";

    /**
     * NBT key for a string value applied to entity persistent data indicating the player that trigger's a miniboss's summoning.
     * <p>
     * Used to resolve the player when the miniboss is added to the world and initialized.
     */
    public static final String PLAYER_KEY = MINIBOSS_KEY + ".player";

    public static final Codec<Elite> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BasicBossData.CODEC.fieldOf("basic_data").forGetter(Elite::basicData),
            Codec.floatRange(0, 1).fieldOf("success_chance").forGetter(Elite::chance),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Elite::entities),
            BossStats.CODEC.fieldOf("stats").forGetter(Elite::stats),
            AffixData.CODEC.optionalFieldOf("affix_data", AffixData.DEFAULT).forGetter(Elite::afxData))
        .apply(inst, Elite::new));

    @Override
    public TieredWeights weights() {
        return this.basicData.weights();
    }

    @Override
    public Constraints constraints() {
        return this.basicData.constraints();
    }

    public float getChance() {
        return this.chance;
    }

    @Override
    public HolderSet<EntityType<?>> getEntities() {
        return this.entities;
    }

    /**
     * Transforms a mob into this miniboss, spawning any supporting entities or mounts as needed.
     *
     * @param mob    The mob being transformed.
     * @param random A random, used for selection of boss stats.
     * @return The newly created boss, or it's mount, if it had one.
     */
    public void transformMiniboss(ServerLevelAccessor level, Mob mob, GenContext ctx) {
        Vec3 pos = mob.getPosition(0);

        Optional<CompoundTag> optNbt = this.basicData.nbt();
        if (optNbt.isPresent()) {
            // Since the mob already exists, we need to load all the entities in the passengers tab.
            CompoundTag nbt = optNbt.get();
            if (nbt.contains(Entity.PASSENGERS_TAG)) {
                ListTag passengers = nbt.getList(Entity.PASSENGERS_TAG, 10);
                for (int i = 0; i < passengers.size(); ++i) {
                    Entity entity = EntityType.loadEntityRecursive(passengers.getCompound(i), level.getLevel(), Function.identity());
                    if (entity != null) {
                        entity.setPos(pos);
                        level.addFreshEntityWithPassengers(entity);
                        entity.startRiding(mob, true);
                    }
                }
            }
        }

        mob.setPos(pos);
        this.initElite(mob, ctx);

        // readAdditionalSaveData should leave unchanged any tags that are not in the NBT data.
        if (optNbt.isPresent()) {
            mob.readAdditionalSaveData(optNbt.get());
        }

        if (this.basicData.hasMount()) {
            Mob mount = this.basicData().createMount(level, BlockPos.containing(pos), mob);
            level.addFreshEntity(mount);
        }

        for (SupportingEntity support : this.basicData.support()) {
            // TODO: Improve spawning algorithm instead of spawning all mobs directly on top of the main entity.
            // Probably best to steal the inward spiral from Gateways.
            Mob supportingMob = support.create(mob.level(), mob.getX() + 0.5, mob.getY(), mob.getZ() + 0.5);
            level.addFreshEntity(supportingMob);
        }
    }

    /**
     * Initializes an entity as a boss, based on the stats of this BossItem.
     *
     * @param rand
     * @param mob
     */
    public void initElite(Mob mob, GenContext ctx) {
        RandomSource rand = ctx.rand();
        mob.getPersistentData().putBoolean("apoth.miniboss", true);

        int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : this.stats.effects()) {
            if (rand.nextFloat() <= inst.chance()) {
                mob.addEffect(inst.create(rand, duration));
            }
        }

        int i = 0;
        for (RandomAttributeModifier modif : this.stats.modifiers()) {
            modif.apply(this.createAttributeModifierId(i++), rand, mob);
        }

        this.basicData.applyEntityName(rand, mob);

        this.basicData.applyGearSet(mob, ctx);

        EquipmentSlot affixedSlot = this.afxData.applyTo(mob, ctx, this.stats.enchLevels().primary(), true);

        for (EquipmentSlot s : EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(s);
            if (!stack.isEmpty() && s != affixedSlot && rand.nextFloat() < this.stats.enchantChance()) {
                Invader.enchantBossItem(rand, stack, this.stats.enchLevels().secondary(), true, mob.level().registryAccess());
                mob.setItemSlot(s, stack);
            }
        }

        mob.setHealth(mob.getMaxHealth());

        this.basicData.appendBonusLoot(mob);
    }

    @Override
    public Codec<? extends Elite> getCodec() {
        return CODEC;
    }

    protected ResourceLocation createAttributeModifierId(int index) {
        ResourceLocation key = EliteRegistry.INSTANCE.getKey(this);
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), Invader.INVADER_ATTR_PREFIX + key.getPath() + "_modif_" + index);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BasicBossData basicData;
        private float chance = -1;
        private HolderSet<EntityType<?>> entities = null;
        private BossStats stats;
        private AffixData afxData = AffixData.DEFAULT;

        public Builder basicData(UnaryOperator<BasicBossData.Builder> config) {
            this.basicData = config.apply(BasicBossData.builder()).build();
            return this;
        }

        public Builder chance(float chance) {
            this.chance = chance;
            return this;
        }

        public Builder entities(HolderSet<EntityType<?>> entities) {
            if (this.entities == null) {
                this.entities = entities;
            }
            else {
                this.entities = new OrHolderSet<>(this.entities, entities);
            }
            return this;
        }

        public Builder entities(TagKey<EntityType<?>> entities) {
            return this.entities(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(entities));
        }

        @SafeVarargs
        @SuppressWarnings("deprecation")
        public final Builder entities(EntityType<? extends Mob>... entities) {
            return this.entities(HolderSet.direct(EntityType::builtInRegistryHolder, entities));
        }

        public Builder stats(UnaryOperator<BossStats.Builder> config) {
            this.stats = config.apply(BossStats.builder()).build();
            return this;
        }

        public Builder affixes(float chance, Set<LootRarity> rarities) {
            this.afxData = new AffixData(chance, rarities);
            return this;
        }

        public Elite build() {
            if (basicData == null) {
                throw new IllegalStateException("BasicBossData must be set");
            }
            if (chance <= 0) {
                throw new IllegalStateException("Chance value must be positive");
            }
            if (this.entities == null) {
                throw new IllegalStateException("Entities must be set");
            }
            if (stats == null) {
                throw new IllegalStateException("Stats must be set");
            }
            return new Elite(basicData, chance, entities, stats, afxData);
        }
    }

}
