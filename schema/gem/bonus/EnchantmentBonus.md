# Description
An Enchantment Bonus is a [Gem Bonus](./GemBonus.md) which grants bonus levels of an enchantment to the socketed item.  

The behavior depends on the mode:
* In `single` mode, the levels are added unconditionally, granting the enchantment even if the item does not have it.
* In `existing` mode, the levels are only added if the item already has the enchantment.
* In `global` mode, the levels are added to every enchantment already present on the item. The `"enchantment"` field is not used for the effect in this mode, but is still displayed in the tooltip.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:enchantment",
    "gem_class": GemClass,     // [Mandatory] || The set of loot categories this bonus applies to.
    "enchantment": "string",   // [Mandatory] || The registry name of the enchantment to boost.
    "mode": "string",          // [Optional]  || The application mode. One of "single", "existing", or "global". Default value = "single".
    "values": {                // [Mandatory] || The number of bonus levels granted, for each supported purity. Range: [1, 127].
        Purity: integer
    }
}
```

# Examples
An enchantment bonus which grants up to four bonus levels of Sharpness, if the weapon is already enchanted with Sharpness.

```json
{
    "type": "apotheosis:enchantment",
    "enchantment": "minecraft:sharpness",
    "gem_class": {
        "key": "light_weapon",
        "types": [
            "apotheosis:melee_weapon",
            "apotheosis:trident"
        ]
    },
    "mode": "existing",
    "values": {
        "flawed": 1,
        "normal": 2,
        "flawless": 3,
        "perfect": 4
    }
}
```
