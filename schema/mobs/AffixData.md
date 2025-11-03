# Description
AffixData defines how entity equipment is affixed. Currently this is only used by Elites to determine if their equipment is affixed.

# Dependencies
This object references the following objects:
1. [LootRarity](../loot/LootRarity.md)

# Schema
```js
{
    "affix_chance": float,              // [Mandatory] || The chance (0-1) that one of the entity's items will be affixed.
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
A configuration that always creates an affix item, but only with rare/epic/mythic rarity. Weights are based on current world tier. One is selected at random if all weights are zero.

```json
{
    "affix_chance": 1.0,
    "rarities": [
        "apotheosis:rare",
        "apotheosis:epic",
        "apotheosis:mythic"
    ]
}
```
