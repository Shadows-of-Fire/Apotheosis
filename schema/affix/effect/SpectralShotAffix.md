# Description
The SpectralShotAffix is a ranged weapon affix that has a chance to fire additional spectral arrows when shooting. When a player fires a projectile from a bow or crossbow with this affix, there's a chance that a spectral arrow will also be fired alongside the original projectile.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)
4. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:spectral_shot",  // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "values": {                          // [Mandatory] || Per-rarity chance values
        LootRarity: StepFunction         // The chance (0-1) to fire a spectral arrow
    }
}
```

# Examples

## Basic Spectral Shot Affix
A basic spectral shot affix with a moderate chance to fire spectral arrows.

```json
{
    "type": "apotheosis:spectral_shot",
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
            "min": 0.2,
            "max": 0.3
        },
        "epic": {
            "min": 0.3,
            "max": 0.4
        },
        "mythic": {
            "min": 0.4,
            "max": 0.5
        }
    }
}
```

## Advanced Spectral Shot Affix
A more powerful spectral shot affix with a high chance to fire spectral arrows.

```json
{
    "type": "apotheosis:spectral_shot",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:explosive_shot"
        ],
        "weights": {
            "summit": {
                "weight": 5,
                "quality": 1.2
            },
            "pinnacle": {
                "weight": 8,
                "quality": 1.5
            }
        }
    },
    "values": {
        "epic": {
            "min": 0.4,
            "max": 0.6
        },
        "mythic": {
            "min": 0.6,
            "max": 0.8
        }
    }
}
```
