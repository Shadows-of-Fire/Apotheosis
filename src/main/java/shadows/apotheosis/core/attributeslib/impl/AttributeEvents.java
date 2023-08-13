package shadows.apotheosis.core.attributeslib.impl;

import java.util.Map.Entry;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.core.attributeslib.AttributesLib;
import shadows.apotheosis.core.attributeslib.api.ALAttributes;
import shadows.apotheosis.core.attributeslib.api.AttributeHelper;
import shadows.apotheosis.core.attributeslib.api.IFormattableAttribute;
import shadows.apotheosis.core.attributeslib.packet.CritParticleMessage;
import shadows.apotheosis.core.attributeslib.util.AttributesUtil;
import shadows.placebo.network.PacketDistro;

public class AttributeEvents {

    // Fixes attributes which had their base values changed.
    // TODO: Remove 6.4.0
    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void fixChangedAttributes(PlayerLoggedInEvent e) {
        AttributeMap map = e.getEntity().getAttributes();
        for (Entry<ResourceKey<Attribute>, Attribute> entry : Registry.ATTRIBUTE.entrySet()) {
            if (Apotheosis.MODID.equals(entry.getKey().location().getNamespace())) {
                map.getInstance(entry.getValue()).setBaseValue(((RangedAttribute) entry.getValue()).getDefaultValue());
            }
        }
        map.getInstance(ForgeMod.STEP_HEIGHT_ADDITION.get()).setBaseValue(0.6);
    }

    private boolean canBenefitFromDrawSpeed(ItemStack stack) {
        return stack.getItem() instanceof ProjectileWeaponItem || stack.getItem() instanceof TridentItem;
    }

    /**
     * This event handler is the implementation for {@link ALAttributes#DRAW_SPEED}.<br>
     * Each full point of draw speed provides an extra using tick per game tick.<br>
     * Each partial point of draw speed provides an extra using tick periodically.
     */
    @SubscribeEvent
    public void drawSpeed(LivingEntityUseItemEvent.Tick e) {
        if (e.getEntity() instanceof Player player) {
            double t = player.getAttribute(ALAttributes.DRAW_SPEED.get()).getValue() - 1;
            if (t == 0 || !this.canBenefitFromDrawSpeed(e.getItem())) return;

            // Handle negative draw speed.
            int offset = -1;
            if (t < 0) {
                offset = 1;
                t = -t;
            }

            while (t > 1) { // Every 100% triggers an immediate extra tick
                e.setDuration(e.getDuration() + offset);
                t--;
            }

            if (t > 0.5F) { // Special case 0.5F so that values in (0.5, 1) don't round to 1.
                if (e.getEntity().tickCount % 2 == 0) e.setDuration(e.getDuration() + offset);
                t -= 0.5F;
            }

            int mod = (int) Math.floor(1 / Math.min(1, t));
            if (e.getEntity().tickCount % mod == 0) e.setDuration(e.getDuration() + offset);
            t--;
        }
    }

    /**
     * This event handler manages the Life Steal and Overheal attributes.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void lifeStealOverheal(LivingHurtEvent e) {
        if (e.getSource().getDirectEntity() instanceof LivingEntity attacker && AttributesUtil.isPhysicalDamage(e.getSource())) {
            float lifesteal = (float) attacker.getAttributeValue(ALAttributes.LIFE_STEAL.get());
            float dmg = Math.min(e.getAmount(), e.getEntity().getHealth());
            if (lifesteal > 0.001) {
                attacker.heal(dmg * lifesteal);
            }
            float overheal = (float) attacker.getAttributeValue(ALAttributes.OVERHEAL.get());
            float maxOverheal = attacker.getMaxHealth() * 0.5F;
            if (overheal > 0 && attacker.getAbsorptionAmount() < maxOverheal) {
                attacker.setAbsorptionAmount(Math.min(maxOverheal, attacker.getAbsorptionAmount() + dmg * overheal));
            }
        }
    }

    /**
     * Recursion guard for {@link #meleeDamageAttributes(LivingAttackEvent)}.<br>
     * Doesn't need to be ThreadLocal as attack logic is main-thread only.
     */
    private static boolean noRecurse = false;

