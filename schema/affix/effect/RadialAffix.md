# Description
RadialAffix allows tools to mine blocks in an area around the target block. When breaking a block with a tool that has this affix, it will also break surrounding blocks in a specified pattern.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootCategory](../../loot/LootCategory.md)
4. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:radial",        // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "categories": [                      // [Mandatory] || The item categories this affix can be applied to
        LootCategory
    ],
    "values": {                         // [Mandatory] || Per-rarity radial mining data
        LootRarity: [
            {
                "x": integer,           // [Mandatory] || The width of the mining area
                "y": integer,           // [Mandatory] || The height of the mining area
                "x_offset": integer,    // [Optional]  || The X offset of the mining area. Default: 0
                "y_offset": integer     // [Optional]  || The Y offset of the mining area. Default: 0
            }
        ]
    }
}
```

# Examples

## Radial Mining Pick
An affix that allows mining in a 3x3 area.

```json
{
    "type": "apotheosis:radial",
    "definition": {
        "affix_type": "mining",
        "exclusive_set": [],
        "weights": {
            "weight": 5,
            "quality": 1.0
        }
    },
    "categories": [
        "pickaxe",
        "shovel",
        "axe",
        "hoe"
    ],
    "values": {
        "rare": [
            {
                "x": 1,
                "y": 1
            }
        ],
        "epic": [
            {
                "x": 2,
                "y": 1
            },
            {
                "x": 1,
                "y": 2
            }
        ],
        "mythic": [
            {
                "x": 3,
                "y": 2
            },
            {
                "x": 3,
                "y": 3
            }
        ]
    }
}
```

## Tunneling Pick
An affix that mines blocks in a tunnel pattern.

```json
{
    "type": "apotheosis:radial",
    "definition": {
        "affix_type": "mining",
        "exclusive_set": [],
        "weights": {
            "weight": 3,
            "quality": 1.2
        }
    },
    "categories": [
        "pickaxe"
    ],
    "values": {
        "epic": [
            {
                "x": 1,
                "y": 2,
                "x_offset": 0,
                "y_offset": -1
            }
        ],
        "mythic": [
            {
                "x": 2,
                "y": 3,
                "x_offset": 0,
                "y_offset": -1
            }
        ]
    }
}
```
