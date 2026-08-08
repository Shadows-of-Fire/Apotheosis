# Description
A Magical Arrow Affix is an [Affix](../Affix.md) which makes arrows fired from the item deal magic damage that bypasses armor.  

This affix has no values, and its strength does not change with the affix level.  

This affix may only roll on ranged weapons, and does not declare a category field. Instead, it declares the set of rarities it may roll at.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:magical",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "rarities": [                   // [Mandatory] || The set of rarities this affix may roll at.
        LootRarity
    ]
}
```

# Examples
The Magical affix, available at epic and mythic rarities.

```json
{
    "type": "apotheosis:magical",
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
