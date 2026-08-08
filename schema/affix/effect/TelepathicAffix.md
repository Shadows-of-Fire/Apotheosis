# Description
A Telepathic Affix is an [Affix](../Affix.md) which teleports drops directly to the player.  

Block drops are teleported to the breaker, and mob drops are teleported to the killer, including kills made with arrows.  

This affix has no values, and its strength does not change with the affix level.  

This affix may only roll on ranged weapons, melee weapons, and breakers, and does not declare a category field. Instead, it declares the set of rarities it may roll at.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:telepathic",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "rarities": [                   // [Mandatory] || The set of rarities this affix may roll at.
        LootRarity
    ]
}
```

# Examples
The Telepathic affix, available at rare and higher rarities.

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
```
