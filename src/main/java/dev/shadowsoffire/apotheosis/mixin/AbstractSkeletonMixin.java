package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * This mixin allows {@link AbstractSkeleton} to shoot crossbows instead of trying to use them as a melee weapon.
 * <p>
 * This functionality requires a couple different components:
 * <ol>
 * <li>The skeleton class must implement {@link CrossbowAttackMob} and override the interface methods {@link #setChargingCrossbow(boolean)} and
 * {@link #onCrossbowAttackPerformed()}</li>
 * <li>An injection must be made to {@link #performRangedAttack(LivingEntity, float)} to shoot the crossbow if we are currently wielding one</li>
 * <li>An injection must be made to {@link AbstractSkeleton#reassessWeaponGoal()} to pick the crossbow goal when wielding one</li>
 * <li>A final injection must be made to {@link AbstractSkeleton#canFireProjectileWeapon} so that the skeleton can attempt to fire the crossbow</li>
 * </ol>
 * Then, in addition to this, we need a clientside mixin must be made to have the skeleton raise the crossbow when it is loaded, similar to a Pillager.
 */
@Mixin(value = AbstractSkeleton.class, remap = false)
public abstract class AbstractSkeletonMixin extends Monster implements CrossbowAttackMob {

    @Unique
    protected final RangedCrossbowAttackGoal<AbstractSkeletonMixin> apoth_crossbowGoal = new RangedCrossbowAttackGoal<>(this, 1, 15F);

    @Final
    @Shadow
    private RangedBowAttackGoal<AbstractSkeleton> bowGoal;

    @Final
    @Shadow
    private MeleeAttackGoal meleeGoal;

    protected AbstractSkeletonMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
    public void apoth_performRangedCrossbowAttack(LivingEntity target, float velocity, CallbackInfo ci) {
        ItemStack crossbow = this.apoth_getCrossbow();
        if (!crossbow.isEmpty()) {
            this.performCrossbowAttack(this, 1.6F);
            ci.cancel();
        }
    }

    @Inject(method = "reassessWeaponGoal()V", at = @At("HEAD"), cancellable = true)
    public void apoth_pickCrossbowIfAvailable(CallbackInfo ci) {
        if (this.level() != null && !this.level().isClientSide()) {
            // Always remove the crossbow goal in case we pass to vanilla logic.
            this.goalSelector.removeGoal(this.apoth_crossbowGoal);

            // Check if we have a crossbow, and if we do, apply the crossbow goal.
            ItemStack crossbow = this.apoth_getCrossbow();
            if (!crossbow.isEmpty()) {
                this.goalSelector.removeGoal(this.meleeGoal);
                this.goalSelector.removeGoal(this.bowGoal);
                this.goalSelector.addGoal(4, this.apoth_crossbowGoal);
                ci.cancel();
            }
        }
    }

    @Override
    public void setChargingCrossbow(boolean chargingCrossbow) {
        // This doesn't need to do anything, since we don't record that the crossbow is charging.
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Unique
    private ItemStack apoth_getCrossbow() {
        ItemStack stack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, CrossbowItem.class::isInstance));
        if (stack.getItem() instanceof CrossbowItem) {
            return stack;
        }
        return ItemStack.EMPTY;
    }

}
