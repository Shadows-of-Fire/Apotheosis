# Description
A Radial Bonus is a [Gem Bonus](./GemBonus.md) which makes the socketed tool mine an area of blocks at once.  

When the user breaks a block, an X-by-Y plane of blocks around the broken position is also broken. Radial mining can be toggled by the player, and never activates while sneaking.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:radial",
    "gem_class": GemClass,   // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {              // [Mandatory] || The mining area for each supported purity.
        Purity: {
            "x": integer,    // [Mandatory] || The width of the mined area.
            "y": integer,    // [Mandatory] || The height of the mined area.
            "xOff": integer, // [Mandatory] || The horizontal offset of the mined area.
            "yOff": integer  // [Mandatory] || The vertical offset of the mined area.
        }
    }
}
```

# Examples
A radial bonus for mining tools, scaling from a 3x2 area up to a 3x3 area.

```json
{
    "type": "apotheosis:radial",
    "gem_class": "apotheosis:breaker",
    "values": {
        "flawed": {
            "x": 3,
            "xOff": 0,
            "y": 2,
            "yOff": 1
        },
        "normal": {
            "x": 3,
            "xOff": 0,
            "y": 3,
            "yOff": 1
        }
    }
}
```
