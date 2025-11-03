# Description
The ThunderstruckAffix is a powerful combat affix that applies lightning damage to enemies near the target when you attack. This damage bypasses armor.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootCategory](../../loot/LootCategory.md)
3. [LootRarity](../../loot/LootRarity.md)
4. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:thunderstruck",  // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "categories": [                      // [Mandatory] || List of item categories this affix can be applied to
        LootCategory
    ],
    "values": {                          // [Mandatory] || Per-rarity damage values
        LootRarity: StepFunction         // The lightning damage dealt to nearby entities
    }
}
```

# Examples

## Basic Thunderstruck Affix
A basic thunderstruck affix that can be applied to melee weapons and deals moderate lightning damage.

```json
{
    "type": "apotheosis:thunderstruck",
    "categories": [
        "apotheosis:melee_weapon",
        "apotheosis:trident"
    ],
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "values": {
        "apotheosis:epic": {
            "min": 3.0,
            "max": 6.0,
            "step": 1.0
        },
        "apotheosis:mythic": {
            "min": 4.0,
            "max": 8.0,
            "step": 1.0
        }
    }
}