    /**
     * Applies the following melee damage attributes:<br>
     * <ul>
     * <li>{@link ALAttributes#CURRENT_HP_DAMAGE}</li>
     * <li>{@link ALAttributes#FIRE_DAMAGE}</li>
     * <li>{@link ALAttributes#COLD_DAMAGE}</li>
     * </ul>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void meleeDamageAttributes(LivingAttackEvent e) {
        if (e.getEntity().level.isClientSide) return;
        if (noRecurse) return;
        noRecurse = true;
        if (e.getSource().getDirectEntity() instanceof LivingEntity attacker && AttributesUtil.isPhysicalDamage(e.getSource())) {
            float hpDmg = (float) attacker.getAttributeValue(ALAttributes.CURRENT_HP_DAMAGE.get());
            float fireDmg = (float) attacker.getAttributeValue(ALAttributes.FIRE_DAMAGE.get());
            float coldDmg = (float) attacker.getAttributeValue(ALAttributes.COLD_DAMAGE.get());
            LivingEntity target = e.getEntity();
            int time = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (hpDmg > 0.001 && Apotheosis.localAtkStrength >= 0.85F) {
                target.hurt(src(attacker), Apotheosis.localAtkStrength * hpDmg * target.getHealth());
            }
            target.invulnerableTime = 0;
            if (fireDmg > 0.001 && Apotheosis.localAtkStrength >= 0.55F) {
                target.hurt(src(attacker).setMagic().bypassArmor(), Apotheosis.localAtkStrength * fireDmg);
                target.setRemainingFireTicks(target.getRemainingFireTicks() + (int) (10 * fireDmg));
            }
            target.invulnerableTime = 0;
            if (coldDmg > 0.001 && Apotheosis.localAtkStrength >= 0.55F) {
                target.hurt(src(attacker).setMagic().bypassArmor(), Apotheosis.localAtkStrength * coldDmg);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (15 * coldDmg), Mth.floor(coldDmg / 5)));
            }
            target.invulnerableTime = time;
        }
        noRecurse = false;
    }

    private static DamageSource src(LivingEntity entity) {
        return entity instanceof Player p ? DamageSource.playerAttack(p) : DamageSource.mobAttack(entity);
    }

    /**
     * Handles {@link ALAttributes#CRIT_CHANCE} and {@link ALAttributes#CRIT_DAMAGE}
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void apothCriticalStrike(LivingHurtEvent e) {
        LivingEntity attacker = e.getSource().getEntity() instanceof LivingEntity le ? le : null;
        if (attacker == null) return;

        double critChance = attacker.getAttributeValue(ALAttributes.CRIT_CHANCE.get());
        float critDmg = (float) attacker.getAttributeValue(ALAttributes.CRIT_DAMAGE.get());

        RandomSource rand = e.getEntity().random;

        float critMult = 1.0F;

        // Roll for crits. Each overcrit reduces the effectiveness by 15%
        // We stop rolling when crit chance fails or the crit damage would reduce the total damage dealt.
        while (rand.nextFloat() <= critChance && critDmg > 1.0F) {
            critChance--;
            critMult *= critDmg;
            critDmg *= 0.85F;
        }

        e.setAmount(e.getAmount() * critMult);

        if (critMult > 1 && !attacker.level.isClientSide) {
            PacketDistro.sendToTracking(AttributesLib.CHANNEL, new CritParticleMessage(e.getEntity().getId()), (ServerLevel) attacker.level, e.getEntity().blockPosition());
        }
    }

    /**
     * Handles {@link ALAttributes#CRIT_DAMAGE}'s interactions with vanilla critical strikes.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void vanillaCritDmg(CriticalHitEvent e) {
        float critDmg = (float) e.getEntity().getAttributeValue(ALAttributes.CRIT_DAMAGE.get());
        if (e.isVanillaCritical()) {
            e.setDamageModifier(Math.max(e.getDamageModifier(), critDmg));
        }
    }

    /**
     * Handles {@link ALAttributes#MINING_SPEED}
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void breakSpd(BreakSpeed e) {
        e.setNewSpeed(e.getNewSpeed() * (float) e.getEntity().getAttributeValue(ALAttributes.MINING_SPEED.get()));
    }

    /**
     * This event, and {@linkplain #mobXp(LivingExperienceDropEvent) the event below} handle {@link ALAttributes#EXPERIENCE_GAINED}
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void blockBreak(BreakEvent e) {
        double xpMult = e.getPlayer().getAttributeValue(ALAttributes.EXPERIENCE_GAINED.get());
        e.setExpToDrop((int) (e.getExpToDrop() * xpMult));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void mobXp(LivingExperienceDropEvent e) {
        Player player = e.getAttackingPlayer();
        if (player == null) return;
        double xpMult = e.getAttackingPlayer().getAttributeValue(ALAttributes.EXPERIENCE_GAINED.get());
        e.setDroppedExperience((int) (e.getDroppedExperience() * xpMult));
    }

    /**
     * Handles {@link ALAttributes#HEALING_RECEIVED}
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void heal(LivingHealEvent e) {
        float factor = (float) e.getEntity().getAttributeValue(ALAttributes.HEALING_RECEIVED.get());
        e.setAmount(e.getAmount() * factor);
        if (e.getAmount() <= 0) e.setCanceled(true);
    }

    /**
     * Handles {@link ALAttributes#ARROW_DAMAGE} and {@link ALAttributes#ARROW_VELOCITY}
     */
    @SubscribeEvent
    public void arrow(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof AbstractArrow arrow) {
            if (arrow.level.isClientSide || arrow.getPersistentData().getBoolean("attributeslib.arrow.done")) return;
            if (arrow.getOwner() instanceof LivingEntity le) {
                arrow.setBaseDamage(arrow.getBaseDamage() * le.getAttributeValue(ALAttributes.ARROW_DAMAGE.get()));
                arrow.setDeltaMovement(arrow.getDeltaMovement().scale(le.getAttributeValue(ALAttributes.ARROW_VELOCITY.get())));
            }
            arrow.getPersistentData().putBoolean("attributeslib.arrow.done", true);
        }
    }

    /**
     * Copied from {@link MeleeAttackGoal#getAttackReachSqr}
     */
    private static double getAttackReachSqr(Entity attacker, LivingEntity pAttackTarget) {
        return attacker.getBbWidth() * 2.0F * attacker.getBbWidth() * 2.0F + pAttackTarget.getBbWidth();
    }

    /**
     * Random used for dodge calculations.<br>
     * This random is seeded with the target entity's tick count before use.
     */
    private static Random dodgeRand = new Random();

    /**
     * Handles {@link ALAttributes#DODGE_CHANCE} for melee attacks.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void dodge(LivingAttackEvent e) {
        LivingEntity target = e.getEntity();
        if (target.level.isClientSide) return;
        Entity attacker = e.getSource().getDirectEntity();
        if (attacker instanceof LivingEntity) {
            double dodgeChance = target.getAttributeValue(ALAttributes.DODGE_CHANCE.get());
            double atkRangeSqr = attacker instanceof Player p ? p.getAttackRange() * p.getAttackRange() : getAttackReachSqr(attacker, target);
            dodgeRand.setSeed(target.tickCount);
            if (attacker.distanceToSqr(target) <= atkRangeSqr && dodgeRand.nextFloat() <= dodgeChance) {
                this.onDodge(target);
                e.setCanceled(true);
            }
        }
    }

    /**
     * Handles {@link ALAttributes#DODGE_CHANCE} for projectiles.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void dodge(ProjectileImpactEvent e) {
        Entity target = e.getRayTraceResult() instanceof EntityHitResult entRes ? entRes.getEntity() : null;
        if (target instanceof LivingEntity lvTarget) {
            double dodgeChance = lvTarget.getAttributeValue(ALAttributes.DODGE_CHANCE.get());
            // We can skip the distance check for projectiles, as "Projectile Impact" means the projectile is on the target.
            dodgeRand.setSeed(target.tickCount);
            if (dodgeRand.nextFloat() <= dodgeChance) {
                this.onDodge(lvTarget);
                e.setCanceled(true);
            }
        }
    }

    private void onDodge(LivingEntity target) {
        target.level.playSound(null, target, AttributesLib.DODGE_SOUND.get(), SoundSource.NEUTRAL, 1, 0.7F + target.random.nextFloat() * 0.3F);
        if (target.level instanceof ServerLevel sl) {
            double height = target.getBbHeight();
            double width = target.getBbWidth();
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX() - width / 4, target.getY(), target.getZ() - width / 4, 6, -width / 4, height / 8, -width / 4, 0);
        }
    }

    /**
     * Fix for https://github.com/MinecraftForge/MinecraftForge/issues/9370
     */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void fixMCF9370(ProjectileImpactEvent e) {
        if (e.isCanceled()) {
            Entity target = e.getRayTraceResult() instanceof EntityHitResult entRes ? entRes.getEntity() : null;
            Projectile proj = e.getProjectile();
            if (target != null && proj instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
                if (arrow.piercingIgnoreEntityIds == null) {
                    arrow.piercingIgnoreEntityIds = new IntOpenHashSet(arrow.getPierceLevel());
                }
                arrow.piercingIgnoreEntityIds.add(target.getId());
            }
        }
    }

    /**
     * Adds a fake modifier to show Attack Range to weapons with Attack Damage.
     */
    @SubscribeEvent
    public void affixModifiers(ItemAttributeModifierEvent e) {
        boolean hasBaseAD = e.getModifiers().get(Attributes.ATTACK_DAMAGE).stream().filter(m -> ((IFormattableAttribute) Attributes.ATTACK_DAMAGE).getBaseUUID().equals(m.getId())).findAny().isPresent();
        if (hasBaseAD) {
            boolean hasBaseAR = e.getModifiers().get(ForgeMod.ATTACK_RANGE.get()).stream().filter(m -> ((IFormattableAttribute) ForgeMod.ATTACK_RANGE.get()).getBaseUUID().equals(m.getId())).findAny().isPresent();
            if (!hasBaseAR) {
                e.addModifier(ForgeMod.ATTACK_RANGE.get(), new AttributeModifier(AttributeHelper.BASE_ATTACK_RANGE, () -> "attributeslib:fake_base_range", 0, Operation.ADDITION));
            }
        }
    }
}
