package dev.shadowsoffire.apotheosis.adventure;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Predicates;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisCommandEvent;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.TelepathicAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingMenu;
import dev.shadowsoffire.apotheosis.adventure.commands.AffixCommand;
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
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.events.AnvilLandEvent;
import dev.shadowsoffire.placebo.events.GetEnchantmentLevelEvent;
import dev.shadowsoffire.placebo.events.ItemUseEvent;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.AbstractGolem;
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
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.Event.Result;
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
        AffixCommand.register(e.getRoot());
    }

    @SubscribeEvent
    public void affixModifiers(ItemAttributeModifierEvent e) {
        ItemStack stack = e.getItemStack();
        if (stack.hasTag()) {
            SocketHelper.getGems(stack).addModifiers(LootCategory.forItem(stack), e.getSlotType(), e::addModifier);

            var affixes = AffixHelper.getAffixes(stack);
            affixes.forEach((afx, inst) -> inst.addModifiers(e.getSlotType(), e::addModifier));
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
            if (arrow.getOwner() instanceof LivingEntity user) {
                ItemStack bow = user.getUseItem();
                if (bow.isEmpty()) {
                    bow = user.getMainHandItem();
                    if (bow.isEmpty() || !LootCategory.forItem(bow).isRanged()) {
                        bow = user.getOffhandItem();
                    }
                }
                if (bow.isEmpty()) return;
                SocketHelper.getGems(bow).onArrowFired(user, arrow);
                AffixHelper.streamAffixes(bow).forEach(a -> {
                    a.onArrowFired(user, arrow);
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
            SocketHelper.getGemInstances(arrow).forEach(inst -> inst.onArrowImpact(arrow, e.getRayTraceResult()));

            var affixes = AffixHelper.getAffixes(arrow);
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
            amount = SocketHelper.getGems(s).onHurt(src, ent, amount);

            var affixes = AffixHelper.getAffixes(s);
            for (AffixInstance inst : affixes.values()) {
                amount = inst.onHurt(src, ent, amount);
            }
        }
        e.setAmount(amount);
    }

    @SubscribeEvent
    public void onItemUse(ItemUseEvent e) {
        ItemStack s = e.getItemStack();
        InteractionResult socketRes = SocketHelper.getGems(s).onItemUse(e.getContext());
        if (socketRes != null) {
            e.setCanceled(true);
            e.setCancellationResult(socketRes);
        }

        InteractionResult afxRes = AffixHelper.streamAffixes(s).map(afx -> afx.onItemUse(e.getContext())).filter(Predicates.notNull()).findFirst().orElse(null);
        if (afxRes != null) {
            e.setCanceled(true);
            e.setCancellationResult(afxRes);
        }
    }

    @SubscribeEvent
    public void shieldBlock(ShieldBlockEvent e) {
        ItemStack stack = e.getEntity().getUseItem();
        var affixes = AffixHelper.getAffixes(stack);
        float blocked = e.getBlockedDamage();
        blocked = SocketHelper.getGems(stack).onShieldBlock(e.getEntity(), e.getDamageSource(), blocked);

        for (AffixInstance inst : affixes.values()) {
            blocked = inst.onShieldBlock(e.getEntity(), e.getDamageSource(), blocked);
        }
        if (blocked != e.getOriginalBlockedDamage()) e.setBlockedDamage(blocked);
    }

    @SubscribeEvent
    public void blockBreak(BreakEvent e) {
        ItemStack stack = e.getPlayer().getMainHandItem();
        SocketHelper.getGems(stack).onBlockBreak(e.getPlayer(), e.getLevel(), e.getPos(), e.getState());
        AffixHelper.streamAffixes(stack).forEach(inst -> {
            inst.onBlockBreak(e.getPlayer(), e.getLevel(), e.getPos(), e.getState());
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void dropsHigh(LivingDropsEvent e) {
        if (e.getSource().getEntity() instanceof ServerPlayer p && e.getEntity() instanceof Monster) {
            if (p instanceof FakePlayer) return;
            float chance = AdventureConfig.gemDropChance + (e.getEntity().getPersistentData().contains("apoth.boss") ? AdventureConfig.gemBossBonus : 0);
            if (p.random.nextFloat() <= chance) {
                Entity ent = e.getEntity();
                e.getDrops()
                    .add(new ItemEntity(ent.level(), ent.getX(), ent.getY(), ent.getZ(), GemRegistry.createRandomGemStack(p.random, (ServerLevel) p.level(), p.getLuck(), IDimensional.matches(p.level()), IStaged.matches(p)), 0, 0, 0));
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
    public void special(FinalizeSpawn e) {
        if (e.getSpawnType() == MobSpawnType.NATURAL && e.getLevel().getRandom().nextFloat() <= AdventureConfig.randomAffixItem && e.getEntity() instanceof Monster) {
            Player player = e.getLevel().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
            if (player == null) return;
            ItemStack affixItem = LootController.createRandomLootItem(e.getLevel().getRandom(), null, player, (ServerLevel) e.getEntity().level());
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
            if (stack.getItem() == Items.GEM.get()) {
                ent.setItem(new ItemStack(Items.GEM_DUST.get(), stack.getCount()));
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
        SocketHelper.getGems(e.getStack()).getEnchantmentLevels(e.getEnchantments());

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

    /**
     * Allows bosses that descend from {@link AbstractGolem} to despawn naturally, only after they have existed for 10 minutes.
     * Without this, they'll pile up forever - https://github.com/Shadows-of-Fire/Apotheosis/issues/1248
     */
    @SubscribeEvent
    public void despawn(MobSpawnEvent.AllowDespawn e) {
        if (e.getEntity() instanceof AbstractGolem g && g.tickCount > 12000 && g.getPersistentData().getBoolean("apoth.boss")) {
            Entity player = g.level().getNearestPlayer(g, -1.0D);
            if (player != null) {
                double dist = player.distanceToSqr(g);
                int despawnDist = g.getType().getCategory().getDespawnDistance();
                int dsDistSq = despawnDist * despawnDist;
                if (dist > dsDistSq) {
                    e.setResult(Result.ALLOW);
                }
            }
        }
    }

    /**
     * Copy the reforge seed on clone (death or otherwise) to prevent access to free reforge rerolls.
     */
    @SubscribeEvent
    public void clone(PlayerEvent.Clone e) {
        int oldSeed = e.getOriginal().getPersistentData().getInt(ReforgingMenu.REFORGE_SEED);
        e.getEntity().getPersistentData().putInt(ReforgingMenu.REFORGE_SEED, oldSeed);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void removeCloudsOnDeath(LivingDeathEvent e) {
        if (e.getEntity().getPersistentData().getBoolean("apoth.miniboss")) {
            for (Entity passenger : e.getEntity().getPassengers()) {
                if (passenger instanceof AreaEffectCloud cloud) {
                    cloud.discard();
                }
            }
        }
    }

}
