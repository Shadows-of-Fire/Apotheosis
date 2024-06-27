package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.ench.EnchModuleEvents.TridentGetter;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Mixin to tridents to enable Piercing to work.
 */
@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow implements TridentGetter {

    int pierces = 0;
    Vec3 oldVel = null;

    @Shadow
    private boolean dealtDamage;

    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    @Override
    @Accessor
    public abstract ItemStack getTridentItem();

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"), require = 1, remap = false)
    private void init(CallbackInfo ci) {
        this.setPierceLevel((byte) this.getTridentItem().getEnchantmentLevel(Enchantments.PIERCING));
    }

    @Inject(method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V", at = @At("HEAD"), cancellable = true, require = 1)
    public void startHitEntity(EntityHitResult res, CallbackInfo ci) {
        if (this.getPierceLevel() > 0 && !this.isNoPhysics()) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(this.getPierceLevel());
            }
            if (this.piercingIgnoreEntityIds.contains(res.getEntity().getId())) {
                ci.cancel();
            }
            this.oldVel = this.getDeltaMovement();
        }
    }

    @Inject(method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V", at = @At("TAIL"), cancellable = true, require = 1)
    public void endHitEntity(EntityHitResult res, CallbackInfo ci) {
        if (this.getPierceLevel() > 0 && !this.isNoPhysics()) {
            this.piercingIgnoreEntityIds.add(res.getEntity().getId());

            if (this.piercingIgnoreEntityIds.size() <= this.getPierceLevel()) {
                this.dealtDamage = false;
                if (this.oldVel != null) {
                    this.setDeltaMovement(this.oldVel);
                }
            }
        }
    }

}
