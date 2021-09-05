package shadows.apotheosis.ench;

import java.lang.reflect.Method;
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
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
		if (attacker instanceof PlayerEntity) {
			PlayerEntity p = (PlayerEntity) attacker;
			if (p.level.isClientSide) return;
			int scavenger = EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.SCAVENGER, p.getMainHandItem());
			if (scavenger > 0 && p.level.random.nextInt(100) < scavenger * 2.5F) {
				if (this.dropLoot == null) {
					this.dropLoot = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_213354_a", DamageSource.class, boolean.class);
				}
				this.dropLoot.invoke(e.getEntityLiving(), e.getSource(), true);
			}
			int knowledge = EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.KNOWLEDGE, p.getMainHandItem());
			if (knowledge > 0 && !(e.getEntity() instanceof PlayerEntity)) {
				int items = 0;
				for (ItemEntity i : e.getDrops())
					items += i.getItem().getCount();
				if (items > 0) e.getDrops().clear();
				items *= knowledge * 25;
				Entity ded = e.getEntityLiving();
				while (items > 0) {
					int i = ExperienceOrbEntity.getExperienceValue(items);
					items -= i;
					p.level.addFreshEntity(new ExperienceOrbEntity(p.level, ded.getX(), ded.getY(), ded.getZ(), i));
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
		if (e.getEntity().level.isClientSide || e.getEntity().tickCount % 20 != 0) return;
		for (EquipmentSlotType slot : this.slots) {
			ItemStack stack = e.getEntityLiving().getItemBySlot(slot);
			if (!stack.isEmpty() && stack.isDamaged()) {
				int level = EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.LIFE_MENDING, stack);
				if (level > 0) {
					int i = Math.min(level, stack.getDamageValue());
					e.getEntityLiving().hurt(EnchModule.CORRUPTED, i * 0.7F);
					stack.setDamageValue(stack.getDamageValue() - i);
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

	@SubscribeEvent
	public void login(PlayerLoggedInEvent e) {
		PlayerEntity p = e.getPlayer();
		if (!p.level.isClientSide) {
			EnchantingStatManager.dispatch(p);
		}
	}

	/*
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void updateRepairOutput(AnvilUpdateEvent e) {
		ItemStack left = e.getLeft();
		ItemStack right = e.getRight();
		int i = 0;
		int j = 0;
		int k = 0;
		{
			ItemStack leftCopy = left.copy();
			Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(leftCopy);
			j = j + left.getRepairCost() + (right.isEmpty() ? 0 : right.getRepairCost());
			int materialCost = 0;
			boolean isRightEnchBook = right.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(right).isEmpty();
			if (leftCopy.isDamageable() && leftCopy.getItem().getIsRepairable(left, right)) { //Repair Material case
				int repairNeeded = Math.min(leftCopy.getDamage(), leftCopy.getMaxDamage() / 4);
				if (repairNeeded <= 0) return;

				int matCostIterator;
				for (matCostIterator = 0; repairNeeded > 0 && matCostIterator < right.getCount(); ++matCostIterator) {
					int j3 = leftCopy.getDamage() - repairNeeded;
					leftCopy.setDamage(j3);
					++i;
					repairNeeded = Math.min(leftCopy.getDamage(), leftCopy.getMaxDamage() / 4);
				}

				materialCost = matCostIterator;
			} else {
				if (!isRightEnchBook && (leftCopy.getItem() != right.getItem() || !leftCopy.isDamageable())) { return; }

				if (leftCopy.isDamageable() && !isRightEnchBook) { //Two Item Merge case
					int repairNeeded = left.getMaxDamage() - left.getDamage();
					int i1 = right.getMaxDamage() - right.getDamage();
					int j1 = i1 + leftCopy.getMaxDamage() * 12 / 100;
					int k1 = repairNeeded + j1;
					int l1 = leftCopy.getMaxDamage() - k1;
					if (l1 < 0) {
						l1 = 0;
					}

					if (l1 < leftCopy.getDamage()) {
						leftCopy.setDamage(l1);
						i += 2;
					}
				}

				Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(right);
				boolean flag2 = false;
				boolean flag3 = false;

				for (Enchantment enchantment1 : map1.keySet()) {
					if (enchantment1 != null) {
						int i2 = map.getOrDefault(enchantment1, 0);
						int j2 = map1.get(enchantment1);
						j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);
						boolean flag1 = enchantment1.canApply(left);
						if (e.getPlayer().abilities.isCreativeMode || left.getItem() == Items.ENCHANTED_BOOK) {
							flag1 = true;
						}

						for (Enchantment enchantment : map.keySet()) {
							if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
								flag1 = false;
								++i;
							}
						}

						if (!flag1) {
							flag3 = true;
						} else {
							flag2 = true;
							if (j2 > enchantment1.getMaxLevel()) {
								j2 = enchantment1.getMaxLevel();
							}

							map.put(enchantment1, j2);
							int k3 = 0;
							switch (enchantment1.getRarity()) {
							case COMMON:
								k3 = 1;
								break;
							case UNCOMMON:
								k3 = 2;
								break;
							case RARE:
								k3 = 4;
								break;
							case VERY_RARE:
								k3 = 8;
							}

							if (isRightEnchBook) {
								k3 = Math.max(1, k3 / 2);
							}

							i += k3 * j2;
							if (itemstack.getCount() > 1) {
								i = 40;
							}
						}
					}
				}

				if (flag3 && !flag2) {
					this.resultSlots.setInventorySlotContents(0, ItemStack.EMPTY);
					this.maximumCost.set(0);
					return;
				}
			}
		}

		if (StringUtils.isBlank(this.repairedItemName)) {
			if (itemstack.hasDisplayName()) {
				k = 1;
				i += k;
				leftCopy.clearCustomName();
			}
		} else if (!this.repairedItemName.equals(itemstack.getDisplayName().getString())) {
			k = 1;
			i += k;
			leftCopy.setDisplayName(new StringTextComponent(this.repairedItemName));
		}
		if (flag && !leftCopy.isBookEnchantable(right)) leftCopy = ItemStack.EMPTY;

		this.maximumCost.set(j + i);
		if (i <= 0) {
			leftCopy = ItemStack.EMPTY;
		}

		if (k == i && k > 0 && this.maximumCost.get() >= 40) {
			this.maximumCost.set(39);
		}

		if (this.maximumCost.get() >= 40 && !this.player.abilities.isCreativeMode) {
			leftCopy = ItemStack.EMPTY;
		}

		if (!leftCopy.isEmpty()) {
			int k2 = leftCopy.getRepairCost();
			if (!right.isEmpty() && k2 < right.getRepairCost()) {
				k2 = right.getRepairCost();
			}

			if (k != i || k == 0) {
				k2 = getNewRepairCost(k2);
			}

			leftCopy.setRepairCost(k2);
			EnchantmentHelper.setEnchantments(map, leftCopy);
		}

		this.resultSlots.setInventorySlotContents(0, leftCopy);
		this.detectAndSendChanges();
	}*/

}