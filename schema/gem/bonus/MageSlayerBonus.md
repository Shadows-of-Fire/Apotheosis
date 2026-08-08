# Description
A Mage Slayer Bonus is a [Gem Bonus](./GemBonus.md) which converts a portion of incoming magic damage into healing.  

When the user takes damage from a source tagged as magic (`#c:is_magic`), the user is healed for the given fraction of the damage, and the damage is reduced by the same fraction. Non-magic damage is unaffected.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:mageslayer",
    "gem_class": GemClass,  // [Mandatory] || The set of loot categories this bonus applies to.
    "values": {             // [Mandatory] || The fraction of magic damage converted to healing, for each supported purity. Range: [0, 1].
        Purity: float
    }
}
```

# Examples
A mage slayer bonus for helmets, converting up to 35% of magic damage into healing.

```json
{
    "type": "apotheosis:mageslayer",
    "gem_class": "apotheosis:helmet",
    "values": {
        "normal": 0.15,
        "flawless": 0.225,
        "perfect": 0.35
    }
}
```
