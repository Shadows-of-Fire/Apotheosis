# Description
The CleavingAffix is a powerful ability affix that allows melee weapons to hit multiple targets in a single swing. When a player with a weapon that has this affix attacks a target, there is a chance to also damage additional enemies nearby.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)
4. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:cleaving",       // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "values": {                          // [Mandatory] || Per-rarity cleave configuration
        LootRarity: {
            "chance": StepFunction,      // The chance to trigger the cleave effect
            "targets": StepFunction      // The number of additional targets to hit
        }
    }
}
```

# Examples

## Basic Cleaving Affix
A basic cleaving affix that has a chance to hit additional enemies.

```json
{
    "type": "apotheosis:cleaving",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:executing"
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
            "chance": {
                "min": 0.2,
                "max": 0.3
            },
            "targets": {
                "min": 1,
                "max": 1
            }
        },
        "epic": {
            "chance": {
                "min": 0.3,
                "max": 0.4
            },
            "targets": {
                "min": 1,
                "max": 2
            }
        },
        "mythic": {
            "chance": {
                "min": 0.4,
                "max": 0.5
            },
            "targets": {
                "min": 2,
                "max": 3
            }
        }
    }
}
```

## Advanced Cleaving Affix
A more powerful cleaving affix with higher chances and more targets.

```json
{
    "type": "apotheosis:cleaving",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:executing",
            "apotheosis:berserking"
        ],
        "weights": {
            "pinnacle": {
                "weight": 5,
                "quality": 1.5
            }
        }
    },
    "values": {
        "mythic": {
            "chance": {
                "min": 0.6,
                "max": 0.8
            },
            "targets": {
                "min": 3,
                "max": 5
            }
        }
    }
}
```
