# Description
The Retreating Affix allows the user to jump backwards when blocking an attack from nearby enemies. This can help create distance between the shield user and their attacker.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:retreating",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "rarities": [                        // [Mandatory] || The set of rarities this affix can spawn on
        LootRarity
    ]
}
```

# Examples

## Retreating Shield
An affix that pushes the shield user backward when blocking an attack.

```json
{
    "type": "apotheosis:retreating",
    "definition": {
        "affix_type": "basic_effect",
        "exclusive_set": [],
        "weights": {
            "weight": 7,
            "quality": 0.9
        }
    },
    "rarities": [
        "apotheosis:uncommon",
        "apotheosis:rare",
        "apotheosis:epic"
    ]
}
```
