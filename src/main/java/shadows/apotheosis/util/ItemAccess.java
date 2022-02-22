package shadows.apotheosis.util;

import java.util.UUID;

import net.minecraft.world.item.Item;

public class ItemAccess extends Item {

	public ItemAccess(Properties pProperties) {
		super(pProperties);
		// TODO Auto-generated constructor stub
	}

	public static UUID getBaseAD() {
		return Item.BASE_ATTACK_DAMAGE_UUID;
	}

	public static UUID getBaseAS() {
		return Item.BASE_ATTACK_SPEED_UUID;
	}

}
