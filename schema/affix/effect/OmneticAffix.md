# Description
An Omnetic Affix is an [Affix](../Affix.md) which makes the tool function as an entire toolset.  

The tool can harvest any block that one of the configured items could harvest, and its mining speed becomes the fastest speed among the configured items and the tool itself.  

The strength of this affix does not change with the affix level — only the rarity selects which toolset applies.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [ItemStack](../../../../../../Placebo/blob/-/schema/ItemStack.md)
4. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:omnetic",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. An empty list matches nothing.
        LootCategory
    ],
    "values": {                     // [Mandatory] || The emulated toolset for each supported rarity.
        LootRarity: {
            "name": "string",       // [Mandatory] || A name for the toolset, shown in the tooltip via the lang key "misc.apotheosis.<name>".
            "items": [              // [Mandatory] || The items whose harvest abilities and mining speeds are emulated.
                ItemStack
            ]
        }
    }
}
```

# Examples
A trimmed version of the Omnetic affix, which emulates the iron toolset at rare, upgrading to diamond at epic.

```json
{
    "type": "apotheosis:omnetic",
    "categories": [
        "apotheosis:breaker"
    ],
    "definition": {
        "affix_type": "basic_effect",
        "exclusive_set": [],
        "weights": {
            "quality": 5.0,
            "weight": 25
        }
    },
    "values": {
        "apotheosis:rare": {
            "items": [
                {
                    "count": 1,
                    "id": "minecraft:iron_axe"
                },
                {
                    "count": 1,
                    "id": "minecraft:iron_shovel"
                },
                {
                    "count": 1,
                    "id": "minecraft:iron_pickaxe"
                },
                {
                    "count": 1,
                    "id": "minecraft:iron_sword"
                },
                {
                    "count": 1,
                    "id": "minecraft:iron_hoe"
                }
            ],
            "name": "iron"
        },
        "apotheosis:epic": {
            "items": [
                {
                    "count": 1,
                    "id": "minecraft:diamond_axe"
                },
                {
                    "count": 1,
                    "id": "minecraft:diamond_shovel"
                },
                {
                    "count": 1,
                    "id": "minecraft:diamond_pickaxe"
                },
                {
                    "count": 1,
                    "id": "minecraft:diamond_sword"
                },
                {
                    "count": 1,
                    "id": "minecraft:diamond_hoe"
                }
            ],
            "name": "diamond"
        }
    }
}
```
