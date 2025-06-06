# Description
The ThunderstruckAffix is a powerful combat affix that applies lightning damage to enemies near the target when you attack. When a player attacks with a weapon that has this affix, it deals additional lightning damage to all nearby enemies (not just the target being hit).

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
    "type": "apotheosis:thunderstruck",  // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "categories": [                      // [Mandatory] || List of item categories this affix can be applied to
        LootCategory
    ],
    "values": {                          // [Mandatory] || Per-rarity damage values
        LootRarity: StepFunction         // The lightning damage dealt to nearby entities
    }
}
```

# Examples

## Basic Thunderstruck Affix
A basic thunderstruck affix that can be applied to melee weapons and deals moderate lightning damage.

```json
{
    "type": "apotheosis:thunderstruck",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:fire_damage",
            "apotheosis:ice_damage"
        ],
        "weights": {
            "rare": {
                "weight": 5
            },
            "epic": {
                "weight": 10
            },
            "mythic": {
                "weight": 15
            }
        }
    },
    "categories": [
        "weapon"
    ],
    "values": {
        "rare": {
            "min": 2.0,
            "max": 4.0
        },
        "epic": {
            "min": 4.0,
            "max": 6.0
        },
        "mythic": {
            "min": 6.0,
            "max": 10.0
        }
    }
}
```

## Powerful Thunderstruck Affix
A more powerful thunderstruck affix that deals high lightning damage and is available for both melee and ranged weapons.

```json
{
    "type": "apotheosis:thunderstruck",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:fire_damage",
            "apotheosis:ice_damage",
            "apotheosis:cleaving"
        ],
        "weights": {
            "summit": {
                "weight": 5,
                "quality": 1.2
            },
            "pinnacle": {
                "weight": 8,
                "quality": 1.5
            }
        }
    },
    "categories": [
        "weapon",
        "bow",
        "crossbow"
    ],
    "values": {
        "epic": {
            "min": 8.0,
            "max": 12.0
        },
        "mythic": {
            "min": 12.0,
            "max": 20.0
        }
    }
}
```
