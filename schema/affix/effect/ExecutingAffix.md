# Description
The ExecutingAffix is a powerful combat affix that can instantly kill enemies below a certain health threshold. When a player attacks with a weapon that has this affix, there's a chance to execute enemies that have health below the configured percentage threshold.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)
4. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

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

## Advanced Executing Affix
A more powerful executing affix with a higher threshold that only appears at high tiers.

```json
{
    "type": "apotheosis:executing",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:cleaving",
            "apotheosis:berserking"
        ],
        "weights": {
            "summit": {
                "weight": 3,
                "quality": 1.2
            },
            "pinnacle": {
                "weight": 5,
                "quality": 1.5
            }
        }
    },
    "values": {
        "epic": {
            "min": 0.30,
            "max": 0.35
        },
        "mythic": {
            "min": 0.35,
            "max": 0.40
        }
    }
}
```
