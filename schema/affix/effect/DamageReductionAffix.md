# Description
The DamageReductionAffix is a defensive affix that reduces damage of a specific type (like fire, magic, or physical) when applied to equipment. This affix provides percentage-based damage reduction against the specific damage source.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootCategory](../../loot/LootCategory.md)
4. [LootRarity](../../loot/LootRarity.md)
5. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)

# Schema
```js
{
    "type": "apotheosis:damage_reduction", // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,         // [Mandatory] || The affix definition
    "damage_type": DamageType,             // [Mandatory] || The type of damage to reduce
    "values": {                            // [Mandatory] || Per-rarity reduction percentage values (0-1)
        LootRarity: StepFunction
    },
    "categories": [                        // [Mandatory] || List of item categories this affix can be applied to
        LootCategory
    ]
}
```

The `damage_type` must be one of the following values:
- `"physical"` - Physical damage (melee attacks, etc.)
- `"magic"` - Magic damage
- `"fire"` - Fire damage
- `"fall"` - Fall damage
- `"explosion"` - Explosion damage
- `"projectile"` - Projectile damage
- `"lightning"` - Lightning damage

# Examples

## Basic Fire Resistance Affix
A basic affix that reduces fire damage when applied to armor pieces.

```json
{
    "type": "apotheosis:damage_reduction",
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
    "damage_type": "fire",
    "values": {
        "uncommon": {
            "min": 0.1,
            "max": 0.15
        },
        "rare": {
            "min": 0.15,
            "max": 0.2
        },
        "epic": {
            "min": 0.2,
            "max": 0.3
        }
    },
    "categories": [
        "helmet",
        "chestplate",
        "leggings",
        "boots"
    ]
}
```

## Advanced Projectile Protection Affix
A more powerful affix that provides significant protection against projectiles.

```json
{
    "type": "apotheosis:damage_reduction",
    "definition": {
        "affix_type": "basic_effect",
        "exclusive_set": [
            "apotheosis:damage_reduction_explosion"
        ],
        "weights": {
            "summit": {
                "weight": 8,
                "quality": 1.2
            },
            "pinnacle": {
                "weight": 12,
                "quality": 1.5
            }
        }
    },
    "damage_type": "projectile",
    "values": {
        "epic": {
            "min": 0.25,
            "max": 0.35
        },
        "mythic": {
            "min": 0.35,
            "max": 0.5
        }
    },
    "categories": [
        "helmet",
        "chestplate",
        "shield"
    ]
}
```
