# Description
A Treasure Goblin Bonus is a [Gem Bonus](./GemBonus.md) which grants a chance to summon a Treasure Goblin when the user attacks an entity.  

A Treasure Goblin is a fast, glowing Redcap with bonus health that drops valuable treasure when slain.  

This bonus is only available when Twilight Forest is installed.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:twilight_treasure_goblin",
    "gem_class": GemClass,      // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {                 // [Mandatory] || The bonus data for each supported purity.
        Purity: {
            "chance": float,    // [Mandatory] || The chance to summon a goblin when attacking.
            "cooldown": integer // [Mandatory] || The cooldown between summons, in ticks.
        }
    }
}
```

# Examples
A treasure goblin bonus for weapons, with up to a 1.5% summon chance.

```json
{
    "type": "apotheosis:twilight_treasure_goblin",
    "gem_class": {
        "key": "weapons",
        "types": [
            "apotheosis:melee_weapon",
            "apotheosis:trident",
            "apotheosis:bow"
        ]
    },
    "values": {
        "flawed": {
            "chance": 0.005,
            "cooldown": 4800
        },
        "normal": {
            "chance": 0.0075,
            "cooldown": 4800
        },
        "flawless": {
            "chance": 0.01,
            "cooldown": 4800
        },
        "perfect": {
            "chance": 0.015,
            "cooldown": 4800
        }
    }
}
```
