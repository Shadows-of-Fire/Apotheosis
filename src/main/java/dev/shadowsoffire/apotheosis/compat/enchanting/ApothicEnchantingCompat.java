package dev.shadowsoffire.apotheosis.compat.enchanting;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

/**
 * Compat handler for Apothic Enchanting. All classes that reference Apothic Enchanting types live in this package,
 * and may only be classloaded when {@link #isLoaded()} is true. This class itself is always safe to load.
 */
public class ApothicEnchantingCompat {

    public static final String MODID = "apothic_enchanting";

    private static final boolean LOADED = ModList.get().isLoaded(MODID);

    public static final DeferredHelper R = DeferredHelper.create(Apotheosis.MODID);

    public static boolean isLoaded() {
        return LOADED;
    }

    /**
     * Called from the mod constructor when Apothic Enchanting is installed.
     */
    public static void register(IEventBus bus) {
        R.recipeSerializer("potion_charm_infusion", () -> CharmInfusionRecipe.Serializer.INSTANCE);
        bus.register(R);
    }

    /**
     * Resolves the real max level of an enchantment, which Apothic Enchanting can raise above the vanilla value.
     */
    public static int getMaxLevel(Enchantment ench) {
        return LOADED ? Inner.getMaxLevel(ench) : ench.getMaxLevel();
    }

    private static class Inner {

        static int getMaxLevel(Enchantment ench) {
            return EnchHooks.getMaxLevel(ench);
        }

    }

}
