# Description
An Executing Affix is an [Affix](../Affix.md) which instantly slays enemies that are below a health threshold.  

When the user lands a fully-charged attack against an entity whose remaining health fraction is below the threshold, the entity is killed outright. Totems of Undying still protect against the execution.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)
4. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:executing",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. An empty list matches nothing.
        LootCategory
    ],
    "values": {                     // [Mandatory] || The health fraction below which targets are executed, for each supported rarity. Range: [0, 1].
        LootRarity: StepFunction
    }
}
```

# Examples
The Executing affix, which executes targets below up to 25% health.

```json
{
    "type": "apotheosis:executing",
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
            "min": 0.1,
            "max": 0.2
        },
        "apotheosis:mythic": {
            "min": 0.15,
            "max": 0.25
        }
    }
}
```
