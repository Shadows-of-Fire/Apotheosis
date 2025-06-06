# Description
MagicalArrowAffix makes arrows fired from ranged weapons count as magical damage that bypasses armor. This allows arrows to damage entities that are normally immune to non-magical damage or have high armor protection.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:magical_arrow",  // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,        // [Mandatory] || The affix definition
    "rarities": [                         // [Mandatory] || The list of rarities this affix can spawn with
        LootRarity
    ]
}
```

# Examples

## Magical Arrow Bow
An affix that makes arrows bypass armor and deal magical damage.

```json
{
    "type": "apotheosis:magical_arrow",
    "definition": {
        "affix_type": "special",
        "exclusive_set": [],
        "weights": {
            "weight": 5,
            "quality": 1.2
        }
    },
    "rarities": [
        "rare",
        "epic",
        "mythic",
        "ancient"
    ]
}
```
