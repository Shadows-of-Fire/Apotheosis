package shadows.apotheosis.core.attributeslib.util;

import net.minecraft.world.damagesource.DamageSource;

public class AttributesUtil {

	public static boolean isPhysicalDamage(DamageSource src) {
		return !src.isMagic() && !src.isFire() && !src.isExplosion();
	}

}
