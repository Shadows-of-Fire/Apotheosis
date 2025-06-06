# Description
OmneticAffix allows a tool to harvest blocks and have mining speed as if it were another tool type. For example, a sword with this affix could break blocks as if it were a pickaxe, axe, or shovel.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:omnetic",        // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,        // [Mandatory] || The affix definition
    "values": {                          // [Mandatory] || Per-rarity omnetic data
        LootRarity: {
            "name": string,              // [Mandatory] || The name of the tool type (e.g., "pickaxe", "axe", "shovel")
            "tools": [                   // [Mandatory] || The list of tool item stacks to inherit properties from
                ItemStack
            ]
        }
    }
}
```

# Examples

## Omnetic Sword
An affix that allows a sword to mine blocks like a pickaxe.

```json
{
    "type": "apotheosis:omnetic",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "weight": 5,
            "quality": 1.0
        }
    },
    "values": {
        "rare": {
            "name": "pickaxe",
            "tools": [
                {"id": "minecraft:iron_pickaxe"}
            ]
        },
        "epic": {
            "name": "pickaxe",
            "tools": [
                {"id": "minecraft:diamond_pickaxe"}
            ]
        },
        "mythic": {
            "name": "pickaxe",
            "tools": [
                {"id": "minecraft:netherite_pickaxe"}
            ]
        }
    }
}
```

## Multi-Tool
An affix that allows a tool to function as multiple tool types.

```json
{
    "type": "apotheosis:omnetic",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "weight": 3,
            "quality": 1.2
        }
    },
    "values": {
        "epic": {
            "name": "multi_tool",
            "tools": [
                {"id": "minecraft:diamond_pickaxe"},
                {"id": "minecraft:diamond_axe"},
                {"id": "minecraft:diamond_shovel"}
            ]
        },
        "mythic": {
            "name": "multi_tool",
            "tools": [
                {"id": "minecraft:netherite_pickaxe"},
                {"id": "minecraft:netherite_axe"},
                {"id": "minecraft:netherite_shovel"}
            ]
        }
    }
}
```
