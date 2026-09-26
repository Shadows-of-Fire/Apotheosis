# Description
An Attribute Toggle Affix is an [Affix](../Affix.md) which lets the wearer toggle all bonuses to one or more attributes on or off with a keybind.  

While an attribute is suppressed, its value for the wearer is clamped down to the player's base value for that attribute, with the vanilla sprinting boost carved out. The clamp only ever lowers the value: net penalties (such as Slowness) still pass through, but a stack of bonuses and penalties whose net effect is above the base value is clamped to normal rather than becoming a penalty.  

All attributes declared by a single affix are toggled together. Pressing the key re-enables every attribute if any of them is currently suppressed, and suppresses every attribute otherwise. The suppression state is stored on the player and is cleared as soon as the boots granting it are removed.  

This affix has no values, and its strength does not change with the affix level.  

This affix may only roll on boots, and does not declare a category field. Instead, it declares the set of rarities it may roll at.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:attribute_toggle",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "attributes": [                 // [Mandatory] || The registry names of the attributes this affix allows the wearer to toggle. Must not be empty.
        "string"
    ],
    "rarities": [                   // [Mandatory] || The set of rarities this affix may roll at.
        LootRarity
    ]
}
```

# Examples
The Steadfast affix, which toggles both movement speed and step height. It is only available at Mythic rarity in the Pinnacle world tier, and is exclusive with the single-attribute Unhurried and Surefooted affixes.

```json
{
    "type": "apotheosis:attribute_toggle",
    "attributes": [
        "minecraft:generic.movement_speed",
        "minecraft:generic.step_height"
    ],
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:boots/ability/unhurried",
            "apotheosis:boots/ability/surefooted"
        ],
        "weights": {
            "pinnacle": {
                "quality": 0.75,
                "weight": 20
            }
        }
    },
    "rarities": [
        "apotheosis:mythic"
    ]
}
```
