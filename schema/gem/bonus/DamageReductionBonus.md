# Description
A Damage Reduction Bonus is a [Gem Bonus](./GemBonus.md) which reduces incoming damage of a specific type by a percentage.  

Damage sources that bypass invulnerability or bypass enchantments are always exempt from this reduction.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:damage_reduction",
    "damage_type": "string",  // [Mandatory] || The type of damage that is reduced.
    "gem_class": GemClass,    // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {               // [Mandatory] || The fraction of damage removed, for each supported purity. Range: [0, 1].
        Purity: float
    }
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
A damage reduction bonus which reduces incoming physical damage when socketed into chestplates or leggings.

```json
{
    "type": "apotheosis:damage_reduction",
    "damage_type": "physical",
    "gem_class": {
        "key": "core_armor",
        "types": [
            "apotheosis:chestplate",
            "apotheosis:leggings"
        ]
    },
    "values": {
        "cracked": 0.05,
        "chipped": 0.075,
        "flawed": 0.125,
        "normal": 0.175,
        "flawless": 0.225,
        "perfect": 0.275
    }
}
```
