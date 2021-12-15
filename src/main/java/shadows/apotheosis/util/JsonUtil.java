package shadows.apotheosis.util;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;

public class JsonUtil {

	public static boolean checkAndLogEmpty(JsonElement e, ResourceLocation id, String type, Logger logger) {
		String s = e.toString();
		if (s.isEmpty() || s.equals("{}")) {
			logger.debug("Ignoring {} with id {} as it is empty.", type, id);
			return true;
		}
		return false;
	}
}
