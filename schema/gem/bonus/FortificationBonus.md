# Description
A Fortification Bonus is a [Gem Bonus](./GemBonus.md) which grants a chance to receive five Fortification Shields when the user is hurt.  

Fortification Shields are a Twilight Forest mechanic, where each shield absorbs one incoming attack.  

This bonus is only available when Twilight Forest is installed.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:twilight_fortification",
    "gem_class": GemClass,      // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {                 // [Mandatory] || The bonus data for each supported purity.
        Purity: {
            "chance": float,    // [Mandatory] || The chance to gain shields when hurt.
            "cooldown": integer // [Mandatory] || The cooldown between activations, in ticks.
        }
    }
}
```

# Examples
A fortification bonus for chestplates, with up to a 15% activation chance.

```json
{
    "type": "apotheosis:twilight_fortification",
    "gem_class": "apotheosis:chestplate",
    "values": {
        "flawed": {
            "chance": 0.05,
            "cooldown": 6000
        },
        "normal": {
            "chance": 0.1,
            "cooldown": 5400
        },
        "flawless": {
            "chance": 0.125,
            "cooldown": 5100
        },
        "perfect": {
            "chance": 0.15,
            "cooldown": 4800
        }
    }
}
```
