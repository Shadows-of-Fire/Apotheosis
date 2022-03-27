package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import shadows.apotheosis.Apoth;

@Mixin(Player.class)
public class PlayerMixin {

	@Inject(method = "createAttributes", at = @At("RETURN"))
	private static void createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		AttributeSupplier.Builder builder = cir.getReturnValue();
		addIfExists(builder, Apoth.Attributes.COLD_DAMAGE, Apoth.Attributes.CRIT_CHANCE, Apoth.Attributes.CRIT_DAMAGE, Apoth.Attributes.CURRENT_HP_DAMAGE, Apoth.Attributes.DRAW_SPEED, Apoth.Attributes.FIRE_DAMAGE, Apoth.Attributes.LIFE_STEAL, Apoth.Attributes.OVERHEAL, Apoth.Attributes.PIERCING);
	}

	private static void addIfExists(AttributeSupplier.Builder builder, Attribute... attribs) {
		for (Attribute attrib : attribs)
			if (attrib != null) builder.add(attrib);
	}

}
