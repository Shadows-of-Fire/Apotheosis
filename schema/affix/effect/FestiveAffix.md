# Description
A Festive Affix is an [Affix](../Affix.md) which grants a chance for slain enemies to explode into a shower of bonus drops, like a loot pinata.  

When the user kills a mob and the chance roll passes, an explosion effect is played, and every dropped item is duplicated the given number of extra times.  

Equipment worn or held by the mob is never duplicated, and mobs with the `apoth.no_pinata` flag in their persistent NBT data are excluded entirely.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)
4. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:festive",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. An empty list matches nothing.
        LootCategory
    ],
    "values": {                     // [Mandatory] || The pinata data for each supported rarity.
        LootRarity: {
            "chance": StepFunction, // [Mandatory] || The chance that a kill triggers the pinata. Range: [0, 1].
            "rolls": integer        // [Mandatory] || The number of extra copies of each dropped item.
        }
    }
}
```

# Examples
The Festive affix, with up to a 6% chance to drop 20 extra copies of each item.

```json
{
    "type": "apotheosis:festive",
    "categories": [
        "apotheosis:melee_weapon",
        "apotheosis:trident"
    ],
    "definition": {
        "affix_type": "basic_effect",
        "exclusive_set": [],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "values": {
        "apotheosis:epic": {
            "chance": {
                "min": 0.02,
                "max": 0.05,
                "step": 0.005
            },
            "rolls": 20
        },
        "apotheosis:mythic": {
            "chance": {
                "min": 0.03,
                "max": 0.06,
                "step": 0.005
            },
            "rolls": 20
        }
    }
}
```
