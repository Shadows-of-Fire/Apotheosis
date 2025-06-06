# Description
AffixData defines whether and how boss equipment receives special affixes (magical properties). When affixes are enabled, one of the boss's equipped items will become an affix item with the specified rarity.

# Dependencies
This object references the following objects:
1. [LootRarity](../loot/LootRarity.md)

# Schema
```js
{
    "affix_chance": float,              // [Mandatory] || The chance (0-1) that one of the boss's items will become an affix item.
    "rarities": [                       // [Optional]  || A pool of rarities that can be used; if empty, all rarities will be considered.
        LootRarity
    ]
}
```

# Examples

## Basic Affix Data
A simple configuration with a 50% chance to create an affix item, with no restrictions on rarity.

```json
{
    "affix_chance": 0.5
}
```

## Advanced Affix Data with Specific Rarities
A configuration that always creates an affix item, but only with rare or higher rarity.

```json
{
    "affix_chance": 1.0,
    "rarities": [
        "rare",
        "epic",
        "mythic"
    ]
}
```
