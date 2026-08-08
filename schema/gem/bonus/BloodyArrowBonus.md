# Description
A Bloody Arrow Bonus is a [Gem Bonus](./GemBonus.md) which empowers fired arrows at the cost of the shooter's health.  

When the user fires an arrow while off cooldown, they take a fraction of their maximum health as damage, and the arrow's base damage is multiplied.  

This bonus has a fixed gem class of bows, and does not declare the `"gem_class"` field.

# Dependencies
This object references the following objects:
1. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:bloody_arrow",
    "values": {                      // [Mandatory] || The bonus data for each supported purity.
        Purity: {
            "health_cost": float,    // [Mandatory] || The fraction of the user's maximum health paid per empowered shot.
            "damage_mult": float,    // [Mandatory] || The multiplier applied to the arrow's base damage.
            "cooldown": integer      // [Mandatory] || The cooldown between empowered shots, in ticks.
        }
    }
}
```

# Examples
A bloody arrow bonus which multiplies arrow damage by up to 2.5x, at the cost of up to half of the user's maximum health.

```json
{
    "type": "apotheosis:bloody_arrow",
    "values": {
        "flawed": {
            "cooldown": 450,
            "damage_mult": 1.6,
            "health_cost": 0.2
        },
        "normal": {
            "cooldown": 450,
            "damage_mult": 1.9,
            "health_cost": 0.3
        },
        "flawless": {
            "cooldown": 450,
            "damage_mult": 2.2,
            "health_cost": 0.4
        },
        "perfect": {
            "cooldown": 450,
            "damage_mult": 2.5,
            "health_cost": 0.5
        }
    }
}
```
