# Description
A Cleaving Affix is an [Affix](../Affix.md) which grants a chance for melee attacks to strike nearby enemies as well.  

When the user lands a fully-charged attack, the cleave chance is rolled. On success, the attack is repeated against up to the given number of entities near the original target. Cleave hits cannot trigger further cleaves, and will not hit animals or villagers unless the original target was one.  

This affix may only roll on melee weapons and tridents, and does not declare a category field.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/1.21/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:cleaving",
    "definition": AffixDefinition,    // [Mandatory] || The definition of this affix.
    "values": {                       // [Mandatory] || The cleave data for each supported rarity.
        LootRarity: {
            "chance": StepFunction,   // [Mandatory] || The chance that a fully-charged attack cleaves. Range: [0, 1].
            "targets": StepFunction   // [Mandatory] || The number of extra entities that can be hit. The output of the function will be truncated to an integer.
        }
    }
}
```

# Examples
The Cleaving affix, with up to a 60% chance to strike up to 6 extra targets.

```json
{
    "type": "apotheosis:cleaving",
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
            "chance": {
                "min": 0.3,
                "max": 0.5,
                "step": 0.05
            },
            "targets": {
                "min": 2.0,
                "max": 5.0,
                "step": 1.0
            }
        },
        "apotheosis:mythic": {
            "chance": {
                "min": 0.4,
                "max": 0.6,
                "step": 0.05
            },
            "targets": {
                "min": 3.0,
                "max": 6.0,
                "step": 1.0
            }
        }
    }
}
```
