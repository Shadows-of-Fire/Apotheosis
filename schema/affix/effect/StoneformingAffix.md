# Description
A Stoneforming Affix is an [Affix](../Affix.md) which converts mined blocks into a block of the user's choosing.  

The affix holds a set of candidate blocks. Shift-right-clicking a candidate block selects it as the target. From then on, whenever a candidate block is mined, its drops are converted into the target block.  

If a block drops a different item than itself (such as stone dropping cobblestone), both blocks must be in the candidate set for the conversion to apply.  

This affix has no values, may roll at any rarity, and its strength does not change with the affix level.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:stoneforming",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. An empty list matches nothing.
        LootCategory
    ],
    "candidates": HolderSet         // [Mandatory] || The set of interchangeable blocks. Accepts a list of block registry names, or a single #-prefixed tag name.
}
```

# Examples
The Stoneforming affix, which converts between the blocks in the `#apotheosis:stoneforming_candidates` tag, and is exclusive with the Sandforming affix.

```json
{
    "type": "apotheosis:stoneforming",
    "candidates": "#apotheosis:stoneforming_candidates",
    "categories": [
        "apotheosis:breaker"
    ],
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:breaker/ability/sandforming"
        ],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    }
}
```
