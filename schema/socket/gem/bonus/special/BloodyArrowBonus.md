# Description
BloodyArrowBonus is a special gem bonus for bows that increases arrow damage at the cost of the user's health. When firing an arrow, the player sacrifices a percentage of their maximum health to significantly boost the arrow's damage. This bonus includes a cooldown between activations.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md) - This bonus is exclusively for the "bow" gem class
3. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:bloody_arrow_bonus", // [Mandatory] || The bonus type identifier
    "values": {                            // [Mandatory] || Per-purity configuration values
        Purity: {
            "health_cost": float,         // [Mandatory] || Percentage of max health sacrificed (0.1 = 10%)
            "damage_mult": float,         // [Mandatory] || Multiplier for arrow damage (2.0 = double damage)
            "cooldown": int               // [Mandatory] || Cooldown in ticks between uses (20 ticks = 1 second)
        }
    }
}
```

# Examples

## Basic Bloody Arrow Gem
A gem that sacrifices health to increase arrow damage with cooldowns based on purity.

```json
{
    "type": "apotheosis:bloody_arrow_bonus",
    "values": {
        "chipped": {
            "health_cost": 0.05,
            "damage_mult": 1.5,
            "cooldown": 200
        },
        "flawed": {
            "health_cost": 0.05,
            "damage_mult": 1.75,
            "cooldown": 180
        },
        "normal": {
            "health_cost": 0.05,
            "damage_mult": 2.0,
            "cooldown": 160
        },
        "flawless": {
            "health_cost": 0.05,
            "damage_mult": 2.25,
            "cooldown": 140
        },
        "perfect": {
            "health_cost": 0.05,
            "damage_mult": 2.5,
            "cooldown": 120
        }
    }
}
```

## High Risk, High Reward Gem
A gem with higher health cost but much greater damage increase and shorter cooldown.

```json
{
    "type": "apotheosis:bloody_arrow_bonus",
    "values": {
        "flawless": {
            "health_cost": 0.15,
            "damage_mult": 3.0,
            "cooldown": 100
        },
        "perfect": {
            "health_cost": 0.2,
            "damage_mult": 4.0,
            "cooldown": 80
        }
    }
}
```
