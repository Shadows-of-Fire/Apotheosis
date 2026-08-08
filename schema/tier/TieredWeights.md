# Description
A Tiered Weights is a mapping from World Tier to Weight, and is used by most objects to specify their per-tier weight and quality values. 
   
There are a couple different syntaxes for specifying weights for all tiers uniformly, all tiers individually, or a subset of tiers.

# Dependencies
This object references the following objects:
1. [WorldTier](./WorldTier.md)
2. [Weight](./Weight.md)

# Schema

## Individual Tier Weights

Tiered Weights are typically represented in the following form:

```js
{
    WorldTier: Weight
}
```

A Weight may be specified for each world tier. Any tiers that are omitted will receive a weight of zero.

## Shared Weights

To specify the same weight value for all tiers, a TieredWeights object can be directly inlined as a Weight object.

```js
Weight
```

# Examples
Weight values specified for frontier, ascent, and summit. Since both haven and pinnacle are omitted, they have a weight (and quality) of zero.  

```json
{
    "frontier": {
        "quality": 1.0,
        "weight": 25
    },
    "ascent": {
        "weight": 10
    },
    "summit": {
        "weight": 10
    }
}
```

Weights that will be used for all tiers.  

```json
{
    "quality": 1.0,
    "weight": 25
}
```
