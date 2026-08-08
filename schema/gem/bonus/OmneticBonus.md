# Description
An Omnetic Bonus is a [Gem Bonus](./GemBonus.md) which makes the socketed tool function as an entire toolset.  

The tool can harvest any block that one of the configured items could harvest, and its mining speed becomes the fastest speed among the configured items and the tool itself.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)
3. [ItemStack](../../../../../../Placebo/blob/1.21/schema/ItemStack.md)

# Schema
```js
{
    "type": "apotheosis:omnetic",
    "gem_class": GemClass,      // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {                 // [Mandatory] || The emulated toolset for each supported purity.
        Purity: {
            "name": "string",   // [Mandatory] || A name for the toolset, shown in the tooltip via the lang key "misc.apotheosis.<name>".
            "items": [          // [Mandatory] || The items whose harvest abilities and mining speeds are emulated.
                ItemStack
            ]
        }
    }
}
```

# Examples
An omnetic bonus for tools which emulates the diamond toolset. Higher purities typically upgrade to stronger toolsets.

```json
{
    "type": "apotheosis:omnetic",
    "gem_class": {
        "key": "tools",
        "types": [
            "apotheosis:breaker",
            "apotheosis:shears"
        ]
    },
    "values": {
        "flawed": {
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
