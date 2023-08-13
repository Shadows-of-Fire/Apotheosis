package shadows.apotheosis.util;

import java.util.Comparator;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * Misc Comparator Utils
 */
public class Comparators {

    @SafeVarargs
    public static <T> Comparator<T> chained(Comparator<T>... comparators) {
        Comparator<T> c = comparators[0];
        for (int i = 1; i < comparators.length; i++) {
            c = c.thenComparing(comparators[i]);
        }
        return c;
    }

    // Note: Will NPE on unregistered objects.
    public static <T> Comparator<T> idComparator(Registry<T> reg) {
        return Comparator.comparing(reg::getKey, ResourceLocation::compareTo);
    }

}
