# Description
StoneformingAffix allows tools to convert between different types of blocks when mining. When you mine a block with this affix, it will drop a different block from the candidate set. You can shift-right-click with the tool on a candidate block to set that as the target conversion block.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootCategory](../../loot/LootCategory.md)
4. [Block](../../../../../Minecraft/blob/-/schema/Block.md)

# Schema
```js
{
    "type": "apotheosis:stoneforming",   // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,        // [Mandatory] || The affix definition
    "categories": [                       // [Mandatory] || The item categories this affix can be applied to
        LootCategory
    ],
    "candidates": [                       // [Mandatory] || The list of blocks that can be converted between
        Block
    ]
}
```

# Examples

## Stone Conversion Tool
An affix that allows converting between stone, cobblestone, and other stone variants.

```json
{
    "type": "apotheosis:stoneforming",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "weight": 6,
            "quality": 1.0
        }
    },
    "categories": [
        "pickaxe"
    ],
    "candidates": [
        "minecraft:stone",
        "minecraft:cobblestone",
        "minecraft:stone_bricks",
        "minecraft:cracked_stone_bricks",
        "minecraft:mossy_stone_bricks",
        "minecraft:chiseled_stone_bricks"
    ]
}
```

## Wood Conversion Tool
An affix that allows converting between different wood types.

```json
{
    "type": "apotheosis:stoneforming",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "weight": 5,
            "quality": 1.0
        }
    },
    "categories": [
        "axe"
    ],
    "candidates": [
        "minecraft:oak_planks",
        "minecraft:spruce_planks",
        "minecraft:birch_planks",
        "minecraft:jungle_planks",
        "minecraft:acacia_planks",
        "minecraft:dark_oak_planks",
        "minecraft:mangrove_planks",
        "minecraft:cherry_planks",
        "minecraft:bamboo_planks"
    ]
}
```
