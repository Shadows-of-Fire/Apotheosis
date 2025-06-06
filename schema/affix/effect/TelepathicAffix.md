# Description
The TelepathicAffix is a utility affix that teleports item drops directly to the player. When a player breaks blocks with a tool that has this affix, or when they kill entities with a weapon that has this affix, the drops will be teleported directly to the player's position instead of dropping at the target location.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootRarity](../../loot/LootRarity.md)

# Schema
```js
{
    "type": "apotheosis:telepathic",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "rarities": [                        // [Mandatory] || List of rarities this affix can have
        LootRarity
    ]
}
```

# Examples

## Basic Telepathic Affix
A basic telepathic affix that can appear on rare or higher rarity items.

```json
{
    "type": "apotheosis:telepathic",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [],
        "weights": {
            "rare": {
                "weight": 5,
                "quality": 1.0
            },
            "epic": {
                "weight": 10,
                "quality": 1.0
            },
            "mythic": {
                "weight": 15,
                "quality": 1.0
            }
        }
    },
    "rarities": [
        "rare",
        "epic",
        "mythic"
    ]
}
```

## Late-Game Telepathic Affix
A telepathic affix that only appears on mythic items in the highest world tiers.

```json
{
    "type": "apotheosis:telepathic",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:excavating",
            "apotheosis:stonebreaking"
        ],
        "weights": {
            "summit": {
                "weight": 5,
                "quality": 1.0
            },
            "pinnacle": {
                "weight": 10,
                "quality": 1.2
            }
        }
    },
    "rarities": [
        "mythic"
    ]
}
```
