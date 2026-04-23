package dev.shadowsoffire.apotheosis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.mixin.AbstractSkeletonMixin;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

/**
 * Mixin to {@link AbstractSkeletonRenderer} to support {@link AbstractSkeletonMixin}.
 * <p>
 * Overrides the computed arm pose to a crossbow pose when the skeleton is wielding a crossbow,
 * matching how player/illager renderers select {@code CROSSBOW_HOLD} or {@code CROSSBOW_CHARGE}.
 */
@Mixin(value = AbstractSkeletonRenderer.class, remap = false)
public abstract class AbstractSkeletonRendererMixin {

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private void apoth_overrideCrossbowPose(AbstractSkeleton mob, HumanoidArm arm, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        if (mob.getMainArm() != arm || !mob.isAggressive()) return;

        ItemStack held = mob.getMainHandItem();
        if (!(held.getItem() instanceof CrossbowItem)) return;

        if (mob.isUsingItem() && mob.getUseItem() == held) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_CHARGE);
        }
        else if (CrossbowItem.isCharged(held)) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        }
    }
}
