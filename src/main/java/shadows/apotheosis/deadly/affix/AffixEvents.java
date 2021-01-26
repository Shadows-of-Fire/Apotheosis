package shadows.apotheosis.deadly.affix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
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
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.reload.AffixLootManager;

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
	public void starting(FMLServerStartingEvent e) {
		e.getServer().getCommandManager().getDispatcher().register(LiteralArgumentBuilder.<CommandSource>literal("affixloot").requires(c -> c.hasPermissionLevel(2)).then(Commands.argument("rarity", StringArgumentType.word()).suggests((a, b) -> {
			return ISuggestionProvider.suggest(Arrays.stream(LootRarity.values()).map(r -> r.toString()).collect(Collectors.toList()), b);
		}).executes(c -> {
			PlayerEntity p = c.getSource().asPlayer();
			p.addItemStackToInventory(AffixLootManager.genLootItem(AffixLootManager.getRandomEntry(p.world.rand, null), p.world.rand, LootRarity.valueOf(c.getArgument("rarity", String.class))));
			return 0;
		})));
	}

	public static ActionResultType onItemUse(ItemUseContext ctx) {
		ItemStack s = ctx.getItem();
		if (!s.isEmpty()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(s);
			for (Map.Entry<Affix, Float> ent : affixes.entrySet()) {
				ActionResultType type = ent.getKey().onItemUse(ctx, ent.getValue());
				if (type != null) return type;
			}
		}
		return null;
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
	public void spawns(LivingSpawnEvent.SpecialSpawn e) {
		if (e.getSpawnReason() == SpawnReason.NATURAL || e.getSpawnReason() == SpawnReason.CHUNK_GENERATION) {
			LivingEntity entity = e.getEntityLiving();
			Random rand = e.getWorld().getRandom();
			if (!e.getWorld().isRemote() && entity instanceof MonsterEntity) {
				if (entity.getHeldItemMainhand().isEmpty() && rand.nextInt(DeadlyConfig.randomAffixItem) == 0) {
					LootRarity rarity = LootRarity.random(rand);
					AffixLootEntry entry = WeightedRandom.getRandomItem(rand, AffixLootManager.getEntries());
					EquipmentSlotType slot = entry.getType().getSlot(entry.getStack());
					ItemStack loot = AffixLootManager.genLootItem(entry.getStack().copy(), rand, rarity);
					loot.getTag().putBoolean("apoth_rspawn", true);
					entity.setItemStackToSlot(slot, loot);
					((MobEntity) entity).setDropChance(slot, 2);
					return;
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void trades(WandererTradesEvent e) {
		if (DeadlyConfig.affixTrades) e.getRareTrades().add(new AffixTrade());
	}
}