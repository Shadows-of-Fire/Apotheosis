package dev.shadowsoffire.apotheosis.adventure.compat;

import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.world.entity.player.Player;

public class GameStagesCompat {

    /**
     * Checks if the player has any of the stages listed in the second param.
     *
     * @param player The player in question.
     * @param stages The stages of the item being checked. Null means "all stages", empty means "never available".
     * @return If the player has any of the passed stages.
     */
    public static boolean hasStage(Player player, Set<String> stages) {
        return !AdventureModule.STAGES_LOADED || stages == null || Inner.hasStage(player, stages);
    }

    /**
     * An item that is restricted by a particular game stage (or other string).
     */
    public static interface IStaged {

        /**
         * Null means "all stages", empty means "never available".
         *
         * @return A set of all the game stages this item is available in.
         */
        @Nullable
        Set<String> getStages();

        public static <T extends IStaged> Predicate<T> matches(Player player) {
            return obj -> hasStage(player, obj.getStages());
        }

    }

    private static class Inner {

        private static boolean hasStage(Player player, Set<String> stages) {
            return GameStageHelper.hasAnyOf(player, stages);
        }

    }

}
