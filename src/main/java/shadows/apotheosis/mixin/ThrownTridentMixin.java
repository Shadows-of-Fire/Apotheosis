package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import shadows.apotheosis.ench.EnchModuleEvents.TridentGetter;

@Mixin(TridentEntity.class)
public abstract class ThrownTridentMixin extends AbstractArrowEntity implements TridentGetter {

	int pierces = 0;
	Vector3d oldVel = null;
	@Shadow
	private boolean dealtDamage;
	{

		this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
	}

	protected ThrownTridentMixin(EntityType<? extends AbstractArrowEntity> type, World level) {
		super(type, level);
	}

	@Override
	@Accessor
	public abstract ItemStack getTridentItem();

	@Inject(method = "onHitEntity(Lnet/minecraft/util/math/EntityRayTraceResult;)V", at = @At("HEAD"), cancellable = true)
	public void startHitEntity(EntityRayTraceResult res, CallbackInfo ci) {
		this.oldVel = this.getDeltaMovement();
		if (this.piercingIgnoreEntityIds.contains(res.getEntity().getId())) ci.cancel();
	}

	@Inject(method = "onHitEntity(Lnet/minecraft/util/math/EntityRayTraceResult;)V", at = @At("TAIL"), cancellable = true)
	public void endHitEntity(EntityRayTraceResult res, CallbackInfo ci) {
		int pierceLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, this.getTridentItem());
		if (this.pierces++ < pierceLevel) {
			this.dealtDamage = false;
			this.setDeltaMovement(this.oldVel);
			this.piercingIgnoreEntityIds.add(res.getEntity().getId());
		}
	}

}
