package dev.shadowsoffire.apotheosis.util;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public interface AttributeInstanceExt {

    /**
     * Returns a counter that increments every time the base value or modifier set of this instance changes.
     */
    int apoth$getModificationCount();

    static AttributeInstanceExt of(AttributeInstance inst) {
        return (AttributeInstanceExt) inst;
    }

}
