# Description
A Leech Block Bonus is a [Gem Bonus](./GemBonus.md) which heals the user when they block damage with a shield.  

The heal amount is a fraction of the blocked damage. Blocks of two damage or less do not trigger the heal, and the blocked damage itself is not modified.  

This bonus has a fixed gem class of shields, and does not declare the `"gem_class"` field.

# Dependencies
This object references the following objects:
1. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:leech_block",
    "values": {                    // [Mandatory] || The bonus data for each supported purity.
        Purity: {
            "heal_factor": float,  // [Mandatory] || The fraction of the blocked damage restored as health.
            "cooldown": integer    // [Mandatory] || The cooldown between heals, in ticks.
        }
    }
}
```

# Examples
A leech block bonus which restores up to 65% of the blocked damage.

```json
{
    "type": "apotheosis:leech_block",
    "values": {
        "flawed": {
            "cooldown": 450,
            "heal_factor": 0.25
        },
        "normal": {
            "cooldown": 450,
            "heal_factor": 0.4
        },
        "flawless": {
            "cooldown": 450,
            "heal_factor": 0.55
        },
        "perfect": {
            "cooldown": 450,
            "heal_factor": 0.65
        }
    }
}
```
