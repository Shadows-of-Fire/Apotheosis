package shadows.apotheosis.deadly.affix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.LocationInput;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.impl.tool.RadiusMiningAffix;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.gen.BossItem;
import shadows.apotheosis.deadly.objects.AffixTomeItem;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.deadly.reload.BossItemManager;
import shadows.placebo.events.ItemUseEvent;
import shadows.placebo.events.ShieldBlockEvent;
import shadows.placebo.util.ReflectionHelper;

public class AffixEvents {

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof AbstractArrowEntity && !e.getEntity().getPersistentData().getBoolean("apoth.generated")) {
			AbstractArrowEntity ent = (AbstractArrowEntity) e.getEntity();
			Entity shooter = ent.func_234616_v_();
			if (shooter instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) shooter;
				ItemStack bow = living.getHeldItemMainhand();
				Map<Affix, Float> affixes = AffixHelper.getAffixes(bow);
				CompoundNBT nbt = new CompoundNBT();
				affixes.keySet().forEach(a -> {
					a.onArrowFired(living, ent, bow, affixes.get(a));
					nbt.putFloat(a.getRegistryName().toString(), affixes.get(a));
				});
				ent.getPersistentData().put("apoth.affixes", nbt);
			}
		}
	}

	@SubscribeEvent
	public void impact(ProjectileImpactEvent.Arrow e) {
		CompoundNBT nbt = e.getArrow().getPersistentData().getCompound("apoth.affixes");
		for (String s : nbt.keySet()) {
			Affix a = Affix.REGISTRY.getValue(new ResourceLocation(s));
			a.onArrowImpact(e.getArrow(), e.getRayTraceResult(), e.getRayTraceResult().getType(), nbt.getFloat(s));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDamage(LivingHurtEvent e) {
		if (e.getSource() instanceof IndirectEntityDamageSource) {
			IndirectEntityDamageSource src = (IndirectEntityDamageSource) e.getSource();
			if ("arrow".equals(src.damageType)) {
				CompoundNBT affixes = src.getImmediateSource().getPersistentData().getCompound("apoth.affixes");
				if (affixes.contains(Affixes.MAGIC_ARROW.getRegistryName().toString())) {
					e.setCanceled(true);
					DamageSource nSrc = new IndirectEntityDamageSource("apoth.magic_arrow", src.getImmediateSource(), src.getTrueSource()).setDamageBypassesArmor().setMagicDamage().setProjectile();
					e.getEntityLiving().hurtResistantTime = 0;
					e.getEntityLiving().attackEntityFrom(nSrc, e.getAmount());
				}
			}
		}
		if (e.getSource().getTrueSource() instanceof LivingEntity) {
			LivingEntity src = (LivingEntity) e.getSource().getTrueSource();
			Map<Affix, Float> affixes = AffixHelper.getAffixes(src.getHeldItemMainhand());
			if (affixes.containsKey(Affixes.PIERCING)) {
				e.getSource().setDamageBypassesArmor();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void afterDamage(LivingHurtEvent e) {
		if (e.getSource() instanceof EntityDamageSource && e.getSource().getTrueSource() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) e.getSource().getTrueSource();
			Map<Affix, Float> affixes = AffixHelper.getAffixes(player.getHeldItemMainhand());
			float lifeSteal = affixes.getOrDefault(Affixes.LIFE_STEAL, 0F);
			float dmg = Math.min(e.getAmount(), e.getEntityLiving().getHealth());
			if (lifeSteal > 0 && !e.getSource().isMagicDamage()) {
				player.heal(dmg * lifeSteal);
			}
			float overheal = affixes.getOrDefault(Affixes.OVERHEAL, 0F);
			if (overheal > 0 && !e.getSource().isMagicDamage() && player.getAbsorptionAmount() < 20) {
				player.setAbsorptionAmount(Math.min(20, player.getAbsorptionAmount() + dmg * overheal));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void drops(LivingDropsEvent e) {
		if (e.getSource() instanceof IndirectEntityDamageSource) {
			IndirectEntityDamageSource src = (IndirectEntityDamageSource) e.getSource();
			if (src.getImmediateSource() instanceof AbstractArrowEntity && src.getTrueSource() != null) {
				CompoundNBT affixes = src.getImmediateSource().getPersistentData().getCompound("apoth.affixes");
				int canTeleport = (int) affixes.getFloat(Affixes.TELEPORT_DROPS.getRegistryName().toString());
				for (ItemEntity item : e.getDrops()) {
					if (canTeleport > 0) {
						Entity tSrc = src.getTrueSource();
						item.setPosition(tSrc.getPosX(), tSrc.getPosY(), tSrc.getPosZ());
						canTeleport--;
					}
				}
			}
		}
		if (e.getSource().getTrueSource() instanceof PlayerEntity && !e.getDrops().isEmpty() && e.getEntityLiving().isNonBoss()) {
			LivingEntity dead = e.getEntityLiving();
			PlayerEntity player = (PlayerEntity) e.getSource().getTrueSource();
			float chance = AffixHelper.getAffixes(player.getHeldItemMainhand()).getOrDefault(Affixes.LOOT_PINATA, 0F);
			if (player.world.rand.nextFloat() < chance) {
				player.world.playSound(null, dead.getPosX(), dead.getPosY(), dead.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.2F) * 0.7F);
				((ServerWorld) player.world).spawnParticle(ParticleTypes.EXPLOSION_EMITTER, dead.getPosX(), dead.getPosY(), dead.getPosZ(), 2, 1.0D, 0.0D, 0.0D, 0);
				List<ItemEntity> drops = new ArrayList<>(e.getDrops());
				for (int i = 0; i < 20; i++) {
					for (ItemEntity item : drops) {
						e.getDrops().add(new ItemEntity(player.world, item.getPosX(), item.getPosY(), item.getPosZ(), item.getItem().copy()));
					}
				}
				for (ItemEntity item : e.getDrops()) {
					if (!item.getItem().getItem().isDamageable()) {
						item.setPosition(dead.getPosX(), dead.getPosY(), dead.getPosZ());
						item.setMotion(-0.3 + dead.world.rand.nextDouble() * 0.6, 0.3 + dead.world.rand.nextDouble() * 0.3, -0.3 + dead.world.rand.nextDouble() * 0.6);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void update(LivingEntityUseItemEvent.Tick e) {
		if (e.getEntity() instanceof PlayerEntity) {
			ItemStack stack = e.getItem();
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Affixes.DRAW_SPEED)) {
				float t = affixes.get(Affixes.DRAW_SPEED);
				while (t > 0) {
					if (e.getEntity().ticksExisted % (int) Math.floor(1 / Math.min(1, t)) == 0) e.setDuration(e.getDuration() - 1);
					t--;
				}
			}
		}
	}

	@SubscribeEvent
	public void crit(CriticalHitEvent e) {
		Map<Affix, Float> affixes = AffixHelper.getAffixes(e.getPlayer().getHeldItemMainhand());

		if (!e.isVanillaCritical() && e.getPlayer().world.rand.nextFloat() < affixes.getOrDefault(Affixes.CRIT_CHANCE, 0F)) {
			e.setResult(Result.ALLOW);
		}

		if (!e.isVanillaCritical() && affixes.containsKey(Affixes.MAX_CRIT)) {
			e.setResult(Result.ALLOW);
		}

		if (affixes.containsKey(Affixes.CRIT_DAMAGE)) {
			e.setDamageModifier((1 + affixes.get(Affixes.CRIT_DAMAGE)) * Math.max(1.5F, e.getDamageModifier()));
		}
	}

	@SubscribeEvent
	public void cmds(RegisterCommandsEvent e) {
		e.getDispatcher().register(LiteralArgumentBuilder.<CommandSource>literal("affixloot").requires(c -> c.hasPermissionLevel(2)).then(Commands.argument("rarity", StringArgumentType.word()).suggests((a, b) -> ISuggestionProvider.suggest(Arrays.stream(LootRarity.values()).map(LootRarity::toString).collect(Collectors.toList()), b)).then(Commands.argument("type", StringArgumentType.word()).suggests((a, b) -> ISuggestionProvider.suggest(Arrays.stream(EquipmentType.values()).map(EquipmentType::toString).collect(Collectors.toList()), b)).executes(c -> {
			PlayerEntity p = c.getSource().asPlayer();
			String type = c.getArgument("type", String.class);
			EquipmentType eType = null;
			try {
				eType = EquipmentType.valueOf(type);
			} catch (Exception ex) {
			}
			AffixLootEntry entry = AffixLootManager.getRandomEntry(p.world.rand, eType);
			ItemStack stack = entry.getStack().copy();
			p.addItemStackToInventory(AffixLootManager.genLootItem(stack, p.world.rand, entry.getType(), LootRarity.valueOf(c.getArgument("rarity", String.class))));
			return 0;
		}))));
		e.getDispatcher().register(LiteralArgumentBuilder.<CommandSource>literal("apothboss").requires(c -> c.hasPermissionLevel(2)).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(c -> {
			BlockPos pos = c.getArgument("pos", LocationInput.class).getBlockPos(c.getSource());
			BossItem item = BossItemManager.INSTANCE.getRandomItem(ThreadLocalRandom.current());
			ServerWorld world = c.getSource().getWorld();
			MobEntity ent = item.createBoss(world, pos, ThreadLocalRandom.current());
			world.addEntity(ent);
			c.getSource().sendFeedback(new StringTextComponent(ent.getName().getString() + " has been summoned."), false);
			return 0;
		})));
	}

	@SubscribeEvent
	public void onItemUse(ItemUseEvent e) {
		ItemStack s = e.getItemStack();
		if (!s.isEmpty()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
			for (Map.Entry<Affix, Float> ent : affixes.entrySet()) {
				ActionResultType type = ent.getKey().onItemUse(e.getContext(), ent.getValue());
				if (type != null) {
					e.setCanceled(true);
					e.setCancellationResult(type);
				}
			}
		}
	}

	@SubscribeEvent
	public void harvest(HarvestCheck e) {
		ItemStack stack = e.getPlayer().getHeldItemMainhand();
		if (!stack.isEmpty()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Affixes.OMNITOOL)) {
				if (Items.DIAMOND_PICKAXE.canHarvestBlock(e.getTargetBlock()) || Items.DIAMOND_SHOVEL.canHarvestBlock(e.getTargetBlock()) || Items.DIAMOND_AXE.canHarvestBlock(e.getTargetBlock())) e.setCanHarvest(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void speed(BreakSpeed e) {
		ItemStack stack = e.getPlayer().getHeldItemMainhand();
		if (!stack.isEmpty()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			if (affixes.containsKey(Affixes.OMNITOOL)) {
				float shovel = getBaseSpeed(e.getPlayer(), Items.DIAMOND_SHOVEL, e.getState(), e.getPos());
				float axe = getBaseSpeed(e.getPlayer(), Items.DIAMOND_AXE, e.getState(), e.getPos());
				float pickaxe = getBaseSpeed(e.getPlayer(), Items.DIAMOND_PICKAXE, e.getState(), e.getPos());
				e.setNewSpeed(Math.max(shovel, Math.max(axe, Math.max(pickaxe, e.getOriginalSpeed()))));
			}
		}
	}

	static float getBaseSpeed(PlayerEntity player, Item tool, BlockState state, BlockPos pos) {
		float f = tool.getDestroySpeed(ItemStack.EMPTY, state);
		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(player);
			ItemStack itemstack = player.getHeldItemMainhand();
			if (i > 0 && !itemstack.isEmpty()) {
				f += i * i + 1;
			}
		}

		if (EffectUtils.hasMiningSpeedup(player)) {
			f *= 1.0F + (EffectUtils.getMiningSpeedup(player) + 1) * 0.2F;
		}

		if (player.isPotionActive(Effects.MINING_FATIGUE)) {
			float f1;
			switch (player.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) {
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

		if (player.areEyesInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
			f /= 5.0F;
		}

		if (!player.isOnGround()) {
			f /= 5.0F;
		}
		return f;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void addAffixGear(LivingSpawnEvent.SpecialSpawn e) {
		if (e.getSpawnReason() == SpawnReason.NATURAL || e.getSpawnReason() == SpawnReason.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntityLiving();
			Random rand = e.getWorld().getRandom();
			if (!e.getWorld().isRemote() && entity instanceof MonsterEntity) {
				if (entity.getHeldItemMainhand().isEmpty() && rand.nextInt(DeadlyConfig.randomAffixItem) == 0) {
					LootRarity rarity = LootRarity.random(rand);
					AffixLootEntry entry = AffixLootManager.getRandomEntry(rand);
					EquipmentSlotType slot = entry.getType().getSlot(entry.getStack());
					ItemStack loot = AffixLootManager.genLootItem(entry.getStack().copy(), rand, entry.getType(), rarity);
					loot.getTag().putBoolean("apoth_rspawn", true);
					entity.setItemStackToSlot(slot, loot);
					((MobEntity) entity).setDropChance(slot, 2);
					return;
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void surfaceBosses(LivingSpawnEvent.CheckSpawn e) {
		if (e.getSpawnReason() == SpawnReason.NATURAL || e.getSpawnReason() == SpawnReason.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntityLiving();
			Random rand = e.getWorld().getRandom();
			if (!e.getWorld().isRemote() && entity instanceof MonsterEntity && e.getResult() == Result.DEFAULT) {
				if (rand.nextInt(DeadlyConfig.surfaceBossChance) == 0 && e.getWorld().canSeeSky(new BlockPos(e.getX(), e.getY(), e.getZ()))) {
					BossItem item = BossItemManager.INSTANCE.getRandomItem(rand);
					PlayerEntity player = e.getWorld().getClosestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
					if (player == null) return; //Should never be null, but we check anyway since nothing makes sense around here.
					MobEntity boss = item.createBoss((IServerWorld) e.getWorld(), new BlockPos(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), rand);
					if (canSpawn(e.getWorld(), boss, player.getDistanceSq(boss))) {
						e.getWorld().addEntity(boss);
						e.setResult(Result.DENY);
						DeadlyModule.debugLog(boss.getPosition(), "Surface Boss - " + boss.getName().getString());
						if (DeadlyConfig.surfaceBossLightning) {
							LightningBoltEntity le = EntityType.LIGHTNING_BOLT.create(((IServerWorld) e.getWorld()).getWorld());
							le.setPosition(boss.getPosX(), boss.getPosY(), boss.getPosZ());
							le.setEffectOnly(true);
							e.getWorld().addEntity(le);
						}
					}
				}
			}
		}
	}

	private static boolean canSpawn(IWorld world, MobEntity entity, double playerDist) {
		if (playerDist > entity.getType().getClassification().getInstantDespawnDistance() * entity.getType().getClassification().getInstantDespawnDistance() && entity.canDespawn(playerDist)) {
			return false;
		} else {
			return entity.canSpawn(world, SpawnReason.NATURAL) && entity.isNotColliding(world);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void trades(WandererTradesEvent e) {
		if (DeadlyConfig.affixTrades) e.getRareTrades().add(new AffixTrade());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void sortModifiers(ItemAttributeModifierEvent e) {
		if (e.getModifiers().isEmpty()) return;
		Multimap<Attribute, AttributeModifier> map = TreeMultimap.create((k1, k2) -> k1.getRegistryName().compareTo(k2.getRegistryName()), (v1, v2) -> {
			int compOp = Integer.compare(v1.getOperation().ordinal(), v2.getOperation().ordinal());
			int compValue = Double.compare(v2.getAmount(), v1.getAmount());
			return compOp == 0 ? compValue : compOp;
		});
		map.putAll(e.getModifiers());
		ReflectionHelper.setPrivateValue(ItemAttributeModifierEvent.class, e, map, "unmodifiableModifiers");
	}

	@SubscribeEvent
	public void affixModifiers(ItemAttributeModifierEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.getItem() instanceof IAffixSensitiveItem && !((IAffixSensitiveItem) stack.getItem()).receivesAttributes(stack)) return;
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
			List<ITextComponent> components = new ArrayList<>();
			affixes.forEach((afx, lvl) -> afx.addInformation(stack, lvl, components::add));
			e.getToolTip().addAll(1, components);
		}
	}

	@SubscribeEvent
	public void shieldBlock(ShieldBlockEvent e) {
		ItemStack stack = e.getEntity().getActiveItemStack();
		if (stack.isShield(e.getEntity()) && stack.hasTag()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			float blocked = e.getBlocked();
			for (Map.Entry<Affix, Float> ent : affixes.entrySet()) {
				blocked = ent.getKey().onShieldBlock(e.getEntity(), stack, e.getSource(), blocked, ent.getValue());
			}
			if (blocked != e.getOriginalBlocked()) e.setBlocked(blocked);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBreak(BlockEvent.BreakEvent e) {
		PlayerEntity player = e.getPlayer();
		ItemStack tool = player.getHeldItemMainhand();
		World world = player.world;
		if (!world.isRemote && tool.hasTag()) {
			int level = (int) AffixHelper.getAffixLevel(tool, Affixes.RADIUS_MINING);
			if (level > 0) {
				float hardness = e.getState().getBlockHardness(e.getWorld(), e.getPos());
				RadiusMiningAffix.breakExtraBlocks((ServerPlayerEntity) player, e.getPos(), tool, level, hardness);
			}
		}
	}

	@SubscribeEvent
	public void anvilEvent(AnvilUpdateEvent e) {
		if (AffixTomeItem.updateAnvil(e)) return;
	}
}