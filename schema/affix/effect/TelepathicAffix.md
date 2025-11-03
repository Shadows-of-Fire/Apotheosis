# Description
The TelepathicAffix is a utility affix that teleports item drops directly to the player. While using this affix, breaking blocks or killing entities will teleport the drops directly to the player's position.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:telepathic",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "rarities": [                        // [Mandatory] || List of rarities this affix can have
        LootRarity
    ]
}
```

# Examples

## Basic Telepathic Affix
A basic telepathic affix that can appear on rare or higher rarity items.

```json
{
    "type": "apotheosis:telepathic",
    "definition": {
        "affix_type": "basic_effect",
        "exclusive_set": [],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "rarities": [
        "apotheosis:rare",
        "apotheosis:epic",
        "apotheosis:mythic"
    ]
}
