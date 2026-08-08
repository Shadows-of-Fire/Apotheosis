# Description
A Gem is a socketable item that provides bonuses when inserted into an item with an empty socket.  

Each gem holds a list of [Gem Bonuses](./bonus/GemBonus.md), and may hold at most one bonus per loot category. When a gem is socketed, the bonus matching the loot category of the socketed item becomes active.  

Every gem item also has a [Purity](./Purity.md), which determines the strength of its bonuses. A gem may only be socketed into an item if the relevant bonus provides a value for the gem's purity.

# Dependencies
This object references the following objects:
1. [TieredWeights](../tier/TieredWeights.md)
2. [Constraints](../tier/Constraints.md)
3. [Purity](./Purity.md)
4. [GemBonus](./bonus/GemBonus.md)

# Schema
```js
{
    "weights": TieredWeights,    // [Mandatory] || The weights for this gem, relative to other gems.
    "constraints": Constraints,  // [Optional]  || The constraints for this gem. Defaults to no constraints.
    "min_purity": Purity,        // [Optional]  || The minimum purity this gem may generate with. Default value = "cracked".
    "bonuses": [                 // [Mandatory] || The list of bonuses this gem provides. Must not be empty.
        GemBonus
    ],
    "unique": boolean            // [Optional]  || If set, only a single copy of this gem may be socketed into a given item. Default value = false.
}
```

Note: The gem classes of the bonuses may not overlap. If two bonuses cover the same loot category, the gem will fail to load.  

Additional bonuses can be attached to an existing gem using an [ExtraGemBonus](./ExtraGemBonus.md).

# Examples
The Gem of Splendor, which provides luck when socketed into armor, and bonus experience gains when socketed into weapons or tools.

```json
{
    "type": "apotheosis:gem",
    "weights": {
        "quality": 0.1,
        "weight": 100
    },
    "bonuses": [
        {
            "type": "apotheosis:attribute",
            "attribute": "minecraft:generic.luck",
            "gem_class": {
                "key": "armor",
                "types": [
                    "apotheosis:helmet",
                    "apotheosis:chestplate",
                    "apotheosis:leggings",
                    "apotheosis:boots"
                ]
            },
            "operation": "add_value",
            "values": {
                "cracked": 0.5,
                "chipped": 1.25,
                "flawed": 2.0,
                "normal": 2.25,
                "flawless": 3.5,
                "perfect": 5.0
            }
        },
        {
            "type": "apotheosis:attribute",
            "attribute": "apothic_attributes:experience_gained",
            "gem_class": {
                "key": "weapon_or_tool",
                "types": [
                    "apotheosis:melee_weapon",
                    "apotheosis:trident",
                    "apotheosis:bow",
                    "apotheosis:breaker"
                ]
            },
            "operation": "add_multiplied_base",
            "values": {
                "cracked": 0.075,
                "chipped": 0.15,
                "flawed": 0.225,
                "normal": 0.3,
                "flawless": 0.4,
                "perfect": 0.6
            }
        }
    ]
}
```
