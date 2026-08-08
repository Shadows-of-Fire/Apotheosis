# Description
An Attribute Affix is an [Affix](./Affix.md) which applies a single attribute modifier to the item.  

The modifier's value is produced by evaluating the step function for the item's rarity at the affix's level, and the modifier is active in every equipment slot matching the item's loot category.

# Dependencies
This object references the following objects:
1. [AffixDefinition](./AffixDefinition.md)
2. [LootRarity](../loot/LootRarity.md)
3. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)
4. [LootCategory](../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:attribute",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "attribute": "string",          // [Mandatory] || The registry name of the attribute to modify.
    "operation": "string",          // [Mandatory] || The operation of the modifier. One of "add_value", "add_multiplied_base", or "add_multiplied_total".
    "values": {                     // [Mandatory] || The modifier value for each supported rarity. This affix may only roll on rarities present in this map.
        LootRarity: StepFunction
    },
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. When empty, all categories are allowed.
        LootCategory
    ]
}
```

# Examples
A trimmed version of the Berserking affix, which grants overheal on melee weapons, and is exclusive with the Vampiric affix.

```json
{
    "type": "apotheosis:attribute",
    "attribute": "apothic_attributes:overheal",
    "categories": [
        "apotheosis:melee_weapon"
    ],
    "definition": {
        "affix_type": "stat",
        "exclusive_set": [
            "apotheosis:melee/attribute/vampiric"
        ],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "operation": "add_value",
    "values": {
        "apotheosis:common": {
            "min": 0.05,
            "max": 0.1
        },
        "apotheosis:rare": {
            "min": 0.1,
            "max": 0.2
        }
    }
}
```
