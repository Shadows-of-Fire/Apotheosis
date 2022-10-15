package shadows.apotheosis.adventure;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisCommandEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixManager;
import shadows.apotheosis.adventure.affix.effect.DamageReductionAffix;
import shadows.apotheosis.adventure.affix.socket.GemManager;
import shadows.apotheosis.adventure.commands.CategoryCheckCommand;
import shadows.apotheosis.adventure.commands.GemCommand;
import shadows.apotheosis.adventure.commands.LootifyCommand;
import shadows.apotheosis.adventure.commands.ModifierCommand;
import shadows.apotheosis.adventure.commands.RarityCommand;
import shadows.apotheosis.adventure.commands.SocketCommand;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.util.DamageSourceUtil;
import shadows.placebo.events.AnvilLandEvent;
import shadows.placebo.events.ItemUseEvent;

public class AdventureEvents {

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

	private static final UUID HEAVY_WEAPON_AS = UUID.fromString("f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454");

	@SubscribeEvent
	public void affixModifiers(ItemAttributeModifierEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.hasTag()) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
			affixes.forEach((afx, inst) -> inst.addModifiers(e.getSlotType(), e::addModifier));
			if (!affixes.isEmpty() && LootCategory.forItem(stack) == LootCategory.HEAVY_WEAPON && e.getSlotType() == EquipmentSlot.MAINHAND) {
				e.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(HEAVY_WEAPON_AS, "Heavy Weapon AS", -0.60, Operation.MULTIPLY_TOTAL));
			}
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
					nbt.putFloat(a.affix().getId().toString(), a.level());
					nbt.putString(AffixHelper.RARITY, a.rarity().id());
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
				Affix a = AffixManager.INSTANCE.getValue(new ResourceLocation(s));
				if (a != null) {
					a.onArrowImpact(rarity, nbt.getFloat(s), arrow, e.getRayTraceResult(), e.getRayTraceResult().getType());
				}
			}

		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void pierce(LivingHurtEvent e) {
		if (e.getSource().getDirectEntity() instanceof LivingEntity attacker) {
			if (!e.getSource().isBypassArmor() && !e.getSource().isMagic()) {
				LivingEntity target = e.getEntityLiving();
				float pierce = (float) (attacker.getAttributeValue(Apoth.Attributes.PIERCING) - 1);
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

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onDamage(LivingHurtEvent e) {
		Apoth.Affixes.MAGICAL.ifPresent(afx -> afx.onHurt(e));
		DamageReductionAffix.onHurt(e);
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

	private static boolean noRecurse = false;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void attack(LivingAttackEvent e) {
		if (e.getEntity().level.isClientSide) return;
		if (noRecurse) return;
		noRecurse = true;
		if (e.getSource().getDirectEntity() instanceof LivingEntity attacker && !e.getSource().isMagic()) {
			float hpDmg = (float) attacker.getAttributeValue(Apoth.Attributes.CURRENT_HP_DAMAGE) - 1;
			float fireDmg = (float) attacker.getAttributeValue(Apoth.Attributes.FIRE_DAMAGE);
			float coldDmg = (float) attacker.getAttributeValue(Apoth.Attributes.COLD_DAMAGE);
			LivingEntity target = e.getEntityLiving();
			int time = target.invulnerableTime;
			target.invulnerableTime = 0;
			if (hpDmg > 0.001 && Apotheosis.localAtkStrength >= 0.75F) {
				target.hurt(src(attacker).bypassArmor(), Apotheosis.localAtkStrength * hpDmg * target.getHealth());
			}
			target.invulnerableTime = 0;
			if (fireDmg > 0.001) {
				target.hurt(src(attacker).setMagic(), Apotheosis.localAtkStrength * fireDmg);
				target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), (int) (15 * fireDmg)));
			}
			target.invulnerableTime = 0;
			if (coldDmg > 0.001) {
				target.hurt(src(attacker).setMagic(), Apotheosis.localAtkStrength * coldDmg);
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (15 * coldDmg), Mth.floor(coldDmg / 5)));
			}
			target.invulnerableTime = time;
		}
		noRecurse = false;
	}

	private static DamageSource src(LivingEntity entity) {
		return entity instanceof Player p ? DamageSource.playerAttack(p) : DamageSource.mobAttack(entity);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void crit(CriticalHitEvent e) {
		double critChance = e.getPlayer().getAttributeValue(Apoth.Attributes.CRIT_CHANCE) - 1;
		float critDmg = (float) e.getPlayer().getAttributeValue(Apoth.Attributes.CRIT_DAMAGE);
		float modifier = e.isVanillaCritical() ? e.getDamageModifier() : 1.0F;

		if (critChance > 1) { // Overcrit applies the modifier multiple times.
			e.setResult(Result.ALLOW);

			while (critChance > 1) {
				critChance--;
				modifier *= critDmg;
			}

			if (e.getPlayer().level.random.nextFloat() <= critChance) modifier *= critDmg;

			e.setDamageModifier(modifier);
		} else if (e.getPlayer().level.random.nextFloat() <= critChance) {
			e.setResult(Result.ALLOW);
			e.setDamageModifier(modifier * critDmg);
		} else {
			e.setDamageModifier(modifier * critDmg);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void breakSpd(BreakSpeed e) {
		e.setNewSpeed(e.getNewSpeed() * (float) e.getPlayer().getAttributeValue(Apoth.Attributes.MINING_SPEED));
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
		ItemStack stack = e.getEntityLiving().getUseItem();
		Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
		float blocked = e.getBlockedDamage();
		for (AffixInstance inst : affixes.values()) {
			blocked = inst.onShieldBlock(e.getEntityLiving(), e.getDamageSource(), blocked);
		}
		if (blocked != e.getOriginalBlockedDamage()) e.setBlockedDamage(blocked);
	}

	@SubscribeEvent
	public void blockBreak(BreakEvent e) {
		ItemStack stack = e.getPlayer().getMainHandItem();
		Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
		for (AffixInstance inst : affixes.values()) {
			inst.onBlockBreak(e.getPlayer(), e.getWorld(), e.getPos(), e.getState());
		}
	}

	@SubscribeEvent
	public void arrow(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof AbstractArrow arrow) {
			if (arrow.level.isClientSide || arrow.getPersistentData().getBoolean("apoth.attrib.done")) return;
			if (arrow.getOwner() instanceof LivingEntity le) {
				arrow.setBaseDamage(arrow.getBaseDamage() * le.getAttributeValue(Apoth.Attributes.ARROW_DAMAGE));
				arrow.setDeltaMovement(arrow.getDeltaMovement().scale(le.getAttributeValue(Apoth.Attributes.ARROW_VELOCITY)));
				if (!arrow.isCritArrow()) arrow.setCritArrow(arrow.random.nextFloat() <= le.getAttributeValue(Apoth.Attributes.CRIT_CHANCE) - 1);
			}
			arrow.getPersistentData().putBoolean("apoth.attrib.done", true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void dropsHigh(LivingDropsEvent e) {
		if (e.getSource().getEntity() instanceof ServerPlayer p && e.getEntity() instanceof Monster) {
			if (p instanceof FakePlayer) return;
			float chance = AdventureConfig.gemDropChance + (e.getEntity().getPersistentData().contains("apoth.boss") ? AdventureConfig.gemBossBonus : 0);
			if (p.random.nextFloat() <= chance) {
				Entity ent = e.getEntity();
				e.getDrops().add(new ItemEntity(ent.level, ent.getX(), ent.getY(), ent.getZ(), GemManager.getRandomGemStack(p.random, p.getLuck(), p.getLevel()), 0, 0, 0));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void drops(LivingDropsEvent e) {
		Apoth.Affixes.FESTIVE.ifPresent(afx -> afx.drops(e));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void dropsLowest(LivingDropsEvent e) {
		Apoth.Affixes.TELEPATHIC.ifPresent(afx -> afx.drops(e));
	}

	@SubscribeEvent
	public void harvest(HarvestCheck e) {
		Apoth.Affixes.OMNETIC.ifPresent(afx -> afx.harvest(e));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void speed(BreakSpeed e) {
		Apoth.Affixes.OMNETIC.ifPresent(afx -> afx.speed(e));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBreak(BlockEvent.BreakEvent e) {
		Apoth.Affixes.RADIAL.ifPresent(afx -> afx.onBreak(e));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void special(SpecialSpawn e) {
		if (e.getSpawnReason() == MobSpawnType.NATURAL && e.getWorld().getRandom().nextFloat() <= AdventureConfig.randomAffixItem && e.getEntity() instanceof Monster) {
			e.setCanceled(true);
			Player nearest = e.getEntity().level.getNearestPlayer(e.getEntity(), 32);
			float luck = nearest != null ? nearest.getLuck() : 0;
			ItemStack affixItem = LootController.createRandomLootItem(e.getWorld().getRandom(), null, luck, (ServerLevel) e.getEntity().level);
			if (affixItem.isEmpty()) return;
			affixItem.getOrCreateTag().putBoolean("apoth_rspawn", true);
			LootCategory cat = LootCategory.forItem(affixItem);
			EquipmentSlot slot = cat.getSlots(affixItem)[0];
			e.getEntityLiving().setItemSlot(slot, affixItem);
			if (e.getEntityLiving() instanceof Mob mob) mob.setGuaranteedDrop(slot);
		}
	}

	@SubscribeEvent
	public void gemSmashing(AnvilLandEvent e) {
		Level level = e.getLevel();
		BlockPos pos = e.getPos();
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos, pos.offset(1, 1, 1)));
		for (ItemEntity ent : items) {
			ItemStack stack = ent.getItem();
			if (stack.getItem() == Apoth.Items.GEM) {
				ent.setItem(new ItemStack(Apoth.Items.GEM_DUST, stack.getCount()));
			}
		}
	}

}