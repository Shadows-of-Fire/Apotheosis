# Description
The SpectralShotAffix is a ranged weapon affix that provides a chance to fire an additional spectral arrow when shooting.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

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
