package shadows.apotheosis.mixin;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;

@Mixin(Sheep.class)
public class SheepMixin {

    @Inject(method = "onSheared", at = @At("RETURN"), remap = false, cancellable = true)
    public void onSheared(@Nullable Player player, @Nonnull ItemStack item, Level world, BlockPos pos, int fortune, CallbackInfoReturnable<List<ItemStack>> ci) {
        if (Apotheosis.enableEnch) {
            ci.setReturnValue(Apoth.Enchantments.CHROMATIC.get().molestSheepItems((Sheep) (Object) this, item, ci.getReturnValue()));
            ci.setReturnValue(Apoth.Enchantments.EXPLOITATION.get().molestSheepItems((Sheep) (Object) this, item, ci.getReturnValue()));
            Apoth.Enchantments.GROWTH_SERUM.get().unshear((Sheep) (Object) this, item);
        }
    }

    @ModifyConstant(method = "onSheared", constant = @Constant(intValue = 3), remap = false)
    public int onSheared(int oldVal, @Nullable Player player, @Nonnull ItemStack item, Level world, BlockPos pos, int fortune) {
        return oldVal + fortune * 2;
    }

}
