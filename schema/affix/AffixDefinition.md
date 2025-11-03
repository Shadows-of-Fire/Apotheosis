# Description
An affix definition defines the core properties of an Affix, including its type, exclusivity rules, and weights. Each Affix will always specify an affix definition in its json schema.

# Dependencies
This object references the following objects:
1. [AffixType](./AffixType.md)
2. [TieredWeights](../tier/TieredWeights.md)

# Schema
```js
{
    "affix_type": AffixType,             // [Mandatory] || The type of affix (STAT, BASIC_EFFECT, or ABILITY)
    "exclusive_set": [                   // [Mandatory] || Set of affix IDs that cannot be applied alongside this affix
        "string"
    ],
    "weights": TieredWeights             // [Mandatory] || Tier-specific weights determining how often this affix appears
}
```

# Examples

## Basic Definition
A basic affix definition for a stat-type affix that appears in all world tiers.

```json
{
    "affix_type": "stat",
    "exclusive_set": [],
    "weights": {
        "weight": 10,
        "quality": 1.0
    }
}
```

## Advanced Definition
A more complex definition for an ability affix that only appears in higher tiers and is exclusive with several other affixes.

```json
{
    "affix_type": "ability",
    "exclusive_set": [
        "apotheosis:lightning_damage",
        "apotheosis:fire_damage",
        "apotheosis:ice_damage"
    ],
    "weights": {
        "ascent": {
            "weight": 5,
            "quality": 0.8
        },
        "summit": {
            "weight": 10,
            "quality": 1.0
        },
        "pinnacle": {
            "weight": 15,
            "quality": 1.2
        }
    }
}
```
