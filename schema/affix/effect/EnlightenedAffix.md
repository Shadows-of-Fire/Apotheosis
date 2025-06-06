# Description
EnlightenedAffix allows tools to place torches without consuming them from the player's inventory, at the cost of durability. This affix can only be applied to items in breaker categories (tools that can break blocks).

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)
4. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:enlightened",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,         // [Mandatory] || The affix definition
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
        "common": {
            "type": "placebo:step",
            "steps": [5, 4, 3, 2],
            "y_interp": "nearest"
        },
        "uncommon": {
            "type": "placebo:step",
            "steps": [3, 2, 1],
            "y_interp": "nearest"
        },
        "rare": {
            "type": "placebo:step",
            "steps": [2, 1],
            "y_interp": "nearest"
        },
        "epic": {
            "type": "placebo:step",
            "steps": [1],
            "y_interp": "nearest"
        }
    }
}
```
