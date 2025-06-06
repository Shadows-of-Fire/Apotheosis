# Description
FestiveAffix is also known as "Loot Pinata". When a mob is killed with a weapon that has this affix, there is a chance that its drops will multiply significantly and explode outward in a festive manner, accompanied by an explosion sound and particles.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)
4. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:festive",        // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,        // [Mandatory] || The affix definition
    "values": {                          // [Mandatory] || Per-rarity drop multiplication chance
        LootRarity: StepFunction
    }
}
```

# Examples

## Festive Weapon
An affix that gives a chance to multiply mob drops when killing enemies.

```json
{
    "type": "apotheosis:festive",
    "definition": {
        "affix_type": "special",
        "exclusive_set": [],
        "weights": {
            "weight": 3,
            "quality": 1.5
        }
    },
    "values": {
        "rare": {
            "type": "placebo:step",
            "steps": [0.05, 0.10],
            "y_interp": "linear"
        },
        "epic": {
            "type": "placebo:step",
            "steps": [0.10, 0.15],
            "y_interp": "linear"
        },
        "mythic": {
            "type": "placebo:step",
            "steps": [0.15, 0.20],
            "y_interp": "linear"
        },
        "ancient": {
            "type": "placebo:step",
            "steps": [0.20, 0.25],
            "y_interp": "linear"
        }
    }
}
```
