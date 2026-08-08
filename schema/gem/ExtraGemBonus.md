# Description
An Extra Gem Bonus attaches additional bonuses to an existing [Gem](./Gem.md).  

This is primarily used to add conditional bonuses to gems — for example, bonuses that should only exist when another mod is installed — and can also be used by addon mods to extend Apotheosis-native gems.  

The rules for gems still apply: after all extra bonuses are attached, each loot category may still only be covered by a single bonus on the target gem.

# Dependencies
This object references the following objects:
1. [Gem](./Gem.md)
2. [GemBonus](./bonus/GemBonus.md)

# Schema
```js
{
    "gem": "string",  // [Mandatory] || Registry name of the gem to attach bonuses to.
    "bonuses": [      // [Mandatory] || The list of bonuses to attach.
        GemBonus
    ]
}
```

# Examples
An extra bonus which extends the Gem of Splendor with an armor bonus for shields.

```json
{
    "type": "apotheosis:extra_gem_bonus",
    "gem": "apotheosis:core/splendor",
    "bonuses": [
        {
            "type": "apotheosis:attribute",
            "attribute": "minecraft:generic.armor",
            "gem_class": "apotheosis:shield",
            "operation": "add_value",
            "values": {
                "cracked": 1.0,
                "chipped": 2.0,
                "flawed": 3.0,
                "normal": 4.0,
                "flawless": 5.0,
                "perfect": 6.0
            }
        }
    ]
}
```
