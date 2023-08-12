package dev.shadowsoffire.apotheosis.ench;

import java.util.Map.Entry;
import java.util.stream.Collectors;

import dev.shadowsoffire.apotheosis.ench.anvil.AnvilTile;
import dev.shadowsoffire.apotheosis.ench.objects.ExtractionTomeItem;
import dev.shadowsoffire.apotheosis.ench.objects.ImprovedScrappingTomeItem;
import dev.shadowsoffire.apotheosis.ench.objects.ScrappingTomeItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
            }
            else if (e.getRight().getItem() == dev.shadowsoffire.apotheosis.ench.Ench.Items.PRISMATIC_WEB.get()) {
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
            e.setCost(5 + e.getLeft().getAllEnchantments().entrySet().stream().mapToInt(ent -> ent.getValue() * (ent.getKey().getRarity().ordinal() + 1)).sum());
            e.setMaterialCost(1);
            return;
        }
        if (ScrappingTomeItem.updateAnvil(e)) return;
        if (ImprovedScrappingTomeItem.updateAnvil(e)) return;
        if (ExtractionTomeItem.updateAnvil(e)) return;
    }

    @SubscribeEvent
    public void repairEvent(AnvilRepairEvent e) {
        if (ExtractionTomeItem.updateRepair(e)) return;
    }

    /**
     * Event handler for the Scavenger and Spearfishing enchantments.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void drops(LivingDropsEvent e) throws Throwable {
        if (e.getSource().getEntity() instanceof Player p) {
            dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.SCAVENGER.get().drops(p, e);
            dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.SPEARFISHING.get().addFishes(e);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void dropsLowest(LivingDropsEvent e) {
        if (e.getSource().getEntity() instanceof Player p) {
            dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.KNOWLEDGE.get().drops(p, e);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void healing(LivingHealEvent e) {
        if (e.getEntity().getType() == EntityType.ARMOR_STAND) return; // https://github.com/Shadows-of-Fire/Apotheosis/issues/636
        dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.LIFE_MENDING.get().lifeMend(e);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void block(ShieldBlockEvent e) {
        dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.REFLECTIVE.get().reflect(e);
    }

    @SubscribeEvent
    public void looting(LootingLevelEvent e) {
        DamageSource src = e.getDamageSource();
        if (src != null && src.getDirectEntity() instanceof ThrownTrident trident) {
            ItemStack triStack = ((TridentGetter) trident).getTridentItem();
            e.setLootingLevel(triStack.getEnchantmentLevel(Enchantments.MOB_LOOTING));
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
        dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.STABLE_FOOTING.get().breakSpeed(e);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void breakSpeedLow(PlayerEvent.BreakSpeed e) {
        dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.MINERS_FERVOR.get().breakSpeed(e);
    }

    /**
     * Event handler for the Boon of the Earth enchant.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void breakSpeed(BlockEvent.BreakEvent e) {
        dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.EARTHS_BOON.get().provideBenefits(e);
        dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.CHAINSAW.get().chainsaw(e);
    }

    /**
     * Event handler for the Nature's Blessing enchantment.
     */
    @SubscribeEvent
    public void rightClick(PlayerInteractEvent.RightClickBlock e) {
        dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.NATURES_BLESSING.get().rightClick(e);
    }

    /**
     * Event handler for Anvil Unbreaking.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void applyUnbreaking(AnvilRepairEvent e) {
        if (e.getEntity().containerMenu instanceof AnvilMenu anvMenu) {
            anvMenu.access.execute((level, pos) -> {
                if (level.getBlockEntity(pos) instanceof AnvilTile anvil) {
                    e.setBreakChance(e.getBreakChance() / (anvil.getEnchantments().getInt(Enchantments.UNBREAKING) + 1));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void livingHurt(LivingHurtEvent e) {
        dev.shadowsoffire.apotheosis.ench.Ench.Enchantments.BERSERKERS_FURY.get().livingHurt(e);
    }

}
