# Description
An Ore Magnet Bonus is a [Gem Bonus](./GemBonus.md) which allows the socketed tool to be used as a Twilight Forest Ore Magnet.  

Right-clicking a block activates the ore magnet effect, pulling buried ores towards the user, at the cost of the configured amount of durability.  

This bonus is only available when Twilight Forest is installed.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:twilight_ore_magnet",
    "gem_class": GemClass,  // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {             // [Mandatory] || The durability cost per use, for each supported purity. Range: [0, 4096].
        Purity: integer
    }
}
```

# Examples
An ore magnet bonus for mining tools, with the durability cost decreasing at higher purities.

```json
{
    "type": "apotheosis:twilight_ore_magnet",
    "gem_class": "apotheosis:breaker",
    "values": {
        "flawed": 24,
        "normal": 20,
        "flawless": 16,
        "perfect": 10
    }
}
```
