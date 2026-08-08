# Description
An Enchantment Affix is an [Affix](../Affix.md) which grants bonus levels of an enchantment to the item.  

The behavior depends on the mode:
* In `single` mode, the levels are added unconditionally, granting the enchantment even if the item does not have it.
* In `existing` mode, the levels are only added if the item already has the enchantment.
* In `global` mode, the levels are added to every enchantment already present on the item. The `"enchantment"` field is not used for the effect in this mode, but is still displayed in the tooltip.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)
4. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:enchantment",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "enchantment": "string",        // [Mandatory] || The registry name of the enchantment to boost.
    "mode": "string",               // [Optional]  || The application mode. One of "single", "existing", or "global". Default value = "single".
    "values": {                     // [Mandatory] || The number of bonus levels granted, for each supported rarity. The output of the function will be truncated to an integer.
        LootRarity: StepFunction
    },
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. When empty, all categories are allowed.
        LootCategory
    ]
}
```

# Examples
The Prosperous affix, which grants six to ten bonus levels of Looting on bows.

```json
{
    "type": "apotheosis:enchantment",
    "categories": [
        "apotheosis:bow"
    ],
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "enchantment": "minecraft:looting",
    "values": {
        "apotheosis:epic": {
            "min": 6.0,
            "max": 8.0,
            "step": 0.25
        },
        "apotheosis:mythic": {
            "min": 8.0,
            "max": 10.0,
            "step": 0.25
        }
    }
}
```
