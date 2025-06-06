# Description
The EnchantmentAffix adds bonus levels to enchantments on an item. This affix can work in three different modes: adding levels to a specific enchantment, boosting an enchantment only if it already exists on the item, or boosting all enchantments on an item.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootCategory](../../loot/LootCategory.md)
4. [LootRarity](../../loot/LootRarity.md)
5. [Enchantment](../../../../../../Minecraft/blob/-/schema/Enchantment.md)
6. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:enchantment",    // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "enchantment": Enchantment,          // [Mandatory] || The enchantment to boost (for SINGLE and EXISTING modes)
    "mode": EnchantmentMode,             // [Optional]  || The mode of operation. Defaults to "SINGLE"
    "values": {                          // [Mandatory] || Per-rarity level values
        LootRarity: StepFunction         // The number of levels to add to the enchantment(s)
    },
    "categories": [                      // [Mandatory] || List of item categories this affix can be applied to
        LootCategory
    ]
}
```

The `mode` must be one of the following values:
- `"SINGLE"` - Adds levels to the specified enchantment, whether it exists on the item or not
- `"EXISTING"` - Adds levels to the specified enchantment, but only if it already exists on the item
- `"GLOBAL"` - Adds levels to all enchantments that already exist on the item

# Examples

## Basic Sharpness Boost Affix
A basic enchantment affix that adds levels to the Sharpness enchantment.

```json
{
    "type": "apotheosis:enchantment",
    "definition": {
        "affix_type": "basic_effect",
        "exclusive_set": [],
        "weights": {
            "uncommon": {
                "weight": 10
            },
            "rare": {
                "weight": 15
            },
            "epic": {
                "weight": 20
            }
        }
    },
    "enchantment": "minecraft:sharpness",
    "mode": "SINGLE",
    "values": {
        "uncommon": {
            "min": 1,
            "max": 1
        },
        "rare": {
            "min": 1,
            "max": 2
        },
        "epic": {
            "min": 2,
            "max": 3
        }
    },
    "categories": [
        "weapon",
        "tool"
    ]
}
```

## Advanced Global Enchantment Boost
A powerful affix that boosts all existing enchantments on an item.

```json
{
    "type": "apotheosis:enchantment",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "summit": {
                "weight": 5,
                "quality": 1.2
            },
            "pinnacle": {
                "weight": 10,
                "quality": 1.5
            }
        }
    },
    "mode": "GLOBAL",
    "values": {
        "epic": {
            "min": 1,
            "max": 1
        },
        "mythic": {
            "min": 1,
            "max": 2
        }
    },
    "categories": [
        "weapon",
        "bow",
        "crossbow",
        "helmet",
        "chestplate",
        "leggings",
        "boots"
    ]
}
```
