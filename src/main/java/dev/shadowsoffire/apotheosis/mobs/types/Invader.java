package dev.shadowsoffire.apotheosis.mobs.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.util.BasicBossData;
import dev.shadowsoffire.apotheosis.mobs.util.BossStats;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apothic_attributes.modifiers.EquipmentSlotCompat;
import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
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
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.connection.ConnectionType;

/**
 * An Invader is a preset entity with per-rarity stats that will spawn with a full gear set and an equipped affix item.
 * <p>
 * Invaders piggyback off of normal spawns, cancelling the original and spawning in the invader, instead of spinning up a new spawn mechanism.
 *
 * @param basicData The basic boss data for the spawned entity
 * @param entity    The type of spawned entity
 * @param size      The AABB of the spawned entity, accounting for any mounts or supports.
 * @param stats     The per-rarity stats for this invader.
 */
public record Invader(BasicBossData basicData, EntityType<?> entity, AABB size, Map<LootRarity, BossStats> stats) implements CodecProvider<Invader>, Constrained, Weighted {

    /**
     * NBT key for a boolean value applied to entity persistent data to indicate a mob is an apoth boss.
     */
    public static final String BOSS_KEY = "apoth.boss";

    /**
     * NBT key for a string value applied to entity persistent data indicating a boss's rarity.
     */
    public static final String RARITY_KEY = BOSS_KEY + ".rarity";

    public static final String INVADER_ATTR_PREFIX = "apothic_invader_";

