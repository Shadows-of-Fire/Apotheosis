package shadows.apotheosis.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shadows.apotheosis.ench.enchantments.masterwork.CrescendoEnchant;

@Pseudo
@Mixin(targets = "com.rolfmao.upgradednetherite.content.UpgradedNetheriteCrossbow")
public class UpgradedNetheriteCrossbowMixin extends CrossbowItem {
    public UpgradedNetheriteCrossbowMixin(final Properties properties) {
        super(properties);
    }

    @Inject(method = "m_7203_", at = @At(value = "INVOKE", target = "Lcom/rolfmao/upgradednetherite/content/UpgradedNetheriteCrossbow;performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V", remap = false))
    public void apoth_preFired(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci) {
        CrescendoEnchant.preArrowFired(pPlayer.getItemInHand(pHand));
    }

    @Inject(method = "m_7203_", at = @At(value = "RETURN", ordinal = 0))
    public void apoth_addCharges(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci) {
        CrescendoEnchant.onArrowFired(pPlayer.getItemInHand(pHand));
    }

    @Inject(method = "getArrow", at = @At(value = "RETURN"), remap = false)
    private static void apoth_markArrows(Level pLevel, LivingEntity pLivingEntity, ItemStack pCrossbowStack, ItemStack pAmmoStack, CallbackInfoReturnable<AbstractArrow> ci) {
        CrescendoEnchant.markGeneratedArrows(ci.getReturnValue(), pCrossbowStack);
    }
}
