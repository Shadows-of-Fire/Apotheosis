package shadows.apotheosis.ench;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.objects.ScrappingTomeItem;
import shadows.apotheosis.ench.table.EnchantingStatManager;

public class EnchModuleEvents {

	@SubscribeEvent
	public void anvilEvent(AnvilUpdateEvent e) {
		if (e.getLeft().isEnchanted()) {
			if (e.getRight().getItem() == Items.COBWEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> ent.getKey().isCurse()).collect(Collectors.toMap(Entry::getKey, Entry::getValue)), stack);
				e.setCost(1);
				e.setMaterialCost(1);
				e.setOutput(stack);
			} else if (e.getRight().getItem() == ApotheosisObjects.PRISMATIC_WEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> !ent.getKey().isCurse()).collect(Collectors.toMap(Entry::getKey, Entry::getValue)), stack);
				e.setCost(30);
				e.setMaterialCost(1);
				e.setOutput(stack);
				return;
			}
		}
		if ((e.getLeft().getItem() == Items.CHIPPED_ANVIL || e.getLeft().getItem() == Items.DAMAGED_ANVIL) && e.getRight().getItem().is(Tags.Items.STORAGE_BLOCKS_IRON)) {
			if (e.getLeft().getCount() != 1) return;
			int dmg = e.getLeft().getItem() == Items.DAMAGED_ANVIL ? 2 : 1;
			ItemStack out = new ItemStack(dmg == 1 ? Items.ANVIL : Items.CHIPPED_ANVIL);
			EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(e.getLeft()), out);
			out.setCount(1);
			e.setOutput(out);
			e.setCost(5 + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, e.getLeft()) + EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.SPLITTING, e.getLeft()) * 2);
			e.setMaterialCost(1);
			return;
		}
		if (e.getLeft().getItem() == ApotheosisObjects.HELLSHELF.asItem() || e.getLeft().getItem() == ApotheosisObjects.SEASHELF.asItem()) {
			if (e.getLeft().getItem() != e.getRight().getItem() || e.getLeft().getCount() != 1) return;
			Enchantment ench = e.getLeft().getItem() == ApotheosisObjects.HELLSHELF.asItem() ? ApotheosisObjects.HELL_INFUSION : ApotheosisObjects.SEA_INFUSION;
			int leftLvl = EnchantmentHelper.getItemEnchantmentLevel(ench, e.getLeft());
			int rightLvl = EnchantmentHelper.getItemEnchantmentLevel(ench, e.getRight());
			if (leftLvl == 0 || rightLvl != leftLvl) return;
			if (leftLvl + 1 > EnchModule.getEnchInfo(ench).getMaxLevel()) return;
			ItemStack out = e.getLeft().copy();
			EnchantmentHelper.setEnchantments(ImmutableMap.of(ench, leftLvl + 1), out);
			out.setCount(1);
			e.setOutput(out);
			e.setCost(1);
			e.setMaterialCost(1);
			return;
		}
		if (ScrappingTomeItem.updateAnvil(e)) return;
	}

	Method dropLoot;

	/**
	 * Event handler for the Scavenger and Knowledge of the Ages enchantments.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void drops(LivingDropsEvent e) throws Exception {
		Entity attacker = e.getSource().getEntity();
		LivingEntity target = e.getEntityLiving();
		if (attacker instanceof PlayerEntity) {
			PlayerEntity p = (PlayerEntity) attacker;
			if (p.level.isClientSide) return;
			int scavenger = EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.SCAVENGER, p.getMainHandItem());
			if (scavenger > 0 && p.level.random.nextInt(100) < scavenger * 2.5F) {
				if (this.dropLoot == null) {
					this.dropLoot = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_213354_a", DamageSource.class, boolean.class);
				}
				target.captureDrops(new ArrayList<>());
				this.dropLoot.invoke(target, e.getSource(), true);
				e.getDrops().addAll(target.captureDrops(null));
			}
			int knowledge = EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.KNOWLEDGE, p.getMainHandItem());
			if (knowledge > 0 && !(e.getEntity() instanceof PlayerEntity)) {
				int items = 0;
				for (ItemEntity i : e.getDrops())
					items += i.getItem().getCount();
				if (items > 0) e.getDrops().clear();
				items *= knowledge * 25;
				while (items > 0) {
					int i = ExperienceOrbEntity.getExperienceValue(items);
					items -= i;
					p.level.addFreshEntity(new ExperienceOrbEntity(p.level, target.getX(), target.getY(), target.getZ(), i));
				}
			}
			ApotheosisObjects.SPEARFISHING.addFishes(e);
		}
	}

	/**
	 * Event handler for the Life Mending enchantment
	 */
	@SubscribeEvent
	public void lifeMend(LivingHealEvent e) {
		ApotheosisObjects.LIFE_MENDING.lifeMend(e);
	}

	/**
	 * Event handler for the Stable Footing and Miner's Fervor enchants.
	 */
	@SubscribeEvent
	public void breakSpeed(PlayerEvent.BreakSpeed e) {
		PlayerEntity p = e.getPlayer();
		if (!p.isOnGround() && EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.STABLE_FOOTING, p) > 0) {
			if (e.getOriginalSpeed() < e.getNewSpeed() * 5) e.setNewSpeed(e.getNewSpeed() * 5F);
		}
		ItemStack stack = p.getMainHandItem();
		if (stack.isEmpty()) return;
		int depth = EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.DEPTH_MINER, stack);
		if (depth > 0) {
			if (stack.getDestroySpeed(e.getState()) > 1.0F) {
				float hardness = e.getState().getDestroySpeed(e.getPlayer().level, e.getPos());
				e.setNewSpeed(Math.min(29.99F, 7.5F + 4.5F * depth) * hardness);
			}
		}
	}

	/**
	 * Event handler for the Nature's Blessing enchantment.
	 */
	@SubscribeEvent
	public void rightClick(PlayerInteractEvent.RightClickBlock e) {
		ItemStack s = e.getItemStack();
		int nbLevel = EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.NATURES_BLESSING, s);
		if (!e.getEntity().isShiftKeyDown() && nbLevel > 0 && BoneMealItem.applyBonemeal(s.copy(), e.getWorld(), e.getPos(), e.getPlayer())) {
			s.hurtAndBreak(6 - nbLevel, e.getPlayer(), ent -> ent.broadcastBreakEvent(e.getHand()));
			e.setCanceled(true);
			e.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	/**
	 * Event handler for Anvil Unbreaking.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void applyUnbreaking(AnvilRepairEvent e) {
		if (e.getPlayer().containerMenu instanceof RepairContainer) {
			RepairContainer r = (RepairContainer) e.getPlayer().containerMenu;
			TileEntity te = r.access.evaluate(World::getBlockEntity).orElse(null);
			if (te instanceof AnvilTile) e.setBreakChance(e.getBreakChance() / (((AnvilTile) te).getEnchantments().getInt(Enchantments.UNBREAKING) + 1));
		}
	}

	/**
	 * Handles the Berserker's Fury and Occult Aversion enchantments.
	 */
	@SubscribeEvent
	public void livingHurt(LivingHurtEvent e) {
		LivingEntity user = e.getEntityLiving();
		if (e.getSource().getEntity() instanceof Entity && user.getEffect(Effects.DAMAGE_RESISTANCE) == null) {
			int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.BERSERK, user);
			if (level > 0) {
				user.invulnerableTime = 0;
				user.hurt(EnchModule.CORRUPTED, level * level);
				user.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 200 * level, level - 1));
				user.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 200 * level, level - 1));
				user.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 200 * level, level - 1));
			}
		}
		if (e.getSource().isMagic() && !e.getSource().isBypassMagic() && e.getSource().getEntity() instanceof LivingEntity) {
			LivingEntity src = (LivingEntity) e.getSource().getEntity();
			int lvl = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.MAGIC_PROTECTION, src);
			if (lvl > 0) {
				e.setAmount(CombatRules.getDamageAfterMagicAbsorb(e.getAmount(), lvl * 2));
			}
		}
	}

	@SubscribeEvent
	public void reloads(AddReloadListenerEvent e) {
		e.addListener(EnchantingStatManager.INSTANCE);
	}

	public static interface TridentGetter {
		ItemStack getTridentItem();
	}

	@SubscribeEvent
	public void looting(LootingLevelEvent e) {
		DamageSource src = e.getDamageSource();
		if (src != null && src.getDirectEntity() instanceof TridentEntity) {
			ItemStack triStack = ((TridentGetter) src.getDirectEntity()).getTridentItem();
			e.setLootingLevel(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, triStack));
		}
	}
	
	/**
	 * Event handler for the Boon of the Earth enchant.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void breakSpeed(BlockEvent.BreakEvent e) {
		ApotheosisObjects.EARTHS_BOON.provideBenefits(e);
		ApotheosisObjects.CHAINSAW.chainsaw(e);
	}

}