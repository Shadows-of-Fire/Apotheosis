# Description
MultiAttrAffix is a complex affix that applies multiple attribute modifiers to a single item. Unlike standard attribute affixes, this allows for creating affixes that affect multiple attributes simultaneously, with specific value ranges for each attribute and a custom description format.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootCategory](../../loot/LootCategory.md)
3. [LootRarity](../../loot/LootRarity.md)
4. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:multi_attr",      // [Mandatory] || The affix type identifier
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
    "desc": string,                       // [Mandatory] || A language key for the description. %s placeholders are replaced with each modifier.
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
                "apotheosis:common": {
                    "min": 0.2,
                    "max": 0.3
                },
                "apotheosis:uncommon": {
                    "min": 0.2,
                    "max": 0.3
                },
                "apotheosis:rare": {
                    "min": 0.3,
                    "max": 0.5
                },
                "apotheosis:epic": {
                    "min": 0.3,
                    "max": 0.5
                },
                "apotheosis:mythic": {
                    "min": 0.4,
                    "max": 0.7
                }
            }
        },
        {
            "attribute": "minecraft:generic.attack_speed",
            "operation": "add_value",
            "values": {
                "apotheosis:common": {
                    "min": 0.2,
                    "max": 0.3
                },
                "apotheosis:uncommon": {
                    "min": 0.2,
                    "max": 0.3
                },
                "apotheosis:rare": {
                    "min": 0.3,
                    "max": 0.5
                },
                "apotheosis:epic": {
                    "min": 0.3,
                    "max": 0.5
                },
                "apotheosis:mythic": {
                    "min": 0.4,
                    "max": 0.7
                }
            }
        }
    ],
    "desc": "Increases damage and attack speed by %s and %s",
    "categories": [
        "apotheosis:melee_weapon"
    ]
}
```
