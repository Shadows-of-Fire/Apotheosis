# Description
A Catalyzing Affix is an [Affix](../Affix.md) which grants great power when blocking an explosion.  

When the user blocks explosion damage with the shield, they gain Strength. The duration comes from the step function for the item's rarity, while the amplifier scales with the amount of damage blocked (growing logarithmically).  

This affix may only roll on shields, and does not declare a category field.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:catalyzing",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "values": {                     // [Mandatory] || The duration of the Strength effect (in ticks), for each supported rarity.
        LootRarity: StepFunction
    }
}
```

# Examples
The Catalyzing affix, which grants 10 to 30 seconds of Strength when blocking an explosion.

```json
{
    "type": "apotheosis:catalyzing",
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
            "min": 200.0,
            "max": 400.0,
            "step": 20.0
        },
        "apotheosis:mythic": {
            "min": 300.0,
            "max": 600.0,
            "step": 20.0
        }
    }
}
```
