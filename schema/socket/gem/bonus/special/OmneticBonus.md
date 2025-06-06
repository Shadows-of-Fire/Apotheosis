# Description
OmneticBonus is a special gem bonus that grants a tool the ability to function as multiple tools at once (similar to an omni-tool concept). When equipped, it allows a tool to mine blocks as if it were any of the configured tool types, using the best mining speed and harvest capability for any given block.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md) 
3. [Purity](../Purity.md)
4. OmneticData - Internal data structure containing tool configurations

# Schema
```js
{
    "type": "apotheosis:omnetic_bonus", // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,             // [Mandatory] || The item types this gem can be applied to
    "values": {                        // [Mandatory] || Per-purity tool configurations
        Purity: {
            "name": string,           // [Mandatory] || Identifier name for this tool configuration
            "items": [                // [Mandatory] || Array of tool items that this bonus will mimic
                ItemStack
            ]
        }
    }
}
```

# Examples

## Basic Mining Omnetic Gem
A gem that makes a tool function as multiple mining tools at once.

```json
{
    "type": "apotheosis:omnetic_bonus",
    "gem_class": {
        "key": "mining/tool",
        "types": ["pickaxe"]
    },
    "values": {
        "chipped": {
            "name": "basic_mining",
            "items": [
                {"id": "minecraft:iron_pickaxe"},
                {"id": "minecraft:iron_shovel"}
            ]
        },
        "flawed": {
            "name": "advanced_mining",
            "items": [
                {"id": "minecraft:iron_pickaxe"},
                {"id": "minecraft:iron_shovel"},
                {"id": "minecraft:iron_axe"}
            ]
        },
        "normal": {
            "name": "diamond_mining",
            "items": [
                {"id": "minecraft:diamond_pickaxe"},
                {"id": "minecraft:diamond_shovel"},
                {"id": "minecraft:diamond_axe"}
            ]
        },
        "flawless": {
            "name": "netherite_mining",
            "items": [
                {"id": "minecraft:netherite_pickaxe"},
                {"id": "minecraft:netherite_shovel"},
                {"id": "minecraft:netherite_axe"}
            ]
        },
        "perfect": {
            "name": "complete_mining",
            "items": [
                {"id": "minecraft:netherite_pickaxe"},
                {"id": "minecraft:netherite_shovel"},
                {"id": "minecraft:netherite_axe"},
                {"id": "minecraft:netherite_hoe"}
            ]
        }
    }
}
```

## Farm Tool Gem
A specialized gem for farming tools.

```json
{
    "type": "apotheosis:omnetic_bonus",
    "gem_class": {
        "key": "farming/tool",
        "types": ["hoe"]
    },
    "values": {
        "flawless": {
            "name": "farmer",
            "items": [
                {"id": "minecraft:diamond_hoe"},
                {"id": "minecraft:diamond_axe"},
                {"id": "minecraft:shears"}
            ]
        },
        "perfect": {
            "name": "master_farmer",
            "items": [
                {"id": "minecraft:netherite_hoe"},
                {"id": "minecraft:netherite_axe"},
                {"id": "minecraft:shears"}
            ]
        }
    }
}
```
