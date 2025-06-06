# Description
PsychicAffix allows shields to reflect damage back to projectile attackers. When blocking an arrow or other projectile with a shield having this affix, a percentage of the blocked damage is reflected back to the shooter.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)
4. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:psychic",        // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,        // [Mandatory] || The affix definition
    "values": {                          // [Mandatory] || Per-rarity damage reflection percentages
        LootRarity: StepFunction
    }
}
```

# Examples

## Psychic Shield
An affix that reflects damage back to projectile attackers when blocking with a shield.

```json
{
    "type": "apotheosis:psychic",
    "definition": {
        "affix_type": "shield_effect",
        "exclusive_set": [],
        "weights": {
            "weight": 6,
            "quality": 1.1
        }
    },
    "values": {
        "rare": {
            "type": "placebo:step",
            "steps": [0.25, 0.35],
            "y_interp": "linear"
        },
        "epic": {
            "type": "placebo:step",
            "steps": [0.40, 0.50],
            "y_interp": "linear"
        },
        "mythic": {
            "type": "placebo:step",
            "steps": [0.60, 0.75],
            "y_interp": "linear"
        }
    }
}
```
