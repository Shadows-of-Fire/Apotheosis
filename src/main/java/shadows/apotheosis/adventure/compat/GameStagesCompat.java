package shadows.apotheosis.adventure.compat;

import java.util.Set;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.world.entity.player.Player;
import shadows.apotheosis.adventure.AdventureModule;

public class GameStagesCompat {

	/**
	 * Checks if the player has any of the stages listed in the second param.
	 * If the second param is null, this always returns true.
	 */
	public static boolean hasStage(Player player, Set<String> stages) {
		return !AdventureModule.stages || stages == null || Inner.hasStage(player, stages);
	}

	private static class Inner {

		private static boolean hasStage(Player player, Set<String> stages) {
			return GameStageHelper.hasAnyOf(player, stages);
		}

	}

}
