package shadows.apotheosis.ench;

import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.objects.ScrappingTomeItem;

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
			} else if (e.getRight().getItem() == Apoth.Items.PRISMATIC_WEB) {
				ItemStack stack = e.getLeft().copy();
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(ent -> !ent.getKey().isCurse()).collect(Collectors.toMap(Entry::getKey, Entry::getValue)), stack);
				e.setCost(30);
				e.setMaterialCost(1);
				e.setOutput(stack);
				return;
			}
		}
		if ((e.getLeft().getItem() == Items.CHIPPED_ANVIL || e.getLeft().getItem() == Items.DAMAGED_ANVIL) && e.getRight().is(Tags.Items.STORAGE_BLOCKS_IRON)) {
			if (e.getLeft().getCount() != 1) return;
			int dmg = e.getLeft().getItem() == Items.DAMAGED_ANVIL ? 2 : 1;
			ItemStack out = new ItemStack(dmg == 1 ? Items.ANVIL : Items.CHIPPED_ANVIL);
			EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(e.getLeft()), out);
			out.setCount(1);
			e.setOutput(out);
			e.setCost(5 + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, e.getLeft()) + EnchantmentHelper.getItemEnchantmentLevel(Apoth.Enchantments.SPLITTING, e.getLeft()) * 2);
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
	public void drops(LivingDropsEvent e) throws Throwable {
		Entity attacker = e.getSource().getEntity();
		if (attacker instanceof Player p) {
			Apoth.Enchantments.SCAVENGER.drops(p, e);
			Apoth.Enchantments.SPEARFISHING.addFishes(e);
			Apoth.Enchantments.KNOWLEDGE.drops(p, e);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void healing(LivingHealEvent e) {
		Apoth.Enchantments.LIFE_MENDING.lifeMend(e);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void block(ShieldBlockEvent e) {
		Apoth.Enchantments.REFLECTIVE.reflect(e);
	}

	@SubscribeEvent
	public void looting(LootingLevelEvent e) {
		DamageSource src = e.getDamageSource();
		if (src != null && src.getDirectEntity() instanceof ThrownTrident trident) {
			ItemStack triStack = ((TridentGetter) trident).getTridentItem();
			e.setLootingLevel(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, triStack));
		}
	}

	public static interface TridentGetter {
		ItemStack getTridentItem();
	}

	/**
	 * Event handler for the Stable Footing and Miner's Fervor enchants.
	 */
	@SubscribeEvent
	public void breakSpeed(PlayerEvent.BreakSpeed e) {
		Apoth.Enchantments.STABLE_FOOTING.breakSpeed(e);
		Apoth.Enchantments.MINERS_FERVOR.breakSpeed(e);
	}

	/**
	 * Event handler for the Boon of the Earth enchant.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void breakSpeed(BlockEvent.BreakEvent e) {
		Apoth.Enchantments.EARTHS_BOON.provideBenefits(e);
		Apoth.Enchantments.CHAINSAW.chainsaw(e);
	}

	/**
	 * Event handler for the Nature's Blessing enchantment.
	 */
	@SubscribeEvent
	public void rightClick(PlayerInteractEvent.RightClickBlock e) {
		Apoth.Enchantments.NATURES_BLESSING.rightClick(e);
	}

	/**
	 * Event handler for Anvil Unbreaking.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void applyUnbreaking(AnvilRepairEvent e) {
		if (e.getPlayer().containerMenu instanceof AnvilMenu) {
			AnvilMenu r = (AnvilMenu) e.getPlayer().containerMenu;
			BlockEntity te = r.access.evaluate(Level::getBlockEntity).orElse(null);
			if (te instanceof AnvilTile) e.setBreakChance(e.getBreakChance() / (((AnvilTile) te).getEnchantments().getInt(Enchantments.UNBREAKING) + 1));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void livingHurt(LivingHurtEvent e) {
		Apoth.Enchantments.BERSERKERS_FURY.livingHurt(e);
	}

}