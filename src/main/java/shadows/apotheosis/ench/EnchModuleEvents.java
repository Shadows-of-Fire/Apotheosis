package shadows.apotheosis.ench;

import java.lang.reflect.Method;
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
import net.minecraft.inventory.EquipmentSlotType;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.objects.ScrappingTomeItem;
import shadows.placebo.util.ReflectionHelper;

public class EnchModuleEvents {

	@SubscribeEvent
	public void anvilEvent(AnvilUpdateEvent e) {
		if (!EnchantmentHelper.getEnchantments(e.getLeft()).isEmpty()) {
			if (e.getRight().getItem() == Items.COBWEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> ent.getKey().isCurse()).collect(Collectors.toMap(ent -> ent.getKey(), ent -> ent.getValue())), stack);
				e.setCost(1);
				e.setMaterialCost(1);
				e.setOutput(stack);
			} else if (e.getRight().getItem() == ApotheosisObjects.PRISMATIC_WEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> !ent.getKey().isCurse()).collect(Collectors.toMap(ent -> ent.getKey(), ent -> ent.getValue())), stack);
				e.setCost(30);
				e.setMaterialCost(1);
				e.setOutput(stack);
				return;
			}
		}
		if ((e.getLeft().getItem() == Items.CHIPPED_ANVIL || e.getLeft().getItem() == Items.DAMAGED_ANVIL) && e.getRight().getItem().isIn(Tags.Items.STORAGE_BLOCKS_IRON)) {
			if (e.getLeft().getCount() != 1) return;
			int dmg = e.getLeft().getItem() == Items.DAMAGED_ANVIL ? 2 : 1;
			ItemStack out = new ItemStack(dmg == 1 ? Items.ANVIL : Items.CHIPPED_ANVIL);
			EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(e.getLeft()), out);
			out.setCount(1);
			e.setOutput(out);
			e.setCost(5 + EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, e.getLeft()) + EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.SPLITTING, e.getLeft()) * 2);
			e.setMaterialCost(1);
			return;
		}
		if (e.getLeft().getItem() == ApotheosisObjects.HELLSHELF.asItem() || e.getLeft().getItem() == ApotheosisObjects.SEASHELF.asItem()) {
			if (e.getLeft().getItem() != e.getRight().getItem() || e.getLeft().getCount() != 1) return;
			Enchantment ench = e.getLeft().getItem() == ApotheosisObjects.HELLSHELF.asItem() ? ApotheosisObjects.HELL_INFUSION : ApotheosisObjects.SEA_INFUSION;
			int leftLvl = EnchantmentHelper.getEnchantmentLevel(ench, e.getLeft());
			int rightLvl = EnchantmentHelper.getEnchantmentLevel(ench, e.getRight());
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
		Entity attacker = e.getSource().getTrueSource();
		if (attacker instanceof PlayerEntity) {
			PlayerEntity p = (PlayerEntity) attacker;
			if (p.world.isRemote) return;
			int scavenger = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.SCAVENGER, p.getHeldItemMainhand());
			if (scavenger > 0 && p.world.rand.nextInt(100) < scavenger * 2.5F) {
				if (dropLoot == null) {
					dropLoot = ReflectionHelper.findMethod(LivingEntity.class, "dropLoot", "func_213354_a", DamageSource.class, boolean.class);
				}
				dropLoot.invoke(e.getEntityLiving(), e.getSource(), true);
			}
			int knowledge = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.KNOWLEDGE, p.getHeldItemMainhand());
			if (knowledge > 0 && !(e.getEntity() instanceof PlayerEntity)) {
				int items = 0;
				for (ItemEntity i : e.getDrops())
					items += i.getItem().getCount();
				if (items > 0) e.getDrops().clear();
				items *= knowledge * 25;
				Entity ded = e.getEntityLiving();
				while (items > 0) {
					int i = ExperienceOrbEntity.getXPSplit(items);
					items -= i;
					p.world.addEntity(new ExperienceOrbEntity(p.world, ded.getPosX(), ded.getPosY(), ded.getPosZ(), i));
				}
			}
		}
	}

	final EquipmentSlotType[] slots = EquipmentSlotType.values();

	/**
	 * Event handler for the Life Mending enchantment
	 */
	@SubscribeEvent
	public void lifeMend(LivingUpdateEvent e) {
		if (e.getEntity().world.isRemote || e.getEntity().ticksExisted % 20 != 0) return;
		for (EquipmentSlotType slot : slots) {
			ItemStack stack = e.getEntityLiving().getItemStackFromSlot(slot);
			if (!stack.isEmpty() && stack.isDamaged()) {
				int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.LIFE_MENDING, stack);
				if (level > 0) {
					int i = Math.min(level, stack.getDamage());
					e.getEntityLiving().attackEntityFrom(EnchModule.CORRUPTED, i * 0.7F);
					stack.setDamage(stack.getDamage() - i);
					return;
				}
			}
		}
	}

	/**
	 * Event handler for the Stable Footing and Miner's Fervor enchants.
	 */
	@SubscribeEvent
	public void breakSpeed(PlayerEvent.BreakSpeed e) {
		PlayerEntity p = e.getPlayer();
		if (!p.isOnGround() && EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.STABLE_FOOTING, p) > 0) {
			if (e.getOriginalSpeed() < e.getNewSpeed() * 5) e.setNewSpeed(e.getNewSpeed() * 5F);
		}
		ItemStack stack = p.getHeldItemMainhand();
		if (stack.isEmpty()) return;
		int depth = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.DEPTH_MINER, stack);
		if (depth > 0) {
			if (stack.getDestroySpeed(e.getState()) > 1.0F) {
				float hardness = e.getState().getBlockHardness(e.getPlayer().world, e.getPos());
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
		int nbLevel = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.NATURES_BLESSING, s);
		if (!e.getEntity().isSneaking() && nbLevel > 0 && BoneMealItem.applyBonemeal(s.copy(), e.getWorld(), e.getPos(), e.getPlayer())) {
			s.damageItem(6 - nbLevel, e.getPlayer(), ent -> ent.sendBreakAnimation(e.getHand()));
			e.setCanceled(true);
			e.setCancellationResult(ActionResultType.SUCCESS);
		}
	}

	/**
	 * Event handler for Anvil Unbreaking.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void applyUnbreaking(AnvilRepairEvent e) {
		if (e.getPlayer().openContainer instanceof RepairContainer) {
			RepairContainer r = (RepairContainer) e.getPlayer().openContainer;
			TileEntity te = r.field_234644_e_.apply((w, p) -> w.getTileEntity(p)).orElse(null);
			if (te instanceof AnvilTile) e.setBreakChance(e.getBreakChance() / (((AnvilTile) te).getEnchantments().getInt(Enchantments.UNBREAKING) + 1));
		}
	}

	/**
	 * Handles the Berserker's Fury and Occult Aversion enchantments.
	 */
	@SubscribeEvent
	public void livingHurt(LivingHurtEvent e) {
		LivingEntity user = e.getEntityLiving();
		if (e.getSource().getTrueSource() instanceof Entity && user.getActivePotionEffect(Effects.RESISTANCE) == null) {
			int level = EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.BERSERK, user);
			if (level > 0) {
				user.attackEntityFrom(EnchModule.CORRUPTED, level * level);
				user.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 200 * level, level - 1));
				user.addPotionEffect(new EffectInstance(Effects.STRENGTH, 200 * level, level - 1));
				user.addPotionEffect(new EffectInstance(Effects.SPEED, 200 * level, level - 1));
			}
		}
		if (e.getSource().isMagicDamage() && e.getSource().getTrueSource() instanceof LivingEntity) {
			LivingEntity src = (LivingEntity) e.getSource().getTrueSource();
			int lvl = EnchantmentHelper.getMaxEnchantmentLevel(ApotheosisObjects.MAGIC_PROTECTION, src);
			if (lvl > 0) {
				//TODO: FIXME should only be reducing damage by the value of OA, this will use all active protection enchantments.
				e.setAmount(CombatRules.getDamageAfterMagicAbsorb(e.getAmount(), EnchantmentHelper.getEnchantmentModifierDamage(src.getArmorInventoryList(), e.getSource())));
			}
		}
	}

}
