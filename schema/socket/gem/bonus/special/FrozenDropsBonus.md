# Description
FrozenDropsBonus is a special gem bonus that increases the amount of loot dropped by enemies when they are killed primarily with cold damage. An entity is considered to have been killed with "primarily cold damage" when at least 55% of the damage it received was cold damage.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. Cold Damage - A damage type tracked by Apotheosis

# Schema
```js
{
    "type": "apotheosis:frozen_drops_bonus", // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,                  // [Mandatory] || The item types this gem can be applied to
    "values": {                             // [Mandatory] || Per-purity increased drop percentages
        Purity: float                       // Value representing percentage increase in drops (0.5 = 50% more drops)
    }
}
```

# Examples

## Basic Frozen Drops Gem
A gem that increases drops from enemies killed with cold damage.

```json
{
    "type": "apotheosis:frozen_drops_bonus",
    "gem_class": {
        "key": "combat/melee",
        "types": ["sword", "axe"]
    },
    "values": {
        "chipped": 0.15,
        "flawed": 0.25,
        "normal": 0.35,
        "flawless": 0.45,
        "perfect": 0.55
    }
}
```

## Magic Cold Harvesting Gem
A gem for magic weapons that increases drops when using cold magic.

```json
{
    "type": "apotheosis:frozen_drops_bonus",
    "gem_class": {
        "key": "combat/magic",
        "types": ["staff", "wand"]
    },
    "values": {
        "flawless": 0.65,
        "perfect": 0.85
    }
}
```

The bonus intelligently calculates increased drops, handling partial amounts through randomization. For example, if an enemy would drop 3 items and the bonus is 50%, it would add 1 item (3 × 0.5 = 1.5 rounded to either 1 or 2 based on a random roll with a 50% chance).