    public static final Codec<Invader> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BasicBossData.CODEC.fieldOf("basic_data").forGetter(Invader::basicData),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(Invader::entity),
            BasicBossData.AABB_CODEC.fieldOf("size").forGetter(Invader::size),
            LootRarity.mapCodec(BossStats.CODEC).fieldOf("stats").forGetter(Invader::stats))
        .apply(inst, Invader::new));

    public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal && ((NearestAttackableTargetGoal<?>) a).targetType == Villager.class;

    public BasicBossData basicData() {
        return this.basicData;
    }

    @Override
    public TieredWeights weights() {
        return this.basicData.weights();
    }

    @Override
    public Constraints constraints() {
        return this.basicData.constraints();
    }

    /**
     * @see #createBoss(ServerLevelAccessor, BlockPos, RandomSource, float, LootRarity)
     */
    public Mob createBoss(ServerLevelAccessor world, BlockPos pos, GenContext ctx) {
        return this.createBoss(world, pos, ctx, null);
    }

    /**
     * Generates (but does not spawn) the result of this BossItem.
     *
     * @param level  The level to create the entity in.
     * @param pos    The location to place the entity. Will be centered (+0.5, +0.5).
     * @param ctx    The generation context, used for selection of boss stats and equipment.
     * @param rarity A rarity override. This will be clamped to a valid rarity, and randomly generated if null.
     * @return The newly created boss, or it's mount, if present.
     */
    public Mob createBoss(ServerLevelAccessor level, BlockPos pos, GenContext ctx, @Nullable LootRarity rarity) {
        Optional<CompoundTag> nbt = this.basicData.nbt();
        CompoundTag fakeNbt = nbt.map(CompoundTag::copy).orElse(new CompoundTag());
        fakeNbt.putString("id", EntityType.getKey(this.entity).toString());
        Mob entity = (Mob) EntityType.loadEntityRecursive(fakeNbt, level.getLevel(), Function.identity());

        this.initBoss(entity, ctx, rarity);

        if (this.basicData.finalizeSpawn()) {
            // TODO: Implement finalize support for invaders. Needs to not fire the event, or somehow recursively fire the event without reentrancy.
        }

        // Re-read here so we can apply certain things after the boss has been modified
        // But only mob-specific things, not a full load()
        if (nbt.isPresent()) {
            entity.readAdditionalSaveData(nbt.get());
        }

        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.rand().nextFloat() * 360.0F, 0.0F);

        if (this.basicData.hasMount()) {
            entity = this.basicData.createMount(level, pos, entity);
        }

        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.rand().nextFloat() * 360.0F, 0.0F);

        // TODO: Implement supporting entities here. Need to return the boss *and* the supports for spawning.
        return entity;
    }

    /**
     * Initializes an entity as a boss, based on the stats of this BossItem.
     *
     * @param rand
     * @param mob
     */
    public void initBoss(Mob mob, GenContext ctx, @Nullable LootRarity rarity) {
        RandomSource rand = ctx.rand();

        if (rarity == null) {
            rarity = LootRarity.random(ctx, this.stats.keySet());
        }

        BossStats stats = this.stats.get(rarity);
        int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : stats.effects()) {
            if (rand.nextFloat() <= inst.chance()) {
                mob.addEffect(inst.create(rand, duration));
            }
        }

        int i = 0;
        for (RandomAttributeModifier modif : stats.modifiers()) {
            modif.apply(this.createAttributeModifierId(i++), rand, mob);
        }

        mob.goalSelector.getAvailableGoals().removeIf(IS_VILLAGER_ATTACK);

        this.basicData.applyEntityName(rand, mob);

        GearSet set = this.basicData.applyGearSet(mob, ctx);

        if (set != null) {
            boolean anyValid = false;

            for (EquipmentSlot t : EquipmentSlot.values()) {
                ItemStack s = mob.getItemBySlot(t);
                if (!s.isEmpty() && !LootCategory.forItem(s).isNone()) {
                    anyValid = true;
                    break;
                }
            }

            if (!anyValid) {
                Apotheosis.LOGGER.error("Attempted to apply boss gear set " + GearSetRegistry.INSTANCE.getKey(set) + " but it had no valid affix loot items generated.");
            }
        }
        else {
            // We didn't apply an armor set to this invader. We still need to generate an affix item, so we'll pull one at random and equip it.
            ItemStack affixItem = LootController.createRandomLootItem(ctx, rarity);
            LootCategory cat = LootCategory.forItem(affixItem);
            EquipmentSlot slot = Arrays.stream(EquipmentSlot.values())
                .filter(eSlot -> cat.getSlots().test(EquipmentSlotCompat.fromVanilla(eSlot)))
                .findAny()
                .orElse(EquipmentSlot.MAINHAND);
            mob.setItemSlot(slot, affixItem);
        }

        EquipmentSlot[] slots = EquipmentSlot.values();

        EquipmentSlot guaranteed = slots[rand.nextInt(6)];
        int tries = 50;

        ItemStack temp = mob.getItemBySlot(guaranteed);
        while (temp.isEmpty() || LootCategory.forItem(temp) == LootCategories.NONE) {
            guaranteed = slots[rand.nextInt(6)];
            temp = mob.getItemBySlot(guaranteed);

            if (tries-- <= 0) {
                break; // Shouldn't happen, but we can't deadlock here.
            }
        }

        for (EquipmentSlot s : EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(s);
            if (stack.isEmpty()) {
                continue;
            }

            if (s == guaranteed) {
                mob.setDropChance(s, 2F);
                mob.setItemSlot(s, modifyBossItem(stack, mob.getName(), ctx, rarity, stats.enchLevels().primary(), mob.level().registryAccess()));
                mob.setCustomName(mob.getName().copy().withStyle(Style.EMPTY.withColor(rarity.color())));
            }
            else if (rand.nextFloat() < stats.enchantChance()) {
                enchantBossItem(rand, stack, stats.enchLevels().secondary(), true, mob.level().registryAccess());
                mob.setItemSlot(s, stack);
            }
        }

        mob.getPersistentData().putBoolean(BOSS_KEY, true);
        mob.getPersistentData().putString(RARITY_KEY, RarityRegistry.INSTANCE.getKey(rarity).toString());
        mob.setHealth(mob.getMaxHealth());

        if (AdventureConfig.bossGlowOnSpawn) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 3600, 0, true, false));
        }

        this.basicData.appendBonusLoot(mob);
    }

    @Override
    public Codec<? extends Invader> getCodec() {
        return CODEC;
    }

    protected ResourceLocation createAttributeModifierId(int index) {
        ResourceLocation key = InvaderRegistry.INSTANCE.getKey(this);
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), INVADER_ATTR_PREFIX + key.getPath() + "_modif_" + index);
    }

    public static void enchantBossItem(RandomSource rand, ItemStack stack, int level, boolean treasure, RegistryAccess reg) {
        Stream<Holder<Enchantment>> available = reg.registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT).map(HolderSet::stream).orElse(Stream.empty());
        List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(rand, stack, level, available);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
        ench.stream().filter(d -> !d.enchantment.is(EnchantmentTags.CURSE)).forEach(i -> builder.upgrade(i.enchantment, i.level));
        EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
    }

    public static ItemStack modifyBossItem(ItemStack stack, Component bossName, GenContext ctx, LootRarity rarity, int enchLevel, RegistryAccess reg) {
        RandomSource rand = ctx.rand();
        if (enchLevel > 0) {
            enchantBossItem(rand, stack, enchLevel, true, reg);
        }
        NameHelper.setItemName(rand, stack);
        stack = LootController.createLootItem(stack, LootCategory.forItem(stack), rarity, ctx);

        // Upgrade all affixes on Invader items by 10-25%
        ItemAffixes.Builder builder = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY).toBuilder();
        for (DynamicHolder<Affix> afx : builder.keySet()) {
            builder.upgrade(afx, Math.min(Affix.STANDARD_MAX_LEVEL, builder.getLevel(afx) + Mth.nextFloat(rand, 0.1F, 0.25F)));
        }
        AffixHelper.setAffixes(stack, builder.build());

        Component bossOwnerName = Component.translatable(NameHelper.ownershipFormat, bossName);
        Component name = AffixHelper.getName(stack);
        if (name.getContents() instanceof TranslatableContents tc) {
            String oldKey = tc.getKey();
            String newKey = "misc.apotheosis.affix_name.two".equals(oldKey) ? "misc.apotheosis.affix_name.three" : "misc.apotheosis.affix_name.four";
            Object[] newArgs = new Object[tc.getArgs().length + 1];
            newArgs[0] = bossOwnerName;
            for (int i = 1; i < newArgs.length; i++) {
                newArgs[i] = tc.getArgs()[i - 1];
            }
            Component copy = Component.translatable(newKey, newArgs).withStyle(name.getStyle().withItalic(false));

            // We need to ensure things aren't entangled, but we can't otherwise make a deep copy of the TranslatableContents.
            // So... we just serialize the entire thing and deserialize it. Shouldn't be too expensive to run here.
            var rfbb = new RegistryFriendlyByteBuf(Unpooled.buffer(), reg, ConnectionType.NEOFORGE);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(rfbb, copy);
            Component deserialized = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(rfbb);

            AffixHelper.setName(stack, deserialized);
        }

        ItemEnchantments.Mutable enchMap = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            if (e.getKey() != null) {
                enchMap.upgrade(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey().value()), e.getIntValue() + rand.nextInt(2)));
            }
        }

        if (AdventureConfig.curseBossItems) {
            List<Holder.Reference<Enchantment>> curses = reg.registryOrThrow(Registries.ENCHANTMENT).holders().filter(e -> e.is(EnchantmentTags.CURSE) && e.is(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)).toList();
            if (!curses.isEmpty()) {
                Holder<Enchantment> curse = curses.get(rand.nextInt(curses.size()));
                enchMap.upgrade(curse, Mth.nextInt(rand, 1, EnchHooks.getMaxLevel(curse.value())));
            }
        }

        EnchantmentHelper.setEnchantments(stack, enchMap.toImmutable());
        stack.set(Components.FROM_BOSS, true);
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BasicBossData basicData;
        private EntityType<? extends Mob> entity;
        private AABB size;
        private Map<LootRarity, BossStats> stats = new HashMap<>();

        public Builder basicData(UnaryOperator<BasicBossData.Builder> config) {
            this.basicData = config.apply(BasicBossData.builder()).build();
            return this;
        }

        public Builder entity(EntityType<? extends Mob> entity) {
            this.entity = entity;
            return this;
        }

        public Builder size(double width, double height) {
            this.size = new AABB(0, 0, 0, width, height, width);
            return this;
        }

        public Builder stats(LootRarity rarity, UnaryOperator<BossStats.Builder> config) {
            this.stats.put(rarity, config.apply(BossStats.builder()).build());
            return this;
        }

        public Invader build() {
            if (basicData == null) {
                throw new IllegalStateException("BasicBossData must be set");
            }
            if (entity == null) {
                throw new IllegalStateException("Entity type must be set");
            }
            if (size == null) {
                throw new IllegalStateException("Size must be set");
            }
            if (stats.isEmpty()) {
                throw new IllegalStateException("Stats must not be empty");
            }
            return new Invader(basicData, entity, size, stats);
        }
    }

}
