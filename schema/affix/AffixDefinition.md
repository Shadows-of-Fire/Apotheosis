# Description
An Affix Definition holds the information common to all [Affixes](./Affix.md): the affix type, the exclusive set, and the weights.  

Two affixes may never coexist on an item if either one names the other in its exclusive set.

# Dependencies
This object references the following objects:
1. [AffixType](./AffixType.md)
2. [TieredWeights](../tier/TieredWeights.md)

# Schema
```js
{
    "affix_type": AffixType,  // [Mandatory] || The type of the owning affix, used by loot rules when selecting which affixes to apply.
    "exclusive_set": [        // [Mandatory] || A set of affix registry names that may not coexist with the owning affix. May be empty. Exclusivity is checked in both directions.
        "string"
    ],
    "weights": TieredWeights  // [Mandatory] || The weights of the owning affix, relative to other affixes in the same selection pool.
}
```

Note: Naming an unknown affix in the exclusive set will log an error when the data is loaded, but will not prevent the affix from loading.

# Examples
A definition for a stat affix that is exclusive with the Vampiric affix, and available in all tiers.

```json
{
    "affix_type": "stat",
    "exclusive_set": [
        "apotheosis:melee/attribute/vampiric"
    ],
    "weights": {
        "quality": 0.1,
        "weight": 25
    }
}
```
