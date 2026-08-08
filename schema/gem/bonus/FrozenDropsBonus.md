# Description
A Frozen Drops Bonus is a [Gem Bonus](./GemBonus.md) which grants bonus loot from mobs that were killed primarily via cold damage.  

When a player kills a mob that has taken at least 55% of its maximum health as cold damage, the dropped loot is increased by the given percentage. Fractional results are resolved randomly, so a value of 0.666 always grants at least 66% more loot, with a chance for slightly more.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:frozen_drops",
    "gem_class": GemClass,  // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {             // [Mandatory] || The bonus loot percentage for each supported purity, as a fraction of the original drops. 1.0 = 100% more loot. Range: [0, 100].
        Purity: float
    }
}
```

# Examples
A frozen drops bonus for melee weapons, granting up to 225% bonus loot.

```json
{
    "type": "apotheosis:frozen_drops",
    "gem_class": "apotheosis:melee_weapon",
    "values": {
        "normal": 0.666,
        "flawless": 1.35,
        "perfect": 2.25
    }
}
```
