# Description
A Retreating Affix is an [Affix](../Affix.md) which launches the user backwards when they block a close-range attack, allowing them to disengage.  

The launch only occurs when the attacker is within three blocks of the user. The blocked damage is not modified.  

This affix has no values, and its strength does not change with the affix level.  

This affix may only roll on shields, and does not declare a category field. Instead, it declares the set of rarities it may roll at.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:retreating",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "rarities": [                   // [Mandatory] || The set of rarities this affix may roll at.
        LootRarity
    ]
}
```

# Examples
The Retreating affix, available at epic and mythic rarities.

```json
{
    "type": "apotheosis:retreating",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "rarities": [
        "apotheosis:epic",
        "apotheosis:mythic"
    ]
}
```
