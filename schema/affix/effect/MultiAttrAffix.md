# Description
MultiAttrAffix is a complex affix that applies multiple attribute modifiers to a single item. Unlike standard attribute affixes, this allows for creating affixes that affect multiple attributes simultaneously, with specific value ranges for each attribute and a custom description format.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootCategory](../../loot/LootCategory.md)
4. [LootRarity](../../loot/LootRarity.md)
5. [Attribute](../../../../../Minecraft/blob/-/schema/Attribute.md)
6. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:multi_attr",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,        // [Mandatory] || The affix definition
    "modifiers": [                        // [Mandatory] || The list of attribute modifiers
        {
            "attribute": Attribute,       // [Mandatory] || The attribute to modify
            "operation": Operation,       // [Mandatory] || The attribute modifier operation
            "values": {                   // [Mandatory] || Per-rarity attribute value ranges
                LootRarity: StepFunction
            }
        }
    ],
    "desc": string,                       // [Mandatory] || The description format string with %s placeholders for each modifier
    "categories": [                       // [Mandatory] || The item categories this affix can be applied to
        LootCategory
    ]
}
```

The attribute operation (`operation`) must be one of the following values:
- `"add_value"` - Adds a flat value to the attribute
- `"add_multiplied_base"` - Adds a percentage of the base value
- `"add_multiplied_total"` - Adds a percentage of the total value (after other modifiers)

# Examples

## Dual Attribute Affix
An affix that increases both attack damage and attack speed.

```json
{
    "type": "apotheosis:multi_attr",
    "definition": {
        "affix_type": "weapon_major",
        "exclusive_set": [],
        "weights": {
            "weight": 7,
            "quality": 0.9
        }
    },
    "modifiers": [
        {
            "attribute": "minecraft:generic.attack_damage",
            "operation": "add_multiplied_base",
            "values": {
                "rare": {
                    "type": "placebo:step",
                    "steps": [0.15, 0.20, 0.25],
                    "y_interp": "linear"
                },
                "epic": {
                    "type": "placebo:step",
                    "steps": [0.25, 0.30, 0.35],
                    "y_interp": "linear"
                },
                "mythic": {
                    "type": "placebo:step",
                    "steps": [0.35, 0.40, 0.45],
                    "y_interp": "linear"
                }
            }
        },
        {
            "attribute": "minecraft:generic.attack_speed",
            "operation": "add_value",
            "values": {
                "rare": {
                    "type": "placebo:step",
                    "steps": [0.2, 0.3, 0.4],
                    "y_interp": "linear"
                },
                "epic": {
                    "type": "placebo:step",
                    "steps": [0.4, 0.5, 0.6],
                    "y_interp": "linear"
                },
                "mythic": {
                    "type": "placebo:step",
                    "steps": [0.6, 0.7, 0.8],
                    "y_interp": "linear"
                }
            }
        }
    ],
    "desc": "Increases damage and attack speed by %s and %s",
    "categories": [
        "sword",
        "trident",
        "melee_weapon"
    ]
}
```
