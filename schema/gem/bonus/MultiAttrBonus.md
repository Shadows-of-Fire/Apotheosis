# Description
A Multi-Attribute Bonus is a [Gem Bonus](./GemBonus.md) which applies multiple attribute modifiers at once, displayed as a single tooltip line.  

This is typically used to pair a strong upside with a downside, such as bonus armor toughness at the cost of movement speed.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:multi_attribute",
    "gem_class": GemClass,      // [Mandatory] || The set of loot categories this bonus applies to.
    "modifiers": [              // [Mandatory] || The list of attribute modifiers granted by this bonus.
        {
            "attribute": "string",  // [Mandatory] || The registry name of the attribute to modify.
            "operation": "string",  // [Mandatory] || The operation of the modifier. One of "add_value", "add_multiplied_base", or "add_multiplied_total".
            "values": {             // [Mandatory] || The modifier value for each supported purity.
                Purity: float
            }
        }
    ],
    "desc": "string"            // [Mandatory] || A translation key for the combined tooltip line. Receives the formatted modifier text of each modifier as arguments.
}
```

Note: Every modifier in the list should provide values for the same set of purities. The first modifier's values determine which purities this bonus supports.

# Examples
A multi-attribute bonus which grants armor toughness at the cost of movement speed.

```json
{
    "type": "apotheosis:multi_attribute",
    "desc": "bonus.apotheosis:multi_attr.desc.and",
    "gem_class": {
        "key": "lower_armor",
        "types": [
            "apotheosis:leggings",
            "apotheosis:boots"
        ]
    },
    "modifiers": [
        {
            "attribute": "minecraft:generic.armor_toughness",
            "operation": "add_multiplied_total",
            "values": {
                "cracked": 0.1,
                "chipped": 0.15,
                "flawed": 0.175,
                "normal": 0.225,
                "flawless": 0.275,
                "perfect": 0.35
            }
        },
        {
            "attribute": "minecraft:generic.movement_speed",
            "operation": "add_multiplied_total",
            "values": {
                "cracked": -0.025,
                "chipped": -0.05,
                "flawed": -0.075,
                "normal": -0.1,
                "flawless": -0.125,
                "perfect": -0.15
            }
        }
    ]
}
```
