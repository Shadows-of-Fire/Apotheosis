# Description
A Spectral Shot Affix is an [Affix](../Affix.md) which grants a chance to fire a bonus spectral arrow alongside the original projectile.  

The spectral arrow copies the original arrow's motion, critical state, and base damage, and can never be picked up.  

This affix may only roll on ranged weapons, and does not declare a category field.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/1.21/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:spectral",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "values": {                     // [Mandatory] || The chance to fire a bonus spectral arrow, for each supported rarity. Range: [0, 1].
        LootRarity: StepFunction
    }
}
```

# Examples
The Spectral affix, with up to a 70% chance to fire a bonus arrow.

```json
{
    "type": "apotheosis:spectral",
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
            "min": 0.2,
            "max": 0.5
        },
        "apotheosis:mythic": {
            "min": 0.3,
            "max": 0.7
        }
    }
}
```
