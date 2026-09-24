# Description
An Anti-Gravity Arrow Bonus is a [Gem Bonus](./GemBonus.md) which removes gravity from arrows fired by the socketed weapon.  

When the user fires an arrow (including thrown tridents), the arrow is marked as having no gravity, so it travels in a straight line. Gravity is restored when the arrow hits something, so projectiles which bounce after impact (such as tridents) land nearby as usual. Gravity is also restored once the arrow has slowed to near-zero speed or has flown for 30 seconds, so arrows that miss everything still fall and despawn normally. Non-arrow projectiles (such as fireworks) are unaffected.

This bonus has no per-purity strength. Instead of a `"values"` map, it declares a minimum purity, and is active for all purities at or above that value.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:anti_gravity_arrow",
    "gem_class": GemClass,  // [Mandatory] || The set of loot categories this bonus applies to.
    "min_purity": Purity    // [Optional]  || The lowest purity at which this bonus is active. Default value: "cracked".
}
```

# Examples
An anti-gravity arrow bonus for bows and tridents, active at flawless purity and above.

```json
{
    "type": "apotheosis:anti_gravity_arrow",
    "gem_class": {
        "key": "ranged_weapon",
        "types": [
            "apotheosis:bow",
            "apotheosis:trident"
        ]
    },
    "min_purity": "flawless"
}
```
