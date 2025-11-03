# Description
The CatalyzingAffix is a shield-specific affix that grants a Strength effect when blocking explosion damage. The power of the granted effect increases based on the amount of damage blocked, and the duration of the effect depends on the affix level and rarity.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:catalyzing",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "values": {                          // [Mandatory] || Per-rarity duration values
        LootRarity: StepFunction         // Duration of the strength effect in ticks
    }
}
```

# Examples

## Basic Catalyzing Affix
A basic catalyzing affix that grants short-duration strength when blocking explosions.

```json
{
    "type": "apotheosis:catalyzing",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "rare": {
                "weight": 5
            },
            "epic": {
                "weight": 10
            },
            "mythic": {
                "weight": 15
            }
        }
    },
    "values": {
        "rare": {
            "min": 60,
            "max": 100
        },
        "epic": {
            "min": 100,
            "max": 200
        },
        "mythic": {
            "min": 200,
            "max": 300
        }
    }
}
```
