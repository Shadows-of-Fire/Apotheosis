package shadows.apotheosis.core.attributeslib.impl;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.core.attributeslib.api.ALAttributes;
import shadows.apotheosis.util.DamageSourceUtil;

public class AttributeEvents {

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
			if (t == 0 || !canBenefitFromDrawSpeed(e.getItem())) return;
			while (t > 1) { // Every 100% triggers an immediate extra tick
				e.setDuration(e.getDuration() - 1);
				t--;
			}

			if (t > 0.5F) { // Special case 0.5F so that values in (0.5, 1) don't round to 1.
				if (e.getEntity().tickCount % 2 == 0) e.setDuration(e.getDuration() - 1);
				t -= 0.5F;
			}

			int mod = (int) Math.floor(1 / Math.min(1, t));
			if (e.getEntity().tickCount % mod == 0) e.setDuration(e.getDuration() - 1);
			t--;
		}
	}

	/**
	 * This event handler is the implementation for {@link ALAttributes#PIERCING}.<br>
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void piercing(LivingHurtEvent e) {
		if (e.getSource().getDirectEntity() instanceof LivingEntity attacker) {
			if (!e.getSource().isBypassArmor() && !e.getSource().isMagic()) {
				LivingEntity target = e.getEntity();
				float pierce = (float) (attacker.getAttributeValue(ALAttributes.PIERCING.get()) - 1);
				if (pierce > 0.001) {
					float pierceDmg = e.getAmount() * pierce;
					e.setAmount(e.getAmount() - pierceDmg);
					int time = target.invulnerableTime;
					target.invulnerableTime = 0;
					target.hurt(DamageSourceUtil.copy(e.getSource()).bypassArmor(), pierceDmg);
					target.invulnerableTime = time;
				}
			}
		}
	}

	/**
	 * This event handler manages the Life Steal and Overheal attributes.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void lifeStealOverheal(LivingHurtEvent e) {
		if (e.getSource().getDirectEntity() instanceof LivingEntity attacker && !e.getSource().isMagic()) {
			float lifesteal = (float) attacker.getAttributeValue(ALAttributes.LIFE_STEAL.get()) - 1;
			float dmg = Math.min(e.getAmount(), e.getEntity().getHealth());
			if (lifesteal > 0.001) {
				attacker.heal(dmg * lifesteal);
			}
			float overheal = (float) attacker.getAttributeValue(ALAttributes.OVERHEAL.get()) - 1;
			if (overheal > 0 && attacker.getAbsorptionAmount() < 20) {
				attacker.setAbsorptionAmount(Math.min(20, attacker.getAbsorptionAmount() + dmg * overheal));
			}
		}

		if (e.getSource() == DamageSource.IN_WALL && e.getEntity().getPersistentData().contains("ALboss")) {
			e.setCanceled(true);
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
	 * <li> {@link ALAttributes#CURRENT_HP_DAMAGE} </li>
	 * <li> {@link ALAttributes#FIRE_DAMAGE} </li>
	 * <li> {@link ALAttributes#COLD_DAMAGE} </li>
	 * </ul>
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void meleeDamageAttributes(LivingAttackEvent e) {
		if (e.getEntity().level.isClientSide) return;
		if (noRecurse) return;
		noRecurse = true;
		Entity direct = e.getSource().getDirectEntity();
		direct = direct instanceof AbstractArrow arr ? arr.getOwner() : direct;
		if (direct instanceof LivingEntity attacker && !e.getSource().isMagic()) {
			float hpDmg = (float) attacker.getAttributeValue(ALAttributes.CURRENT_HP_DAMAGE.get()) - 1;
			float fireDmg = (float) attacker.getAttributeValue(ALAttributes.FIRE_DAMAGE.get());
			float coldDmg = (float) attacker.getAttributeValue(ALAttributes.COLD_DAMAGE.get());
			LivingEntity target = e.getEntity();
			int time = target.invulnerableTime;
			target.invulnerableTime = 0;
			if (hpDmg > 0.001 && Apotheosis.localAtkStrength >= 0.85F) {
				target.hurt(src(attacker), Apotheosis.localAtkStrength * hpDmg * target.getHealth());
			}
			target.invulnerableTime = 0;
			if (fireDmg > 0.001 && Apotheosis.localAtkStrength >= 0.45F) {
				target.hurt(src(attacker).setMagic().bypassArmor(), Apotheosis.localAtkStrength * fireDmg);
				target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), (int) (15 * fireDmg)));
			}
			target.invulnerableTime = 0;
			if (coldDmg > 0.001 && Apotheosis.localAtkStrength >= 0.45F) {
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
	public void crit(CriticalHitEvent e) {
		double critChance = e.getEntity().getAttributeValue(ALAttributes.CRIT_CHANCE.get()) - 1;
		float critDmg = (float) e.getEntity().getAttributeValue(ALAttributes.CRIT_DAMAGE.get());
		float overcritMult = Math.max(1.5F, critDmg - 1.5F);
		RandomSource rand = e.getEntity().random;
		if (e.isVanillaCritical() && critChance >= 0.5F) {
			critChance -= 0.5F;
			critDmg *= 1.5F;
		}

		// Roll once to determine if the attack should become a crit.
		if (rand.nextFloat() <= critChance || critChance >= 1) {
			e.setResult(Result.ALLOW);
		}
		// Reduce the chance since this roll "consumes" 1 point.
		critChance--;

		// Roll for overcrit
		while (rand.nextFloat() <= critChance) {
			e.setResult(Result.ALLOW);
			critChance--;
			critDmg *= overcritMult;
		}

		e.setDamageModifier(critDmg);
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
	}

	/**
	 * Handles {@link ALAttributes#ARROW_DAMAGE} and {@link ALAttributes#ARROW_VELOCITY}
	 */
	@SubscribeEvent
	public void arrow(EntityJoinLevelEvent e) {
		if (e.getEntity() instanceof AbstractArrow arrow) {
			if (arrow.level.isClientSide || arrow.getPersistentData().getBoolean("ALattrib.done")) return;
			if (arrow.getOwner() instanceof LivingEntity le) {
				arrow.setBaseDamage(arrow.getBaseDamage() * le.getAttributeValue(ALAttributes.ARROW_DAMAGE.get()));
				arrow.setDeltaMovement(arrow.getDeltaMovement().scale(le.getAttributeValue(ALAttributes.ARROW_VELOCITY.get())));
				if (!arrow.isCritArrow()) arrow.setCritArrow(arrow.random.nextFloat() <= le.getAttributeValue(ALAttributes.CRIT_CHANCE.get()) - 1);
			}
			arrow.getPersistentData().putBoolean("ALattrib.done", true);
		}
	}
}
