package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.extensions.IForgeEntity;

@Mixin(IForgeEntity.class)
public interface IForgeEntityMixin {

	/**
	 * This mixin overwrites {@link IForgeEntity#getStepHeight()} to completely phase-out {@link Entity#maxUpStep} for players.
	 * <p>
	 * This ensures that the attribute value accurately reflects the player's Step Height, instead of requiring additional correction.
	 * @author Shadows
	 * @reason Ensuring that, for players, the value of the Step Height attribute matches the true step height value.
	 */
	@Overwrite
	@SuppressWarnings("deprecation")
	default float getStepHeight() {
		float legacyStep = ((Entity) (Object) this).maxUpStep;
		if (this instanceof Player player) return (float) player.getAttributeValue(ForgeMod.STEP_HEIGHT_ADDITION.get());

		if (this instanceof LivingEntity living) {
			AttributeInstance stepHeightAttribute = living.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
			if (stepHeightAttribute != null) {
				return (float) Math.max(0, legacyStep + stepHeightAttribute.getValue());
			}
		}
		return legacyStep;
	}

}
