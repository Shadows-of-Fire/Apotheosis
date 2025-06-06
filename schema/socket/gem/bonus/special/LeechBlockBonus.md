# Description
LeechBlockBonus is a special gem bonus designed for shield items that allows the user to heal when blocking damage. When a player blocks damage with a shield containing this gem, they will be healed for a percentage of the blocked damage. The bonus only activates when the blocked damage exceeds a minimum threshold (2 points) and includes a cooldown between activations.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md) - This bonus is exclusively for the "shield" gem class
3. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:leech_block_bonus", // [Mandatory] || The bonus type identifier
    "values": {                            // [Mandatory] || Per-purity configuration values
        Purity: {
            "heal_factor": float,         // [Mandatory] || Percentage of blocked damage converted to healing (0.2 = 20%)
            "cooldown": int               // [Mandatory] || Cooldown in ticks between activations (20 ticks = 1 second)
        }
    }
}
```

# Examples

## Basic Leeching Shield Gem
A gem that converts blocked damage to healing with varying effectiveness based on purity.

```json
{
    "type": "apotheosis:leech_block_bonus",
    "values": {
        "chipped": {
            "heal_factor": 0.05,
            "cooldown": 200
        },
        "flawed": {
            "heal_factor": 0.1,
            "cooldown": 180
        },
        "normal": {
            "heal_factor": 0.15,
            "cooldown": 160
        },
        "flawless": {
            "heal_factor": 0.2,
            "cooldown": 140
        },
        "perfect": {
            "heal_factor": 0.25,
            "cooldown": 120
        }
    }
}
```

## Vampiric Shield Gem
A high-tier gem with greater healing potential and faster cooldowns.

```json
{
    "type": "apotheosis:leech_block_bonus",
    "values": {
        "flawless": {
            "heal_factor": 0.3,
            "cooldown": 100
        },
        "perfect": {
            "heal_factor": 0.4,
            "cooldown": 80
        }
    }
}
```
