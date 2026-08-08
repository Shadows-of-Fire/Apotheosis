# Description
A Durability Bonus is a [Gem Bonus](./GemBonus.md) which grants a chance to ignore durability damage taken by the socketed item.  

The value is the percentage of durability damage that is ignored, and is summed with all other sources of durability reduction on the item.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:durability",
    "gem_class": GemClass,  // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {             // [Mandatory] || The percentage of durability damage to ignore, for each supported purity. Range: [0, 1].
        Purity: float
    }
}
```

# Examples
A durability bonus for tools, scaling from 10% to 60%.

```json
{
    "type": "apotheosis:durability",
    "gem_class": {
        "key": "tools",
        "types": [
            "apotheosis:breaker",
            "apotheosis:shears"
        ]
    },
    "values": {
        "cracked": 0.1,
        "chipped": 0.15,
        "flawed": 0.25,
        "normal": 0.35,
        "flawless": 0.45,
        "perfect": 0.6
    }
}
```
