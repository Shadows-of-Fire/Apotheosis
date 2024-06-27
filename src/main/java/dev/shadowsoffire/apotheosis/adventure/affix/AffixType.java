package dev.shadowsoffire.apotheosis.adventure.affix;

/**
 * TODO: Replace this with some kind of tag-like system that permits for the creation of expressive loot rules.
 */
public enum AffixType {
    STAT,
    POTION,
    ABILITY,
    ANCIENT,
    SOCKET,
    DURABILITY;

    public boolean needsValidation() {
        return this == STAT || this == POTION || this == ABILITY;
    }
}
