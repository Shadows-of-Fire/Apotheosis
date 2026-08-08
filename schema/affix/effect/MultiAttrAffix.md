# Description
A Multi-Attribute Affix is an [Affix](../Affix.md) which applies multiple attribute modifiers at once, displayed as a single tooltip line.  

This is typically used to pair a strong upside with a downside, such as bonus armor toughness at the cost of movement speed.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)
4. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:multi_attr",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "modifiers": [                  // [Mandatory] || The list of attribute modifiers granted by this affix.
        {
            "attribute": "string",  // [Mandatory] || The registry name of the attribute to modify.
            "operation": "string",  // [Mandatory] || The operation of the modifier. One of "add_value", "add_multiplied_base", or "add_multiplied_total".
            "values": {             // [Mandatory] || The modifier value for each supported rarity.
                LootRarity: StepFunction
            }
        }
    ],
    "desc": "string",               // [Mandatory] || A translation key for the combined tooltip line. Receives the formatted modifier text of each modifier as arguments.
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. When empty, all categories are allowed.
        LootCategory
    ]
}
```

Note: Every modifier in the list must provide values for the exact same set of rarities, or the affix will fail to load.

# Examples
A multi-attribute affix which grants armor toughness at the cost of movement speed on leg and foot armor.

```json
{
    "type": "apotheosis:multi_attr",
    "categories": [
        "apotheosis:leggings",
        "apotheosis:boots"
    ],
    "definition": {
        "affix_type": "stat",
        "exclusive_set": [],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "desc": "bonus.apotheosis:multi_attr.desc.and",
    "modifiers": [
        {
            "attribute": "minecraft:generic.armor_toughness",
            "operation": "add_multiplied_total",
            "values": {
                "apotheosis:epic": {
                    "min": 0.1,
                    "max": 0.2
                },
                "apotheosis:mythic": {
                    "min": 0.15,
                    "max": 0.25
                }
            }
        },
        {
            "attribute": "minecraft:generic.movement_speed",
            "operation": "add_multiplied_total",
            "values": {
                "apotheosis:epic": {
                    "min": -0.1,
                    "max": -0.05
                },
                "apotheosis:mythic": {
                    "min": -0.15,
                    "max": -0.1
                }
            }
        }
    ]
}
```
