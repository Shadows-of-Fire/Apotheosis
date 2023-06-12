package shadows.apotheosis.mixin;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin extends TargetGoal {

	public NearestAttackableTargetGoalMixin(Mob pMob, boolean pMustSee) {
		super(pMob, pMustSee);
	}

	@Nullable
	Predicate<LivingEntity> ctorTargetPredicate;

	@Shadow
	TargetingConditions targetConditions;

	@Inject(method = "<init>(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V", at = @At("TAIL"))
	private void apoth_cachePredicate(Mob pMob, Class<?> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate, CallbackInfo ci) {
		this.ctorTargetPredicate = pTargetPredicate;
	}

	/**
	 * Normally, the follow range is encoded into the TargetingConditions at construction time.<br>
	 * This means that modifications to it (via attribute modifiers) won't actually change anything.<br>
	 * This mixin makes it update before use, so the real value is used.
	 * <p>
	 * Technically {@link TargetGoal#canContinueToUse()} uses the real value, which should kick it back after a delay.
	 */
	@Inject(method = "findTarget()V", at = @At("HEAD"))
	private void apoth_updateFollowRange(CallbackInfo ci) {
		this.targetConditions.range(this.getFollowDistance());
	}

}
