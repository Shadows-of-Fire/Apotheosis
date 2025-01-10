package dev.shadowsoffire.apotheosis.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * TODO: Find a better scheme for this.
 * Since many damage sources are singletons, this may need a copy-on-write setup.
 * For now, I only use it for entity-based damage sources, which are not singletons, but this is a potential issue...
 */
public interface DamageSourceExtension {

    /**
     * Adds an additional tag to this damage source.
     * <p>
     * This damage source will be treated as if it had the given tag when applying tag-based checks.
     */
    public void addTag(TagKey<DamageType> tag);

    /**
     * Removes an additional damage tag from this damage source.
     * <p>
     * This function does not allow removing tags that are intrinsic to the damage type, only removing added ones.
     */
    public void removeTag(TagKey<DamageType> tag);

}
