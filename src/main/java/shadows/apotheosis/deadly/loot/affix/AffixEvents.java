package shadows.apotheosis.deadly.loot.affix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import shadows.apotheosis.deadly.loot.LootManager;
import shadows.apotheosis.deadly.loot.LootRarity;

public class AffixEvents {

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof AbstractArrowEntity && !e.getEntity().getPersistentData().getBoolean("apoth.generated")) {
			AbstractArrowEntity ent = (AbstractArrowEntity) e.getEntity();
			Entity shooter = ent.getShooter();
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

	@SubscribeEvent
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
		if (e.getSource() instanceof EntityDamageSource && e.getSource().getTrueSource() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) e.getSource().getTrueSource();
			Map<Affix, Float> affixes = AffixHelper.getAffixes(player.getHeldItemMainhand());
			float lifeSteal = affixes.getOrDefault(Affixes.LIFE_STEAL, 0F);
			if (lifeSteal > 0 && !e.getSource().isMagicDamage()) {
				player.heal(e.getAmount() * lifeSteal);
			}
			float overheal = affixes.getOrDefault(Affixes.OVERHEAL, 0F);
			if (overheal > 0 && !e.getSource().isMagicDamage()) {
				player.setAbsorptionAmount(player.getAbsorptionAmount() + e.getAmount() * overheal);
			}
			if (affixes.containsKey(Affixes.PIERCING)) {
				e.getSource().setDamageBypassesArmor();
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
						item.setPosition(tSrc.getX(), tSrc.getY(), tSrc.getZ());
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
				player.world.playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.2F) * 0.7F);
				((ServerWorld) player.world).spawnParticle(ParticleTypes.EXPLOSION_EMITTER, dead.getX(), dead.getY(), dead.getZ(), 2, 1.0D, 0.0D, 0.0D, 0);
				List<ItemEntity> drops = new ArrayList<>(e.getDrops());
				for (int i = 0; i < 20; i++) {
					for (ItemEntity item : drops) {
						e.getDrops().add(new ItemEntity(player.world, item.getX(), item.getY(), item.getZ(), item.getItem().copy()));
					}
				}
				for (ItemEntity item : e.getDrops()) {
					if (!item.getItem().getItem().isDamageable()) {
						item.setPosition(dead.getX(), dead.getY(), dead.getZ());
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
			p.addItemStackToInventory(LootManager.genLootItem(LootManager.getRandomEntry(p.world.rand, null), p.world.rand, LootRarity.valueOf(c.getArgument("rarity", String.class))));
			return 0;
		})));
	}

}
