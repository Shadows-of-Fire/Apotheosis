package shadows.apotheosis.adventure;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisCommandEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.commands.CategoryCheckCommand;
import shadows.apotheosis.adventure.commands.GemCommand;
import shadows.apotheosis.adventure.commands.LootifyCommand;
import shadows.apotheosis.adventure.commands.ModifierCommand;
import shadows.apotheosis.adventure.commands.RarityCommand;
import shadows.apotheosis.adventure.commands.SocketCommand;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.mixin.LivingEntityInvoker;
import shadows.apotheosis.util.DamageSourceUtil;

public class AdventureModuleEvents {

	@SubscribeEvent
	public void reloads(AddReloadListenerEvent e) {
	}

	@SubscribeEvent
	public void cmds(ApotheosisCommandEvent e) {
		RarityCommand.register(e.getRoot());
		CategoryCheckCommand.register(e.getRoot());
		LootifyCommand.register(e.getRoot());
		ModifierCommand.register(e.getRoot());
		GemCommand.register(e.getRoot());
		SocketCommand.register(e.getRoot());
	}

	@SubscribeEvent
	public void affixModifiers(ItemAttributeModifierEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.hasTag()) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
			affixes.forEach((afx, inst) -> inst.addModifiers(e.getSlotType(), e::addModifier));
		}
	}

	private static final Set<Float> values = ImmutableSet.of(0.1F, 0.2F, 0.25F, 0.33F, 0.5F, 1.0F, 1.1F, 1.2F, 1.25F, 1.33F, 1.5F, 2.0F, 2.1F, 2.25F, 2.33F, 2.5F, 3F);

	/**
	 * This event handler makes the Draw Speed attribute work as intended.
	 * Modifiers targetting this attribute should use the MULTIPLY_BASE operation.
	 */
	@SubscribeEvent
	public void drawSpeed(LivingEntityUseItemEvent.Tick e) {
		if (e.getEntity() instanceof Player player) {
			double t = player.getAttribute(Apoth.Attributes.DRAW_SPEED).getValue() - 1;
			if (t == 0 || !LootCategory.forItem(e.getItem()).isRanged()) return;
			float clamped = values.stream().filter(f -> f >= t).min(Float::compareTo).orElse(3F);
			while (clamped > 0) {
				if (e.getEntity().tickCount % (int) Math.floor(1 / Math.min(1, t)) == 0) e.setDuration(e.getDuration() - 1);
				clamped--;
			}
		}
	}

	/**
	 * This event handler allows affixes to react to arrows being fired to trigger additional actions.
	 * Arrows marked as "apoth.generated" will not trigger the affix hook, so affixes can fire arrows without recursion.
	 */
	@SubscribeEvent
	public void fireArrow(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof AbstractArrow arrow && !arrow.getPersistentData().getBoolean("apoth.generated")) {
			Entity shooter = arrow.getOwner();
			if (shooter instanceof LivingEntity living) {
				ItemStack bow = living.getMainHandItem();
				Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(bow);
				CompoundTag nbt = new CompoundTag();
				affixes.values().forEach(a -> {
					a.onArrowFired(living, arrow);
					nbt.putFloat(a.affix().getRegistryName().toString(), a.level());
				});
				arrow.getPersistentData().put("apoth.affixes", nbt);
			}
		}
	}

	/**
	 * This event handler allows affixes to react to arrows hitting something.
	 */
	@SubscribeEvent
	public void impact(ProjectileImpactEvent e) {
		if (e.getProjectile() instanceof AbstractArrow arrow) {
			CompoundTag nbt = arrow.getPersistentData().getCompound("apoth.affixes");
			LootRarity rarity = AffixHelper.getRarity(nbt);
			for (String s : nbt.getAllKeys()) {
				Affix a = Affix.REGISTRY.getValue(new ResourceLocation(s));
				if (a != null) {
					a.onArrowImpact(rarity, nbt.getFloat(s), arrow, e.getRayTraceResult(), e.getRayTraceResult().getType());
				}
			}

		}
	}

	/**
	 * This event handler manages the Piercing attribute.
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDamage(LivingHurtEvent e) {
		if (e.getSource().getDirectEntity() instanceof LivingEntity attacker) {
			if (!e.getSource().isBypassArmor()) {
				LivingEntity target = e.getEntityLiving();
				float pierce = (float) (attacker.getAttributeValue(Apoth.Attributes.PIERCING) - 1);
				if (pierce > 0.001) {
					float pierceDmg = e.getAmount() * pierce;
					e.setAmount(e.getAmount() - pierce);
					((LivingEntityInvoker) target).callActuallyHurt(DamageSourceUtil.copy(e.getSource()).bypassArmor(), pierceDmg);
				}
			}
		}
	}

	/**
	 * This event handler manages the Life Steal and Overheal attributes.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void afterDamage(LivingHurtEvent e) {
		if (e.getSource().getDirectEntity() instanceof LivingEntity attacker && !e.getSource().isMagic()) {
			float lifesteal = (float) attacker.getAttributeValue(Apoth.Attributes.LIFE_STEAL) - 1;
			float dmg = Math.min(e.getAmount(), e.getEntityLiving().getHealth());
			if (lifesteal > 0.001) {
				attacker.heal(dmg * lifesteal);
			}
			float overheal = (float) attacker.getAttributeValue(Apoth.Attributes.OVERHEAL) - 1;
			if (overheal > 0 && attacker.getAbsorptionAmount() < 20) {
				attacker.setAbsorptionAmount(Math.min(20, attacker.getAbsorptionAmount() + dmg * overheal));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void attack(LivingAttackEvent e) {
		if (e.getSource().getDirectEntity() instanceof LivingEntity attacker && !e.getSource().isMagic()) {
			float hpDmg = (float) attacker.getAttributeValue(Apoth.Attributes.CURRENT_HP_DAMAGE) - 1;
			float fireDmg = (float) attacker.getAttributeValue(Apoth.Attributes.FIRE_DAMAGE);
			float coldDmg = (float) attacker.getAttributeValue(Apoth.Attributes.COLD_DAMAGE);
			LivingEntity target = e.getEntityLiving();
			if (target.invulnerableTime < 10) {
				// Likely call Affix.onEntityHurt here
				if (hpDmg > 0.001) {
					((LivingEntityInvoker) target).callActuallyHurt(src(attacker).setMagic(), Apotheosis.localAtkStrength * hpDmg * target.getHealth());
				}
				if (fireDmg > 0.001) {
					((LivingEntityInvoker) target).callActuallyHurt(src(attacker).setMagic(), Apotheosis.localAtkStrength * fireDmg);
					target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), (int) (40 * fireDmg)));
				}
				if (coldDmg > 0.001) {
					((LivingEntityInvoker) target).callActuallyHurt(src(attacker).setMagic(), Apotheosis.localAtkStrength * coldDmg);
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (40 * coldDmg), (int) Mth.floor(coldDmg / 3)));
				}
			}
		}
	}

	private static DamageSource src(LivingEntity entity) {
		return entity instanceof Player p ? DamageSource.playerAttack(p) : DamageSource.mobAttack(entity);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void crit(CriticalHitEvent e) {
		double critChance = e.getPlayer().getAttributeValue(Apoth.Attributes.CRIT_CHANCE) - 1;
		float critDmg = (float) e.getPlayer().getAttributeValue(Apoth.Attributes.CRIT_DAMAGE) - 1;

		if (!e.isVanillaCritical() && e.getPlayer().level.random.nextFloat() <= critChance) {
			e.setResult(Result.ALLOW);
			e.setDamageModifier(1.5F);
		}

		if (critDmg < 0.001) {
			e.setDamageModifier((1 + critDmg) * e.getDamageModifier());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void breakSpd(BreakSpeed e) {
		e.setNewSpeed(e.getNewSpeed() * (float) e.getPlayer().getAttributeValue(Apoth.Attributes.BREAK_SPEED));
	}

}
