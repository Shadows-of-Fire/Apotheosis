package dev.shadowsoffire.apotheosis.adventure.affix;

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
