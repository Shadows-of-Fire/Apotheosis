package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.damagesource.DamageSource;
import shadows.apotheosis.util.DamageSourceUtil.DmgSrcCopy;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DmgSrcCopy {

	@Shadow private boolean damageHelmet;
	@Shadow private boolean bypassArmor;
	@Shadow private boolean bypassInvul;
	@Shadow private boolean bypassMagic;
	@Shadow private float exhaustion = 0.1F;
	@Shadow private boolean isFireSource;
	@Shadow private boolean isProjectile;
	@Shadow private boolean scalesWithDifficulty;
	@Shadow private boolean isMagic;
	@Shadow private boolean isExplosion;
	@Shadow private boolean isFall;
	@Shadow private boolean noAggro;

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

}