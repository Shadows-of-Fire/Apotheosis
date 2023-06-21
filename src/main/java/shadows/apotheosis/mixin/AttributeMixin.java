package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import shadows.apotheosis.core.attributeslib.api.IFormattableAttribute;

@Mixin(Attribute.class)
public class AttributeMixin implements IFormattableAttribute {

}
