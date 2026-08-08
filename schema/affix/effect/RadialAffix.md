# Description
A Radial Affix is an [Affix](../Affix.md) which makes the tool mine an area of blocks at once.  

When the user breaks a block, an X-by-Y plane of blocks around the broken position is also broken. Radial mining can be toggled by the player, and never activates while sneaking.  

Unlike most affixes, the values for each rarity are a list. The affix level selects an entry from the list, with higher levels selecting later (larger) entries.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:radial",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. An empty list matches nothing.
        LootCategory
    ],
    "values": {                     // [Mandatory] || The list of mining areas for each supported rarity, ordered from weakest to strongest.
        LootRarity: [
            {
                "x": integer,       // [Mandatory] || The width of the mined area.
                "y": integer,       // [Mandatory] || The height of the mined area.
                "xOff": integer,    // [Mandatory] || The horizontal offset of the mined area.
                "yOff": integer     // [Mandatory] || The vertical offset of the mined area.
            }
        ]
    }
}
```

# Examples
The Supermassive affix, which mines a 7x7 area, only appears in the pinnacle tier, and is exclusive with the standard Radial affix.

```json
{
    "type": "apotheosis:radial",
    "categories": [
        "apotheosis:breaker"
    ],
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:breaker/effect/radial"
        ],
        "weights": {
            "pinnacle": {
                "quality": 0.75,
                "weight": 20
            }
        }
    },
    "values": {
        "apotheosis:mythic": [
            {
                "x": 7,
                "xOff": 0,
                "y": 7,
                "yOff": 0
            }
        ]
    }
}
```
