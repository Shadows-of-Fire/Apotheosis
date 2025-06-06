# Description
RetreatingAffix (also known as "Disengage") allows shields to knock the holder backward when blocking an attack from nearby entities. This can help create distance between the shield user and their attacker.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:retreating",    // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "rarities": [                        // [Mandatory] || The list of rarities this affix can spawn with
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
        "affix_type": "shield_effect",
        "exclusive_set": [],
        "weights": {
            "weight": 7,
            "quality": 0.9
        }
    },
    "rarities": [
        "uncommon",
        "rare",
        "epic"
    ]
}
```
