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

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

@Mixin(SheepEntity.class)
public class SheepMixin {

	@Inject(method = "onSheared", at = @At("RETURN"), remap = false, cancellable = true)
	public void onSheared(@Nullable PlayerEntity player, @Nonnull ItemStack item, World world, BlockPos pos, int fortune, CallbackInfoReturnable<List<ItemStack>> ci) {
		if (Apotheosis.enableEnch) {
			ci.setReturnValue(ApotheosisObjects.CHROMATIC.molestSheepItems((SheepEntity) (Object) this, item, ci.getReturnValue()));
			ci.setReturnValue(ApotheosisObjects.EXPLOITATION.molestSheepItems((SheepEntity) (Object) this, item, ci.getReturnValue()));
			ApotheosisObjects.GROWTH_SERUM.unshear((SheepEntity) (Object) this, item);
		}
	}

	@ModifyConstant(method = "onSheared", constant = @Constant(intValue = 3), remap = false)
	public int onSheared(int oldVal, @Nullable PlayerEntity player, @Nonnull ItemStack item, World world, BlockPos pos, int fortune) {
		return oldVal + fortune * 2;
	}

}
