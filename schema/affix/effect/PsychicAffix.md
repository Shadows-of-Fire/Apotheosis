# Description
A Psychic Affix is an [Affix](../Affix.md) which reflects blocked projectile damage back at the shooter.  

When the user blocks a projectile with the shield, the projectile's owner takes the given fraction of the blocked damage. The blocked damage itself is not reduced. The reflected fraction may exceed 1.  

This affix may only roll on shields, and does not declare a category field.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:psychic",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "values": {                     // [Mandatory] || The fraction of blocked damage reflected back at the shooter, for each supported rarity.
        LootRarity: StepFunction
    }
}
```

# Examples
The Psychic affix, which reflects up to 120% of the blocked damage.

```json
{
    "type": "apotheosis:psychic",
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
            "min": 0.5,
            "max": 0.9
        },
        "apotheosis:mythic": {
            "min": 0.6,
            "max": 1.2
        }
    }
}
```
