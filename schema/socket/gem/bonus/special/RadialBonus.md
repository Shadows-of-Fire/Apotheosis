# Description
RadialBonus is a special gem bonus that adds area mining capability to tools. When mining a block, this bonus will cause additional blocks in a configurable area around the target block to also be broken. Players can toggle between different radial mining modes (always active, only when sneaking, only when not sneaking, or disabled) using a special keybind.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. RadialData - Internal data structure containing area dimensions and offsets

# Schema
```js
{
    "type": "apotheosis:radial_bonus", // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,             // [Mandatory] || The item types this gem can be applied to
    "values": {                        // [Mandatory] || Per-purity area mining configurations
        Purity: {
            "x": int,                  // [Mandatory] || Width of the mining area
            "y": int,                  // [Mandatory] || Height of the mining area
            "xOff": int,               // [Mandatory] || Horizontal offset of the mining area
            "yOff": int                // [Mandatory] || Vertical offset of the mining area
        }
    }
}
```

# Examples

## Basic 3x3 Area Mining Gem
A gem that mines in a square area around the target block.

```json
{
    "type": "apotheosis:radial_bonus",
    "gem_class": {
        "key": "mining/pickaxe",
        "types": ["pickaxe"]
    },
    "values": {
        "chipped": {
            "x": 3,
            "y": 3,
            "xOff": 0,
            "yOff": 0
        },
        "flawed": {
            "x": 3,
            "y": 3,
            "xOff": 0,
            "yOff": 0
        },
        "normal": {
            "x": 3,
            "y": 3,
            "xOff": 0,
            "yOff": 0
        },
        "flawless": {
            "x": 5,
            "y": 3,
            "xOff": 0,
            "yOff": 0
        },
        "perfect": {
            "x": 5,
            "y": 5,
            "xOff": 0,
            "yOff": 0
        }
    }
}
```

## Vertical Tunnel Mining Gem
A gem that mines blocks in a vertical tunnel pattern.

```json
{
    "type": "apotheosis:radial_bonus",
    "gem_class": {
        "key": "mining/excavation",
        "types": ["pickaxe"]
    },
    "values": {
        "flawless": {
            "x": 1,
            "y": 5,
            "xOff": 0,
            "yOff": -2
        },
        "perfect": {
            "x": 1,
            "y": 7,
            "xOff": 0,
            "yOff": -3
        }
    }
}
```

The RadialBonus only breaks blocks that:
1. Have a similar destroy speed to the originally targeted block
2. Can be effectively harvested with the player's current tool
3. Are within the configured mining area

Players can toggle between four different radial mining modes using a keybind:
- REQUIRE_NOT_SNEAKING: Only activates when the player is not sneaking
- REQUIRE_SNEAKING: Only activates when the player is sneaking
- ENABLED: Always activates regardless of sneaking state
- DISABLED: Never activates
