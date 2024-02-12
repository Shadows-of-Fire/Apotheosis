package shadows.apotheosis.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.injection.Redirect;
import shadows.placebo.util.EnchantmentUtils;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @ModifyConstant(method = "createResult()V", constant = @Constant(intValue = 40))
    public int apoth_removeLevelCap(int old) {
        return Integer.MAX_VALUE;
    }

    /**
     * Reduces the XP cost to the "optimal" cost (the amount of XP that would have been used if the player had exactly that level).
     *
     * @param player The player using the anvil.
     * @param level  The negative of the cost of performing the anvil operation.
     */
    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    public void apoth_chargeOptimalLevels(Player player, int level) {
        EnchantmentUtils.chargeExperience(player, EnchantmentUtils.getTotalExperienceForLevel(-level));
    }
}
