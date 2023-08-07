package dev.shadowsoffire.apotheosis.adventure;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisCommandEvent;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.TelepathicAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemManager;
import dev.shadowsoffire.apotheosis.adventure.commands.BossCommand;
import dev.shadowsoffire.apotheosis.adventure.commands.CategoryCheckCommand;
import dev.shadowsoffire.apotheosis.adventure.commands.GemCommand;
import dev.shadowsoffire.apotheosis.adventure.commands.LootifyCommand;
import dev.shadowsoffire.apotheosis.adventure.commands.ModifierCommand;
import dev.shadowsoffire.apotheosis.adventure.commands.RarityCommand;
import dev.shadowsoffire.apotheosis.adventure.commands.SocketCommand;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.util.ItemAccess;
import dev.shadowsoffire.placebo.events.AnvilLandEvent;
import dev.shadowsoffire.placebo.events.GetEnchantmentLevelEvent;
import dev.shadowsoffire.placebo.events.ItemUseEvent;
import dev.shadowsoffire.placebo.reload.WeightedJsonReloadListener.IDimensional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AdventureEvents {

    @SubscribeEvent
    public void cmds(ApotheosisCommandEvent e) {
        RarityCommand.register(e.getRoot());
        CategoryCheckCommand.register(e.getRoot());
        LootifyCommand.register(e.getRoot());
        ModifierCommand.register(e.getRoot());
        GemCommand.register(e.getRoot());
        SocketCommand.register(e.getRoot());
        BossCommand.register(e.getRoot());
    }

    private static final UUID HEAVY_WEAPON_AS = UUID.fromString("f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454");

    @SubscribeEvent
    public void affixModifiers(ItemAttributeModifierEvent e) {
        ItemStack stack = e.getItemStack();
        if (stack.hasTag()) {
            Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
            affixes.forEach((afx, inst) -> inst.addModifiers(e.getSlotType(), e::addModifier));
            if (AffixHelper.getRarity(stack) != null && LootCategory.forItem(stack) == LootCategory.HEAVY_WEAPON && e.getSlotType() == EquipmentSlot.MAINHAND) {
                double amt = -0.15 - 0.10 * AffixHelper.getRarity(stack).ordinal();
                AttributeModifier baseAS = e.getModifiers().get(Attributes.ATTACK_SPEED).stream().filter(a -> ItemAccess.getBaseAS() == a.getId()).findFirst().orElse(null);
                if (baseAS != null) {
                    // Try to not reduce attack speed below 0.4 if possible.
                    double value = 4 + baseAS.getAmount();
                    double clampedAmt = 0.4F / value - 1;
                    amt = Math.max(amt, clampedAmt);
                    if (amt >= 0) return;
                }
                e.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(HEAVY_WEAPON_AS, "Heavy Weapon AS", amt, Operation.MULTIPLY_TOTAL));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void preventBossSuffocate(LivingHurtEvent e) {
        if (e.getSource().is(DamageTypes.IN_WALL) && e.getEntity().getPersistentData().contains("apoth.boss")) {
            e.setCanceled(true);
        }
    }

    /**
     * This event handler allows affixes to react to arrows being fired to trigger additional actions.
     * Arrows marked as "apoth.generated" will not trigger the affix hook, so affixes can fire arrows without recursion.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void fireArrow(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof AbstractArrow arrow && !arrow.getPersistentData().getBoolean("apoth.generated")) {
            Entity shooter = arrow.getOwner();
            if (shooter instanceof LivingEntity living) {
                ItemStack bow = living.getUseItem();
                if (bow.isEmpty()) {
                    bow = living.getMainHandItem();
                    if (bow.isEmpty() || !LootCategory.forItem(bow).isRanged()) {
                        bow = living.getOffhandItem();
                    }
                }
                if (bow.isEmpty()) return;
                Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(bow);
                affixes.values().forEach(a -> {
                    a.onArrowFired(living, arrow);
                });
                AffixHelper.copyFrom(bow, arrow);
            }
        }
    }

    /**
     * This event handler allows affixes to react to arrows hitting something.
     */
    @SubscribeEvent
    public void impact(ProjectileImpactEvent e) {
        if (e.getProjectile() instanceof AbstractArrow arrow) {
            Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(arrow);
            affixes.values().forEach(inst -> inst.onArrowImpact(arrow, e.getRayTraceResult(), e.getRayTraceResult().getType()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDamage(LivingHurtEvent e) {
        Apoth.Affixes.MAGICAL.getOptional().ifPresent(afx -> afx.onHurt(e));
        DamageSource src = e.getSource();
        LivingEntity ent = e.getEntity();
        float amount = e.getAmount();
        for (ItemStack s : ent.getAllSlots()) {
            Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(s);
            for (AffixInstance inst : affixes.values()) {
                amount = inst.onHurt(src, ent, amount);
            }
        }
        e.setAmount(amount);
    }

    @SubscribeEvent
    public void onItemUse(ItemUseEvent e) {
        ItemStack s = e.getItemStack();
        Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(s);
        for (AffixInstance inst : affixes.values()) {
            InteractionResult type = inst.onItemUse(e.getContext());
            if (type != null) {
                e.setCanceled(true);
                e.setCancellationResult(type);
            }
        }
    }

    @SubscribeEvent
    public void shieldBlock(ShieldBlockEvent e) {
        ItemStack stack = e.getEntity().getUseItem();
        Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        float blocked = e.getBlockedDamage();
        for (AffixInstance inst : affixes.values()) {
            blocked = inst.onShieldBlock(e.getEntity(), e.getDamageSource(), blocked);
        }
        if (blocked != e.getOriginalBlockedDamage()) e.setBlockedDamage(blocked);
    }

    @SubscribeEvent
    public void blockBreak(BreakEvent e) {
        ItemStack stack = e.getPlayer().getMainHandItem();
        Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        for (AffixInstance inst : affixes.values()) {
            inst.onBlockBreak(e.getPlayer(), e.getLevel(), e.getPos(), e.getState());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void dropsHigh(LivingDropsEvent e) {
        if (e.getSource().getEntity() instanceof ServerPlayer p && e.getEntity() instanceof Monster) {
            if (p instanceof FakePlayer) return;
            float chance = AdventureConfig.gemDropChance + (e.getEntity().getPersistentData().contains("apoth.boss") ? AdventureConfig.gemBossBonus : 0);
            if (p.random.nextFloat() <= chance) {
                Entity ent = e.getEntity();
                e.getDrops()
                    .add(new ItemEntity(ent.level(), ent.getX(), ent.getY(), ent.getZ(), GemManager.createRandomGemStack(p.random, (ServerLevel) p.level(), p.getLuck(), IDimensional.matches(p.level()), IStaged.matches(p)), 0, 0, 0));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void drops(LivingDropsEvent e) {
        Apoth.Affixes.FESTIVE.getOptional().ifPresent(afx -> afx.drops(e));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void deathMark(LivingDeathEvent e) {
        Apoth.Affixes.FESTIVE.getOptional().ifPresent(afx -> afx.markEquipment(e));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void dropsLowest(LivingDropsEvent e) {
        TelepathicAffix.drops(e);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void festive_removeMarker(LivingDropsEvent e) {
        Apoth.Affixes.FESTIVE.getOptional().ifPresent(afx -> afx.removeMarker(e));
    }

    @SubscribeEvent
    public void harvest(HarvestCheck e) {
        Apoth.Affixes.OMNETIC.getOptional().ifPresent(afx -> afx.harvest(e));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void speed(BreakSpeed e) {
        Apoth.Affixes.OMNETIC.getOptional().ifPresent(afx -> afx.speed(e));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBreak(BlockEvent.BreakEvent e) {
        Apoth.Affixes.RADIAL.getOptional().ifPresent(afx -> afx.onBreak(e));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void special(SpecialSpawn e) {
        if (e.getSpawnReason() == MobSpawnType.NATURAL && e.getLevel().getRandom().nextFloat() <= AdventureConfig.randomAffixItem && e.getEntity() instanceof Monster) {
            Player player = e.getLevel().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
            if (player == null) return;
            ItemStack affixItem = LootController.createRandomLootItem(e.getLevel().getRandom(), null, player, (ServerLevel) e.getEntity().level);
            if (affixItem.isEmpty()) return;
            affixItem.getOrCreateTag().putBoolean("apoth_rspawn", true);
            LootCategory cat = LootCategory.forItem(affixItem);
            EquipmentSlot slot = cat.getSlots()[0];
            e.getEntity().setItemSlot(slot, affixItem);
            e.getEntity().setGuaranteedDrop(slot);
        }
    }

    @SubscribeEvent
    public void gemSmashing(AnvilLandEvent e) {
        Level level = e.getLevel();
        BlockPos pos = e.getPos();
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos, pos.offset(1, 1, 1)));
        for (ItemEntity ent : items) {
            ItemStack stack = ent.getItem();
            if (stack.getItem() == Apoth.Items.GEM.get()) {
                ent.setItem(new ItemStack(Apoth.Items.GEM_DUST.get(), stack.getCount()));
            }
        }
    }

    /**
     * {@link AffixHelper#getAffixesImpl} can cause infinite loops when doing validation that ends up depending on the enchantments of an item.<br>
     * We use this to disable enchantment level boosting when recurring (it shouldn't be relevant for these cases anyway).
     */
    private static ThreadLocal<AtomicBoolean> reentrantLock = ThreadLocal.withInitial(() -> new AtomicBoolean(false));

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void enchLevels(GetEnchantmentLevelEvent e) {
        boolean isReentrant = reentrantLock.get().getAndSet(true);
        if (isReentrant) return;
        AffixHelper.streamAffixes(e.getStack()).forEach(inst -> inst.getEnchantmentLevels(e.getEnchantments()));
        reentrantLock.get().set(false);
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void update(LivingTickEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity.getPersistentData().contains("apoth.burns_in_sun")) {
            // Copy of Mob#isSunBurnTick()
            if (entity.level().isDay() && !entity.level().isClientSide) {
                float f = entity.getLightLevelDependentMagicValue();
                BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
                boolean flag = entity.isInWaterRainOrBubble() || entity.isInPowderSnow || entity.wasInPowderSnow;
                if (f > 0.5F && entity.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !flag && entity.level().canSeeSky(blockpos)) {
                    entity.setSecondsOnFire(8);
                }
            }
        }
    }

}
