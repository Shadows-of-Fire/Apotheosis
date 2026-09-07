# Description
A Damage Reduction Augment is a [Tier Augment](./TierAugment.md) which grants all targets in the tier a percentage reduction to incoming damage of a specific type.  

The reduction is applied to incoming damage before armor and enchantment mitigation.  

Damage reductions stack multiplicatively. Starting from a damage factor of 1, each reduction `n` scales the factor by `(1 - n)`, so two 25% reductions produce an effective reduction of 43.75%. The effective reduction can only reach 100% if a single 100% reduction is present.

# Dependencies
This object references the following objects:
1. [TierAugment](./TierAugment.md)
2. [WorldTier](./WorldTier.md)

# Schema
```js
{
    "type": "apotheosis:damage_reduction",
    "tier": WorldTier,        // [Mandatory] || The world tier this augment is active in.
    "target": "string",       // [Mandatory] || The target of this augment. Either "players" or "monsters".
    "sort_index": integer,    // [Optional]  || Ordering of this augment in the world tier selection window. Lower numbers are displayed first. Default value = 1000. Range: [0, 2000].
    "damage_type": "string",  // [Mandatory] || The type of damage that is reduced.
    "amount": float,          // [Mandatory] || The fraction of damage removed. Range: [0, 1].
    "reduction_id": "string"  // [Mandatory] || A unique identifier for this reduction. Only needs to be unique on a per-damage-type basis.
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
An augment which grants all monsters in the summit tier a 10% reduction to incoming physical damage.

```json
{
    "type": "apotheosis:damage_reduction",
    "amount": 0.1,
    "damage_type": "physical",
    "reduction_id": "apotheosis:summit/physical_reduction",
    "sort_index": 400,
    "target": "monsters",
    "tier": "summit"
}
```
