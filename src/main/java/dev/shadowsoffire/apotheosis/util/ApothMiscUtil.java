package dev.shadowsoffire.apotheosis.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class ApothMiscUtil {

    /**
     * Gets the experience cost when enchanting at a particular slot. This computes the true xp cost as if you had exactly as many levels as the level cost.
     * <p>
     * For a slot S and level L, the costs are the following:<br>
     * S == 0 -> cost = XP(L)<br>
     * S == 1 -> cost = XP(L) + XP(L-1)<br>
     * S == 2 -> cost = XP(L) + XP(L-1) + XP(L-2)
     * <p>
     * And so on and so forth, if there were ever to be more than three slots.
     *
     * @param level The level of the slot
     * @param slot  The slot index
     * @return The cost, in experience points, of buying the enchantment in a particular slot.
     */
    public static int getExpCostForSlot(int level, int slot) {
        int cost = 0;
        for (int i = 0; i <= slot; i++) {
            cost += EnchantmentUtils.getExperienceForLevel(level - i);
        }
        return cost - 1; // Eating exactly the amount will put you one point below the level, so offset by one here.
    }

    /**
     * Since {@link GradientColor} goes 1:1 through the entire array, if we have a unidirectional gradient, we need to make it wrap around.
     * <p>
     * This is done by making a reversed copy and concatenating them together.
     *
     * @param data The original unidirectional gradient data.
     * @return A cyclical gradient.
     */
    public static int[] doubleUpGradient(int[] data) {
        int[] out = new int[data.length * 2];
        System.arraycopy(data, 0, out, 0, data.length);
        for (int i = data.length - 1; i >= 0; i--) {
            out[data.length * 2 - 1 - i] = data[i];
        }
        return out;
    }

    @Nullable
    public static Player getClientPlayer() {
        return FMLEnvironment.dist.isClient() ? ClientInternal.getClientPlayer() : null;
    }

    /**
     * A reduction that computes the diminishing return value of multiple durability bonuses.<br>
     * For this computation, the first bonus is applied in full, but further bonuses are only applied to the reduced value.
     *
     * @param result  The current result value.
     * @param element The next element.
     * @return The updated result, after applying the element.
     */
    public static double duraProd(double result, double element) {
        return result + (1 - result) * element;
    }

    @SafeVarargs
    public static <T> Set<T> linkedSet(T... objects) {
        var set = new LinkedHashSet<T>();
        for (T t : objects) {
            set.add(t);
        }
        return set;
    }

    /**
     * Checks if a player has the target advancement, using the appropriate sided path.
     * <p>
     * Returns false if the advancement is not loaded.
     */
    public static boolean hasAdvancement(Player player, ResourceLocation key) {
        if (player.level().isClientSide) {
            return ClientInternal.hasAdvancment(key);
        }

        PlayerAdvancements advancements = ((ServerPlayer) player).getAdvancements();
        ServerAdvancementManager manager = player.getServer().getAdvancements();

        AdvancementHolder holder = manager.get(key);
        if (holder != null) {
            AdvancementProgress progress = advancements.progress.get(holder);
            return progress != null && progress.isDone();
        }

        return false;
    }

    /**
     * Creates a standalone holder that can be serialized in datagen by stealing the {@link UniversalOwner} from the registry lookup.
     */
    public static <T> Holder.Reference<T> standaloneHolder(HolderLookup.Provider registries, ResourceKey<T> key) {
        HolderOwner<T> owner = registries.createSerializationContext(JsonOps.INSTANCE).owner(key.registryKey()).get();
        return Holder.Reference.createStandAlone(owner, key);
    }

    public static MutableComponent dotPrefix(Component comp) {
        return Apotheosis.lang("text", "dot_prefix", comp);
    }

    public static MutableComponent starPrefix(Component comp) {
        return Apotheosis.lang("text", "star_prefix", comp);
    }

    /**
     * Returns a random element from the set, using the provided random source.
     */
    public static <T> T getRandomElement(Collection<T> set, RandomSource rand) {
        int index = rand.nextInt(set.size());
        Iterator<T> iter = set.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }

    public static class ClientInternal {

        public static Player getClientPlayer() {
            return Minecraft.getInstance().player;
        }

        public static boolean hasAdvancment(ResourceLocation key) {
            ClientAdvancements advancements = Minecraft.getInstance().getConnection().getAdvancements();
            AdvancementHolder holder = advancements.get(key);
            if (holder != null) {
                AdvancementProgress progress = advancements.progress.get(holder);
                return progress != null && progress.isDone();
            }
            return false;
        }

        /**
         * Alright, so... Key Mappings have this issue where if the main key is also a modifier key (shift/ctrl/alt), then the key is never considered "down" due to how
         * {@link KeyModifier#NONE} works.
         * To properly validate if the key is down, we need to first check if the key is a modifier key, and if so, we need to skip the modifier check (but still do the
         * conflict context check).
         * 
         * @param mapping
         * @return
         */
        public static boolean isKeyReallyDown(KeyMapping mapping) {
            InputConstants.Key key = mapping.getKey();
            if (key == InputConstants.UNKNOWN) {
                return false;
            }

            IKeyConflictContext context = mapping.getKeyConflictContext();
            if (!context.isActive()) {
                return false;
            }

            if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getValue())) {
                return false;
            }

            KeyModifier modifier = mapping.getKeyModifier();
            if (modifier == KeyModifier.NONE) {
                // If the key doesn't have a modifier, we need to first check if the is a modifier key, and if so, we need to skip the modifier check.
                return KeyModifier.isKeyCodeModifier(key) || modifier.isActive(context);
            }
            else {
                return modifier.isActive(context);
            }
        }
    }

}
