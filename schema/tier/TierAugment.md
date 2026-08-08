# Description
A Tier Augment is a global modifier that is active while a given [World Tier](./WorldTier.md) is active.  

Augments targeting players are applied to every player in the tier, and are removed when the player changes tiers.  
Augments targeting monsters are applied to every monster spawned while the tier is active, and are never removed.  

Tier Augments are displayed in the world tier selection window, so the player can see everything that changes with each tier.

# Dependencies
This object references the following objects:
1. [WorldTier](./WorldTier.md)

# Subtypes
Tier Augments are subtyped, meaning each subtype declares a `"type"` key and its own parameters.  

In addition to the type key, every tier augment provides the following common fields:

```js
{
    "type": "string",       // [Mandatory] || The type of this tier augment.
    "tier": WorldTier,      // [Mandatory] || The world tier this augment is active in.
    "target": "string",     // [Mandatory] || The target of this augment. Either "players" or "monsters".
    "sort_index": integer,  // [Optional]  || Ordering of this augment in the world tier selection window. Lower numbers are displayed first. Default value = 1000. Range: [0, 2000].
    ...                     // Additional type-specific parameters.
}
```

The list of tier augment types is as follows:

1. [Attribute Augment](./AttributeAugment.md) - Applies an attribute modifier to all targets.
