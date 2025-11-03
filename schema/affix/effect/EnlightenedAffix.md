# Description
EnlightenedAffix allows tools to place torches without consuming them from the player's inventory, at the cost of durability. This affix can only be applied to items in breaker categories (tools that can break blocks).

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:enlightened",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,        // [Mandatory] || The affix definition
    "values": {                           // [Mandatory] || Per-rarity durability cost values
        LootRarity: StepFunction
    }
}
```

# Examples

## Enlightened Tool
An affix that allows placing torches at the cost of tool durability.

```json
{
    "type": "apotheosis:enlightened",
    "definition": {
        "affix_type": "utility",
        "exclusive_set": [],
        "weights": {
            "weight": 8,
            "quality": 0.8
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
        },
        "apotheosis:mythic": {
            "min": 5.0,
            "max": 0.0,
            "step": -1.0
        }
    }
}
```
