package shadows.apotheosis.deadly.affix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.ItemUseEvent;
import shadows.apotheosis.deadly.affix.impl.tool.RadiusMiningAffix;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.loot.LootController;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.deadly.objects.AffixTomeItem;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.deadly.reload.BossItemManager;

public class AffixEvents {

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof AbstractArrow ent && !e.getEntity().getPersistentData().getBoolean("apoth.generated")) {
			Entity shooter = ent.getOwner();
			if (shooter instanceof LivingEntity living) {
				ItemStack bow = living.getMainHandItem();
				Map<Affix, Float> affixes = AffixHelper.getAffixes(bow);
				CompoundTag nbt = new CompoundTag();
				affixes.keySet().forEach(a -> {
					a.onArrowFired(living, ent, bow, affixes.get(a));
					nbt.putFloat(a.getRegistryName().toString(), affixes.get(a));
				});
				ent.getPersistentData().put("apoth.affixes", nbt);
			}
		}
	}

	@SubscribeEvent
	public void impact(ProjectileImpactEvent e) {
		if(e.getProjectile() instanceof AbstractArrow arrow)
		{
			CompoundTag nbt = arrow.getPersistentData().getCompound("apoth.affixes");
			for (String s : nbt.getAllKeys()) {
				Affix a = Affix.REGISTRY.getValue(new ResourceLocation(s));
				if (a != null) {
					a.onArrowImpact(arrow, e.getRayTraceResult(), e.getRayTraceResult().getType(), nbt.getFloat(s));
				}
			}

		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDamage(LivingHurtEvent e) {
		if (Apoth.Affixes.MAGIC_ARROW != null && e.getSource() instanceof IndirectEntityDamageSource src) {
			if ("arrow".equals(src.msgId)) {
				CompoundTag affixes = src.getDirectEntity().getPersistentData().getCompound("apoth.affixes");
				if (affixes.contains(Apoth.Affixes.MAGIC_ARROW.getRegistryName().toString())) {
					e.setCanceled(true);
					DamageSource nSrc = new IndirectEntityDamageSource("apoth.magic_arrow", src.getDirectEntity(), src.getEntity()).bypassArmor().setMagic().setProjectile();
					e.getEntityLiving().invulnerableTime = 0;
					e.getEntityLiving().hurt(nSrc, e.getAmount());
				}
			}
		}
		if (Apoth.Affixes.PIERCING != null && e.getSource().getEntity() instanceof LivingEntity src) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(src.getMainHandItem());
			if (affixes.containsKey(Apoth.Affixes.PIERCING)) {
				e.getSource().bypassArmor();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void afterDamage(LivingHurtEvent e) {
		if (e.getSource() instanceof EntityDamageSource src && src.getEntity() instanceof Player player) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(player.getMainHandItem());
			float lifeSteal = affixes.getOrDefault(Apoth.Affixes.LIFE_STEAL, 0F);
			float dmg = Math.min(e.getAmount(), e.getEntityLiving().getHealth());
			if (lifeSteal > 0 && !src.isMagic()) {
				player.heal(dmg * lifeSteal);
			}
			float overheal = affixes.getOrDefault(Apoth.Affixes.OVERHEAL, 0F);
			if (overheal > 0 && !src.isMagic() && player.getAbsorptionAmount() < 20) {
				player.setAbsorptionAmount(Math.min(20, player.getAbsorptionAmount() + dmg * overheal));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void drops(LivingDropsEvent e) {
		if (Apoth.Affixes.TELEPORT_DROPS != null && e.getSource() instanceof IndirectEntityDamageSource src) {
			if (src.getDirectEntity() instanceof AbstractArrow && src.getEntity() != null) {
				CompoundTag affixes = src.getDirectEntity().getPersistentData().getCompound("apoth.affixes");
				int canTeleport = (int) affixes.getFloat(Apoth.Affixes.TELEPORT_DROPS.getRegistryName().toString());
				for (ItemEntity item : e.getDrops()) {
					if (canTeleport > 0) {
						Entity tSrc = src.getEntity();
						item.setPos(tSrc.getX(), tSrc.getY(), tSrc.getZ());
						canTeleport--;
					}
				}
			}
		}
		if (e.getSource().getEntity() instanceof Player player && !e.getDrops().isEmpty() && e.getEntityLiving().canChangeDimensions() && !(e.getEntityLiving() instanceof Player)) {
			LivingEntity dead = e.getEntityLiving();
			float chance = AffixHelper.getAffixes(player.getMainHandItem()).getOrDefault(Apoth.Affixes.LOOT_PINATA, 0F);
			if (player.level.random.nextFloat() < chance) {
				player.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (player.level.random.nextFloat() - player.level.random.nextFloat()) * 0.2F) * 0.7F);
				((ServerLevel) player.level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, dead.getX(), dead.getY(), dead.getZ(), 2, 1.0D, 0.0D, 0.0D, 0);
				List<ItemEntity> drops = new ArrayList<>(e.getDrops());
				for (int i = 0; i < 20; i++) {
					for (ItemEntity item : drops) {
						e.getDrops().add(new ItemEntity(player.level, item.getX(), item.getY(), item.getZ(), item.getItem().copy()));
					}
				}
				for (ItemEntity item : e.getDrops()) {
					if (!item.getItem().getItem().canBeDepleted()) {
						item.setPos(dead.getX(), dead.getY(), dead.getZ());
						item.setDeltaMovement(-0.3 + dead.level.random.nextDouble() * 0.6, 0.3 + dead.level.random.nextDouble() * 0.3, -0.3 + dead.level.random.nextDouble() * 0.6);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void update(LivingEntityUseItemEvent.Tick e) {
		if (e.getEntity() instanceof Player) {
			ItemStack stack = e.getItem();
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Apoth.Affixes.DRAW_SPEED)) {
				float t = affixes.get(Apoth.Affixes.DRAW_SPEED);
				while (t > 0) {
					if (e.getEntity().tickCount % (int) Math.floor(1 / Math.min(1, t)) == 0) e.setDuration(e.getDuration() - 1);
					t--;
				}
			}
		}
	}

	@SubscribeEvent
	public void crit(CriticalHitEvent e) {
		Map<Affix, Float> affixes = AffixHelper.getAffixes(e.getPlayer().getMainHandItem());

		if (!e.isVanillaCritical() && e.getPlayer().level.random.nextFloat() < affixes.getOrDefault(Apoth.Affixes.CRIT_CHANCE, 0F)) {
			e.setResult(Result.ALLOW);
		}

		if (!e.isVanillaCritical() && affixes.containsKey(Apoth.Affixes.MAX_CRIT)) {
			e.setResult(Result.ALLOW);
		}

		if (affixes.containsKey(Apoth.Affixes.CRIT_DAMAGE)) {
			e.setDamageModifier((1 + affixes.get(Apoth.Affixes.CRIT_DAMAGE)) * Math.max(1.5F, e.getDamageModifier()));
		}
	}

	@SubscribeEvent
	public void onItemUse(ItemUseEvent e) {
		ItemStack s = e.getItemStack();
		if (!s.isEmpty()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
			for (Map.Entry<Affix, Float> ent : affixes.entrySet()) {
				InteractionResult interactionResult = ent.getKey().onItemUse(e.getContext(), ent.getValue());
				if (interactionResult != null) {
					e.setCanceled(true);
					e.setCancellationResult(interactionResult);
				}
			}
		}
	}

	@SubscribeEvent
	public void harvest(PlayerEvent.HarvestCheck e) {
		ItemStack stack = e.getPlayer().getMainHandItem();
		if (!stack.isEmpty()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Apoth.Affixes.OMNITOOL)) {
				if (Items.DIAMOND_PICKAXE.isCorrectToolForDrops(e.getTargetBlock()) || Items.DIAMOND_SHOVEL.isCorrectToolForDrops(e.getTargetBlock()) || Items.DIAMOND_AXE.isCorrectToolForDrops(e.getTargetBlock())) e.setCanHarvest(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void speed(PlayerEvent.BreakSpeed e) {
		ItemStack stack = e.getPlayer().getMainHandItem();
		if (!stack.isEmpty()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Apoth.Affixes.OMNITOOL)) {
				float shovel = getBaseSpeed(e.getPlayer(), Items.DIAMOND_SHOVEL, e.getState(), e.getPos());
				float axe = getBaseSpeed(e.getPlayer(), Items.DIAMOND_AXE, e.getState(), e.getPos());
				float pickaxe = getBaseSpeed(e.getPlayer(), Items.DIAMOND_PICKAXE, e.getState(), e.getPos());
				e.setNewSpeed(Math.max(shovel, Math.max(axe, Math.max(pickaxe, e.getOriginalSpeed()))));
			}
		}
	}

	static float getBaseSpeed(Player player, Item tool, BlockState state, BlockPos pos) {
		float f = tool.getDestroySpeed(ItemStack.EMPTY, state);
		if (f > 1.0F) {
			int i = EnchantmentHelper.getBlockEfficiency(player);
			ItemStack itemstack = player.getMainHandItem();
			if (i > 0 && !itemstack.isEmpty()) {
				f += i * i + 1;
			}
		}

		if (MobEffectUtil.hasDigSpeed(player)) {
			f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
		}

		if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float f1;
			switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
			case 0:
				f1 = 0.3F;
				break;
			case 1:
				f1 = 0.09F;
				break;
			case 2:
				f1 = 0.0027F;
				break;
			case 3:
			default:
				f1 = 8.1E-4F;
			}

			f *= f1;
		}

		if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
			f /= 5.0F;
		}

		if (!player.isOnGround()) {
			f /= 5.0F;
		}
		return f;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void addAffixGear(LivingSpawnEvent.SpecialSpawn e) {
		if (e.getSpawnReason() == MobSpawnType.NATURAL || e.getSpawnReason() == MobSpawnType.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntityLiving();
			Random rand = e.getWorld().getRandom();
			if (!e.getWorld().isClientSide() && entity instanceof Monster) {
				if (entity.getMainHandItem().isEmpty() && DeadlyConfig.randomAffixItem > 0 && rand.nextInt(DeadlyConfig.randomAffixItem) == 0) {
					var entry= AffixLootManager.getRandomEntry(rand);
					if(entry.isPresent())
					{
						LootRarity rarity = LootRarity.random(rand);
						ItemStack loot = LootController.lootifyItem(entry.get().getStack().copy(), rarity, rand);
						EquipmentSlot slot = entry.get().getStack().getEquipmentSlot();
						if(slot == null)
							slot = LivingEntity.getEquipmentSlotForItem(loot);
						loot.getOrCreateTag().putBoolean("apoth_rspawn", true);
						entity.setItemSlot(slot, loot);
						((Mob) entity).setDropChance(slot, 2);
					}
					else
						DeadlyModule.LOGGER.error("Failed to get random affix loot entry, cannot add affix gear to random spawn!");
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void surfaceBosses(LivingSpawnEvent.CheckSpawn e) {
		if (e.getSpawnReason() == MobSpawnType.NATURAL || e.getSpawnReason() == MobSpawnType.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntityLiving();
			Random rand = e.getWorld().getRandom();
			if (!e.getWorld().isClientSide() && entity instanceof Monster && e.getResult() == Result.DEFAULT) {
				if (DeadlyConfig.surfaceBossChance > 0 && rand.nextInt(DeadlyConfig.surfaceBossChance) == 0 && e.getWorld().canSeeSky(new BlockPos(e.getX(), e.getY(), e.getZ()))) {
					Player player = e.getWorld().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
					if (player == null) return; //Should never be null, but we check anyway since nothing makes sense around here.
					var item = BossItemManager.INSTANCE.getRandomItem(rand);
					if(!item.isPresent())
					{
						DeadlyModule.LOGGER.error("Failed to procure random boss entry! Cannot spawn");
						return;
					}
					Mob boss = item.get().createBoss((ServerLevelAccessor) e.getWorld(), new BlockPos(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), rand);
					if (canSpawn(e.getWorld(), boss, player.distanceToSqr(boss))) {
						e.getWorld().addFreshEntity(boss);
						e.setResult(Result.DENY);
						DeadlyModule.debugLog(boss.blockPosition(), "Surface Boss - " + boss.getName().getString());
						if (DeadlyConfig.surfaceBossLightning) {
							LightningBolt le = EntityType.LIGHTNING_BOLT.create(((ServerLevelAccessor) e.getWorld()).getLevel());
							le.setPos(boss.getX(), boss.getY(), boss.getZ());
							le.setVisualOnly(true);
							e.getWorld().addFreshEntity(le);
						}
					}
				}
			}
		}
	}

	private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
		if (playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance() && entity.removeWhenFarAway(playerDist)) {
			return false;
		} else {
			return entity.checkSpawnRules(world, MobSpawnType.NATURAL) && entity.checkSpawnObstruction(world);
		}
	}
//
//	@SubscribeEvent(priority = EventPriority.LOW)
//	public void trades(WandererTradesEvent e) {
//		if (DeadlyConfig.affixTrades) for (int i = 0; i < 3; i++)
//			e.getGenericTrades().add(new AffixTrade());
//	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void sortModifiers(ItemAttributeModifierEvent e) {
		if (e.getModifiers() == null || e.getModifiers().isEmpty() || FMLEnvironment.dist == Dist.DEDICATED_SERVER) return;
		Multimap<Attribute, AttributeModifier> map = TreeMultimap.create((k1, k2) -> k1.getRegistryName().compareTo(k2.getRegistryName()), (v1, v2) -> {
			int compOp = Integer.compare(v1.getOperation().ordinal(), v2.getOperation().ordinal());
			int compValue = Double.compare(v2.getAmount(), v1.getAmount());
			return compOp == 0 ? compValue == 0 ? v1.getName().compareTo(v2.getName()) : compValue : compOp;
		});
		for (Map.Entry<Attribute, AttributeModifier> ent : e.getModifiers().entries()) {
			if (ent.getKey() != null && ent.getValue() != null) map.put(ent.getKey(), ent.getValue());
			else DeadlyModule.LOGGER.error("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", e.getItemStack(), ent.getKey(), ent.getValue());
		}
		ObfuscationReflectionHelper.setPrivateValue(ItemAttributeModifierEvent.class, e, map, "unmodifiableModifiers");
	}

	@SubscribeEvent
	public void affixModifiers(ItemAttributeModifierEvent e) {
		ItemStack stack = e.getItemStack();
		if(stack.getItem() instanceof IAffixSensitiveItem affixSensitiveItem && !affixSensitiveItem.receivesAttributes(stack)) return;
		if (stack.hasTag()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			affixes.forEach((afx, lvl) -> afx.addModifiers(stack, lvl, e.getSlotType(), e::addModifier));
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void affixTooltips(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.getItem() instanceof IAffixSensitiveItem && !((IAffixSensitiveItem) stack.getItem()).receivesTooltips(stack)) return;
		if (stack.hasTag()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			List<Component> components = new ArrayList<>();
			affixes.forEach((afx, lvl) -> afx.addInformation(stack, lvl, components::add));
			e.getToolTip().addAll(1, components);
		}
	}

	@SubscribeEvent
	public void shieldBlock(ShieldBlockEvent e) {
		ItemStack stack = e.getEntityLiving().getUseItem();
		if (stack.getItem() instanceof ShieldItem && stack.hasTag()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			float blocked = e.getBlockedDamage();
			for (Map.Entry<Affix, Float> ent : affixes.entrySet()) {
				blocked = ent.getKey().onShieldBlock(e.getEntityLiving(), stack, e.getDamageSource(), blocked, ent.getValue());
			}
			if (blocked != e.getOriginalBlockedDamage()) e.setBlockedDamage(blocked);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBreak(BlockEvent.BreakEvent e) {
		Player player = e.getPlayer();
		ItemStack tool = player.getMainHandItem();
		Level world = player.level;
		if (!world.isClientSide && tool.hasTag()) {
			int level = AffixHelper.getAffixes(tool).getOrDefault(Apoth.Affixes.RADIUS_MINING, 0f).intValue();
			if (level > 0) {
				float hardness = e.getState().getDestroySpeed(e.getWorld(), e.getPos());
				RadiusMiningAffix.breakExtraBlocks((ServerPlayer) player, e.getPos(), tool, level, hardness);
			}
		}
	}

	@SubscribeEvent
	public void anvilEvent(AnvilUpdateEvent e) { AffixTomeItem.updateAnvil(e); }
}