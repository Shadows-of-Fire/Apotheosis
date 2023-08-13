package shadows.apotheosis.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Explosion;
import shadows.apotheosis.util.DamageSourceUtil.DmgSrcCopy;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DmgSrcCopy {

    @Shadow
    private boolean damageHelmet;
    @Shadow
    private boolean bypassArmor;
    @Shadow
    private boolean bypassInvul;
    @Shadow
    private boolean bypassMagic;
    @Shadow
    private float exhaustion = 0.1F;
    @Shadow
    private boolean isFireSource;
    @Shadow
    private boolean isProjectile;
    @Shadow
    private boolean scalesWithDifficulty;
    @Shadow
    private boolean isMagic;
    @Shadow
    private boolean isExplosion;
    @Shadow
    private boolean isFall;
    @Shadow
    private boolean noAggro;

    @Override
    public void copyFrom(DamageSource other) {
        DamageSourceMixin mix = (DamageSourceMixin) (Object) other;
        this.damageHelmet = mix.damageHelmet;
        this.bypassArmor = mix.bypassArmor;
        this.bypassInvul = mix.bypassInvul;
        this.bypassMagic = mix.bypassMagic;
        this.exhaustion = mix.exhaustion;
        this.isFireSource = mix.isFireSource;
        this.isProjectile = mix.isProjectile;
        this.scalesWithDifficulty = mix.scalesWithDifficulty;
        this.isMagic = mix.isMagic;
        this.isExplosion = mix.isExplosion;
        this.isFall = mix.isFall;
        this.noAggro = mix.noAggro;
    }

    // TODO: Remove 1.20 - Fixed by mojang
    @Inject(at = @At("HEAD"), method = "explosion(Lnet/minecraft/world/level/Explosion;)Lnet/minecraft/world/damagesource/DamageSource;", cancellable = true)
    private static void apoth_fixMC_92017(@Nullable Explosion src, CallbackInfoReturnable<DamageSource> cir) {
        if (src != null) {
            Entity exploder = src.getExploder();
            if (exploder instanceof PrimedTnt tnt) {
                cir.setReturnValue(new IndirectEntityDamageSource("explosion.player", tnt, tnt.getOwner()).setScalesWithDifficulty().setExplosion());
            }
            else if (exploder instanceof Projectile proj) {
                cir.setReturnValue(new IndirectEntityDamageSource("explosion.player", proj, proj.getOwner()).setScalesWithDifficulty().setExplosion());
            }
        }
    }

}
