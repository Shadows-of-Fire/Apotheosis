# Description
PsychicAffix allows shields to reflect damage back to projectile attackers. When blocking an arrow or other projectile with a shield having this affix, a percentage of the blocked damage is reflected back to the shooter.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:psychic",        // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
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
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "weight": 6,
            "quality": 1.1
        }
    },
    "values": {
        "rare": {
            "min": 0.15,
            "max": 0.25
        },
        "epic": {
            "min": 0.35,
            "max": 0.45
        },
        "mythic": {
            "min": 0.60,
            "max": 0.75
        }
    }
}
```
