package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.CrescendoEnchant;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Pseudo
@Mixin(targets = "com.hollingsworth.arsnouveau.common.items.SpellCrossbow")
public class SpellCrossbowMixin extends CrossbowItem {

    public SpellCrossbowMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(method = "m_7203_", at = @At(value = "INVOKE", target = "Lcom/hollingsworth/arsnouveau/common/items/SpellCrossbow;shootStoredProjectiles(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V", remap = false))
    public void apoth_preFired(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci) {
        CrescendoEnchant.preArrowFired(pPlayer.getItemInHand(pHand));
    }

    @Inject(method = "m_7203_", at = @At(value = "RETURN", ordinal = 0))
    public void apoth_addCharges(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci) {
        CrescendoEnchant.onArrowFired(pPlayer.getItemInHand(pHand));
    }

    @Inject(method = "getArrow", at = @At(value = "RETURN"), remap = false)
    private void apoth_markArrows(Level pLevel, LivingEntity pLivingEntity, ItemStack pCrossbowStack, ItemStack pAmmoStack, CallbackInfoReturnable<AbstractArrow> ci) {
        CrescendoEnchant.markGeneratedArrows(ci.getReturnValue(), pCrossbowStack);
    }

}
