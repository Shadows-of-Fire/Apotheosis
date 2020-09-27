package shadows.apotheosis.potion.compat;

import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.SlotTypeMessage;

public class CuriosCompat {

	public static void sendIMC() {
		InterModComms.sendTo("curios", "REGISTER_TYPE", () -> new SlotTypeMessage.Builder("charm").size(1).build());
	}
}