# Description
MageSlayerBonus is a defensive gem bonus that provides protection against magical damage. When a player takes magic damage while equipped with an item containing this gem, a percentage of that damage is converted to healing and the player receives reduced damage. This makes it an effective countermeasure against magic-wielding enemies.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. Magic Damage - Damage tagged with the Minecraft "is_magic" damage type tag

# Schema
```js
{
    "type": "apotheosis:mage_slayer_bonus", // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,                 // [Mandatory] || The item types this gem can be applied to
    "values": {                           // [Mandatory] || Per-purity damage conversion values
        Purity: float                     // Value between 0 and 1, representing percentage of magic damage converted to healing and reduced
    }
}
```

# Examples

## Basic Anti-Magic Gem
A gem that provides moderate protection against magic damage.

```json
{
    "type": "apotheosis:mage_slayer_bonus",
    "gem_class": {
        "key": "combat/armor",
        "types": ["helmet", "chestplate", "leggings", "boots"]
    },
    "values": {
        "chipped": 0.1,
        "flawed": 0.15,
        "normal": 0.2,
        "flawless": 0.25,
        "perfect": 0.3
    }
}
```

## Specialized Magic Shield Gem
A high-tier gem specifically for shields providing strong protection against magic.

```json
{
    "type": "apotheosis:mage_slayer_bonus",
    "gem_class": {
        "key": "combat/shield",
        "types": ["shield"]
    },
    "values": {
        "flawless": 0.35,
        "perfect": 0.5
    }
}
```

When a player takes magical damage with this gem equipped, the specified percentage of that damage is both converted to healing and subtracted from the damage taken. For example, with a 30% value, if a player would take 10 magical damage, they would instead take only 7 damage and be healed for 3 health points.
