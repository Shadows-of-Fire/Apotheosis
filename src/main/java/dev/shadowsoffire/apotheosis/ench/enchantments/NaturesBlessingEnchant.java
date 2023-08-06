package dev.shadowsoffire.apotheosis.ench.enchantments;

import dev.shadowsoffire.apotheosis.ench.EnchModule;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class NaturesBlessingEnchant extends Enchantment {

    public NaturesBlessingEnchant() {
        super(Rarity.RARE, EnchModule.HOE, new EquipmentSlot[0]);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 25 + level * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return 200;
    }

    public void rightClick(PlayerInteractEvent.RightClickBlock e) {
        ItemStack s = e.getItemStack();
        int nbLevel = s.getEnchantmentLevel(this);
        if (!e.getEntity().isShiftKeyDown() && nbLevel > 0 && BoneMealItem.applyBonemeal(s.copy(), e.getLevel(), e.getPos(), e.getEntity())) {
            s.hurtAndBreak(Math.max(1, 6 - nbLevel), e.getEntity(), ent -> ent.broadcastBreakEvent(e.getHand()));
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

}
