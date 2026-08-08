# Description
An Enlightened Affix is an [Affix](../Affix.md) which allows the user to place torches by right-clicking with the tool, at the cost of durability.  

No torch item is consumed. The placed item is configurable in the Apotheosis config, and defaults to a normal torch.  

Note that the durability cost is the value produced by the step function, so lower values are better. The shipped data uses negative steps, meaning the cost decreases as the affix level increases.  

This affix may only roll on breakers, and does not declare a category field.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/1.21/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:enlightened",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "values": {                     // [Mandatory] || The durability cost per torch placed, for each supported rarity. The output of the function will be truncated to an integer.
        LootRarity: StepFunction
    }
}
```

# Examples
A trimmed version of the Enlightened affix. At rare, the cost ranges from 12 (at level 0) down to 8 (at level 1).

```json
{
    "type": "apotheosis:enlightened",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "values": {
        "apotheosis:rare": {
            "min": 12.0,
            "max": 8.0,
            "step": -1.0
        },
        "apotheosis:epic": {
            "min": 10.0,
            "max": 5.0,
            "step": -1.0
        }
    }
}
```
