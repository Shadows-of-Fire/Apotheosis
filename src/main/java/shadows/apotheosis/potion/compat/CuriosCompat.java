package shadows.apotheosis.potion.compat;

import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;

public class CuriosCompat {

	public static void sendIMC() {
		InterModComms.sendTo("curios", "register_type", () -> new CurioIMCMessage("charm"));
	}
}
