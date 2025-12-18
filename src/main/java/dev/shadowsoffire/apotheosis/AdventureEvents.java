package dev.shadowsoffire.apotheosis;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig.ConfigPayload;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.effect.FestiveAffix;
import dev.shadowsoffire.apotheosis.affix.effect.MagicalArrowAffix;
import dev.shadowsoffire.apotheosis.affix.effect.OmneticAffix;
import dev.shadowsoffire.apotheosis.affix.effect.RadialAffix;
import dev.shadowsoffire.apotheosis.affix.effect.TelepathicAffix;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingMenu;
import dev.shadowsoffire.apotheosis.commands.AffixCommand;
import dev.shadowsoffire.apotheosis.commands.BossCommand;
import dev.shadowsoffire.apotheosis.commands.CategoryCheckCommand;
import dev.shadowsoffire.apotheosis.commands.DebugWeightCommand;
import dev.shadowsoffire.apotheosis.commands.GemCommand;
import dev.shadowsoffire.apotheosis.commands.RarityCommand;
import dev.shadowsoffire.apotheosis.commands.ReforgeCommand;
import dev.shadowsoffire.apotheosis.commands.SocketCommand;
import dev.shadowsoffire.apotheosis.commands.WorldTierCommand;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.net.RadialStatePayload;
import dev.shadowsoffire.apotheosis.net.WorldTierPayload;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.OmneticBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.RadialBonus;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialState;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.event.ApotheosisCommandEvent;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import dev.shadowsoffire.placebo.events.AnvilLandEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdventureEvents {

    @SubscribeEvent
    public void cmds(ApotheosisCommandEvent e) {
        RarityCommand.register(e.getRoot());
        CategoryCheckCommand.register(e.getRoot());
        ReforgeCommand.register(e.getRoot());
        GemCommand.register(e.getRoot());
        SocketCommand.register(e.getRoot());
        BossCommand.register(e.getRoot());
        AffixCommand.register(e.getRoot());
        WorldTierCommand.register(e.getRoot());

        LiteralArgumentBuilder<CommandSourceStack> debug = Commands.literal("debug").requires(c -> c.hasPermission(4));
        DebugWeightCommand.register(debug, e.getContext());
        e.getRoot().then(debug);
    }

    @SubscribeEvent
    public void affixModifiers(StackAttributeModifiersEvent e) {
        ItemStack stack = e.getItemStack();
        SocketHelper.getGems(stack).addModifiers(e);
        AffixHelper.streamAffixes(stack).forEach(inst -> inst.addModifiers(e));
    }

    @SubscribeEvent
    public void preventBossSuffocate(EntityInvulnerabilityCheckEvent e) {
        if (e.getSource().is(DamageTypes.IN_WALL) && e.getEntity().getPersistentData().contains("apoth.boss")) {
            e.setInvulnerable(true);
        }
    }

    /**
     * This event handler allows affixes to react to projectiles being fired to trigger additional actions.
     * Projectiles marked as "apoth.generated" will not trigger the affix hook, so affixes can fire projectiles without recursion.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void fireProjectile(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Projectile proj && !proj.getPersistentData().getBoolean("apoth.generated")) {
            if (proj.getOwner() instanceof LivingEntity user) {
                ItemStack weapon = user.getUseItem();
                if (weapon.isEmpty()) {
                    weapon = user.getMainHandItem();
                    // TODO: Use a tag here after the LootCategory registry refactor, so external categories work right.
                    if (weapon.isEmpty() || !LootCategory.forItem(weapon).isRanged()) {
                        weapon = user.getOffhandItem();
                    }
                }
                if (weapon.isEmpty()) {
                    return;
                }
                SocketHelper.getGems(weapon).onProjectileFired(user, proj);
                AffixHelper.streamAffixes(weapon).forEach(a -> {
                    a.onProjectileFired(user, proj);
                });
                AffixHelper.copyToProjectile(weapon, proj);
            }
        }
    }

    /**
     * This event handler allows affixes to react to projectiles hitting something.
     */
    @SubscribeEvent
    public void impact(ProjectileImpactEvent e) {
        if (e.getProjectile() instanceof Projectile proj) {
            SocketHelper.getGemInstances(proj).forEach(inst -> inst.onProjectileImpact(proj, e.getRayTraceResult()));

            var affixes = AffixHelper.getAffixes(proj);
            affixes.values().forEach(inst -> inst.onProjectileImpact(proj, e.getRayTraceResult(), e.getRayTraceResult().getType()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void modifyIncomingDamageTags(EntityInvulnerabilityCheckEvent e) {
        MagicalArrowAffix.modifyIncomingDamageTags(e);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDamage(LivingIncomingDamageEvent e) {
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
    public void shieldBlock(LivingShieldBlockEvent e) {
        ItemStack stack = e.getEntity().getUseItem();
        var affixes = AffixHelper.getAffixes(stack);
        float blocked = e.getBlockedDamage();
        blocked = SocketHelper.getGems(stack).onShieldBlock(e.getEntity(), e.getDamageSource(), blocked);

        for (AffixInstance inst : affixes.values()) {
            blocked = inst.onShieldBlock(e.getEntity(), e.getDamageSource(), blocked);
        }
        if (blocked != e.getOriginalBlockedDamage()) {
            e.setBlockedDamage(blocked);
        }
    }

    @SubscribeEvent
    public void blockBreak(BreakEvent e) {
        ItemStack stack = e.getPlayer().getMainHandItem();
        SocketHelper.getGems(stack).onBlockBreak(e.getPlayer(), e.getLevel(), e.getPos(), e.getState());
        AffixHelper.streamAffixes(stack).forEach(inst -> {
            inst.onBlockBreak(e.getPlayer(), e.getLevel(), e.getPos(), e.getState());
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void drops(LivingDropsEvent e) {
        if (e.getSource().getEntity() instanceof Player p) {
            AffixHelper.streamAffixes(p.getMainHandItem()).forEach(a -> a.modifyEntityLoot(e));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void deathMark(LivingDeathEvent e) {
        FestiveAffix.markEquipment(e);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void dropsLowest(LivingDropsEvent e) {
        TelepathicAffix.drops(e);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void dropsLowest(BlockDropsEvent e) {
        TelepathicAffix.drops(e);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void festive_removeMarker(LivingDropsEvent e) {
        FestiveAffix.removeMarker(e);
    }

    @SubscribeEvent
    public void harvest(HarvestCheck e) {
        OmneticAffix.harvest(e);
        OmneticBonus.harvest(e);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void speed(BreakSpeed e) {
        OmneticAffix.speed(e);
        OmneticBonus.speed(e);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBreak(BlockEvent.BreakEvent e) {
        RadialAffix.onBreak(e);
        RadialBonus.onBreak(e);
    }

    @SubscribeEvent
    public void gemSmashing(AnvilLandEvent e) {
        Level level = e.getLevel();
        BlockPos pos = e.getPos();
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos));
        for (ItemEntity ent : items) {
            ItemStack stack = ent.getItem();
            if (stack.is(Items.GEM)) {
                ent.setItem(new ItemStack(Items.GEM_DUST, stack.getCount()));
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
        if (isReentrant) {
            return;
        }
        SocketHelper.getGems(e.getStack()).getEnchantmentLevels(e);

        AffixHelper.streamAffixes(e.getStack()).forEach(inst -> inst.getEnchantmentLevels(e));
        reentrantLock.get().set(false);
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void update(EntityTickEvent.Post e) {
        Entity entity = e.getEntity();
        if (entity.getPersistentData().contains("apoth.burns_in_sun")) {
            // Copy of Mob#isSunBurnTick()
            if (entity.level().isDay() && !entity.level().isClientSide) {
                float f = entity.getLightLevelDependentMagicValue();
                BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
                boolean flag = entity.isInWaterRainOrBubble() || entity.isInPowderSnow || entity.wasInPowderSnow;
                if (f > 0.5F && entity.getRandom().nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !flag && entity.level().canSeeSky(blockpos)) {
                    entity.setRemainingFireTicks(160);
                }
            }
        }
    }

    /**
     * Allows bosses that descend from {@link AbstractGolem} to despawn naturally, only after they have existed for 10 minutes.
     * Without this, they'll pile up forever - https://github.com/Shadows-of-Fire/Apotheosis/issues/1248
     */
    @SubscribeEvent
    public void despawn(MobDespawnEvent e) {
        if (e.getEntity() instanceof AbstractGolem g && g.tickCount > 12000 && g.getPersistentData().getBoolean("apoth.boss")) {
            Entity player = g.level().getNearestPlayer(g, -1.0D);
            if (player != null) {
                double dist = player.distanceToSqr(g);
                int despawnDist = g.getType().getCategory().getDespawnDistance();
                int dsDistSq = despawnDist * despawnDist;
                if (dist > dsDistSq) {
                    e.setResult(MobDespawnEvent.Result.ALLOW);
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

    @SubscribeEvent
    public void equip(LivingEquipmentChangeEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            Apoth.Triggers.EQUIPPED_ITEM.trigger(player, e.getSlot(), e.getTo());
        }
    }

    @SubscribeEvent
    public void sendWorldTierDataOnJoin(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            PacketDistributor.sendToPlayer(p, new WorldTierPayload(WorldTier.getTier(p)));
        }
    }

    @SubscribeEvent
    public void applyMissedTierAugments(EntityJoinLevelEvent e) {
        if (!(e.getLevel() instanceof ServerLevelAccessor)) {
            return;
        }

        Entity entity = e.getEntity();
        if (entity.getData(Attachments.TIER_AUGMENTS_APPLIED)) {
            return;
        }

        if (entity instanceof Player player) {
            WorldTier tier = player.getData(Attachments.WORLD_TIER);
            for (TierAugment aug : TierAugmentRegistry.getAugments(tier, Target.PLAYERS)) {
                aug.apply((ServerLevelAccessor) e.getLevel(), player);
            }
            entity.setData(Attachments.TIER_AUGMENTS_APPLIED, true);
        }
        else if (entity instanceof Mob mob) {
            float healthPct = mob.getHealth() / mob.getMaxHealth();
            Player player = e.getLevel().getNearestPlayer(mob, -1);
            if (player != null) {
                WorldTier tier = player.getData(Attachments.WORLD_TIER);
                for (TierAugment aug : TierAugmentRegistry.getAugments(tier, Target.MONSTERS)) {
                    aug.apply((ServerLevelAccessor) e.getLevel(), mob);
                }
                entity.setData(Attachments.TIER_AUGMENTS_APPLIED, true);
            }
            mob.setHealth(healthPct * mob.getMaxHealth());
        }
    }

    @SubscribeEvent
    public void sync(OnDatapackSyncEvent e) {
        ConfigPayload payload = new ConfigPayload();
        e.getRelevantPlayers().forEach(p -> PacketDistributor.sendToPlayer(p, payload));
    }

    @SubscribeEvent
    public void recordColdDamage(LivingDamageEvent.Post e) {
        if (e.getSource().is(ALObjects.DamageTypes.COLD_DAMAGE)) {
            LivingEntity entity = e.getEntity();
            entity.setData(Attachments.COLD_DAMAGE_TAKEN, entity.getData(Attachments.COLD_DAMAGE_TAKEN) + e.getNewDamage());
        }
    }

    @SubscribeEvent
    public void stackedOnOther(ItemStackedOnOtherEvent e) {
        Slot slot = e.getSlot();
        SlotAccess access = e.getCarriedSlotAccess();
        if (e.getClickAction() == ClickAction.SECONDARY && e.getCarriedItem().is(Items.GEM) && slot.allowModification(e.getPlayer())) {
            ItemStack stack = e.getStackedOnItem();
            ItemStack gemStack = e.getCarriedItem();
            ItemStack socketed = SocketHelper.socketGemInItem(stack, gemStack);
            if (!socketed.isEmpty()) {
                slot.set(socketed);
                access.set(gemStack.copyWithCount(gemStack.getCount() - 1));
                e.setCanceled(true);
                e.getPlayer().playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1, 1.5F + 0.35F * (1 - 2 * e.getPlayer().getRandom().nextFloat()));
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void removeCloudsOnDeath(LivingDeathEvent e) {
        if (e.getEntity().getPersistentData().getBoolean(Elite.MINIBOSS_KEY)) {
            for (Entity passenger : e.getEntity().getPassengers()) {
                if (passenger instanceof AreaEffectCloud cloud) {
                    cloud.discard();
                }
            }
        }
    }

    @SubscribeEvent
    public void syncRadialState(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new RadialStatePayload(RadialState.getState(player)));
        }
    }

}
