# Description
A Thunderstruck Affix is an [Affix](../Affix.md) which makes melee attacks chain damage to all nearby enemies.  

When the user lands a fully-charged attack, every valid entity near the target takes the given amount of lightning damage, which bypasses armor. Unlike the Cleaving affix, there is no chance roll and no target cap, but the chained damage is a flat amount rather than a full attack.  

Chained damage will not hit animals or villagers unless the original target was one.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)
4. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:thunderstruck",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. An empty list matches nothing.
        LootCategory
    ],
    "values": {                     // [Mandatory] || The flat damage dealt to each nearby entity, for each supported rarity.
        LootRarity: StepFunction
    }
}
```

# Examples
The Thunderstruck affix, dealing up to 8 chained damage.

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
```
