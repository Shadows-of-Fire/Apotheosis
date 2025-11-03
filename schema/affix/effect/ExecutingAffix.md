# Description
This affixes gives a weapon the ability to instantly kill enemies below the threshold percentage.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:executing",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,      // [Mandatory] || The affix definition
    "values": {                         // [Mandatory] || Per-rarity threshold values
        LootRarity: StepFunction        // The health threshold (0-1) below which enemies will be executed
    }
}
```

# Examples

## Basic Executing Affix
A basic executing affix that can instantly kill enemies below a moderate health threshold.

```json
{
    "type": "apotheosis:executing",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:cleaving"
        ],
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
            "min": 0.15,
            "max": 0.20
        },
        "epic": {
            "min": 0.20,
            "max": 0.25
        },
        "mythic": {
            "min": 0.25,
            "max": 0.30
        }
    }
}
```
