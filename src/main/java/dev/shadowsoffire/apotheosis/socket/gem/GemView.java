package dev.shadowsoffire.apotheosis.socket.gem;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;

/**
 * Superclass of both {@link GemInstance} and {@link UnsocketedGem}, used to pass either class to a single method.
 */
public interface GemView {

    /**
     * Returns the gem specified by {@link Components#GEM} on the {@link #gemStack()}.
     */
    DynamicHolder<Gem> gem();

    /**
     * Returns the purity specified by {@link Components#PURITY} on the {@link #gemStack()}.
     * <p>
     * If the gem {@linkplain DynamicHolder#isBound() is bound}, and the purity would be invalid, it will be clamped to an in-range value.
     */
    Purity purity();
}
