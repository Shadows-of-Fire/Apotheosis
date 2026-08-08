# Description
A Damage Reduction Affix is an [Affix](../Affix.md) which reduces incoming damage of a specific type by a percentage.  

Damage sources that bypass invulnerability or bypass enchantments are always exempt from this reduction.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/1.21/schema/StepFunction.md)
4. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:damage_reduction",
    "definition": AffixDefinition,  // [Mandatory] || The definition of this affix.
    "damage_type": "string",        // [Mandatory] || The type of damage that is reduced.
    "values": {                     // [Mandatory] || The fraction of damage removed, for each supported rarity. Range: [0, 1].
        LootRarity: StepFunction
    },
    "categories": [                 // [Mandatory] || The loot categories this affix may roll on. When empty, all categories are allowed.
        LootCategory
    ]
}
```

The list of damage types is as follows:

1. `physical` - Physical (non-magical) damage.
2. `magic` - Magic damage.
3. `fire` - Fire damage.
4. `fall` - Fall damage.
5. `explosion` - Explosion damage.
6. `projectile` - Projectile damage.
7. `lightning` - Lightning damage.

# Examples
A trimmed version of the Runed affix, which reduces incoming magic damage when rolled on armor.

```json
{
    "type": "apotheosis:damage_reduction",
    "categories": [
        "apotheosis:helmet",
        "apotheosis:chestplate",
        "apotheosis:leggings",
        "apotheosis:boots"
    ],
    "damage_type": "magic",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:armor/dmg_reduction/blockading"
        ],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "values": {
        "apotheosis:common": {
            "min": 0.01,
            "max": 0.05
        },
        "apotheosis:uncommon": {
            "min": 0.01,
            "max": 0.05
        }
    }
}
```
