# Description
An Attribute Augment is a [Tier Augment](./TierAugment.md) which applies an attribute modifier to all targets in the tier.  

The modifier is silently ignored if the target entity does not have the attribute.

# Dependencies
This object references the following objects:
1. [TierAugment](./TierAugment.md)
2. [WorldTier](./WorldTier.md)
3. [RandomAttributeModifier](../../../../../Placebo/blob/-/schema/RandomAttributeModifier.md)

# Schema
```js
{
    "type": "apotheosis:attribute",
    "tier": WorldTier,                    // [Mandatory] || The world tier this augment is active in.
    "target": "string",                   // [Mandatory] || The target of this augment. Either "players" or "monsters".
    "sort_index": integer,                // [Optional]  || Ordering of this augment in the world tier selection window. Lower numbers are displayed first. Default value = 1000. Range: [0, 2000].
    "modifier": RandomAttributeModifier,  // [Mandatory] || The attribute modifier to apply. Used in constant mode.
    "modifier_id": "string"               // [Mandatory] || A unique identifier for this attribute modifier. Only needs to be unique on a per-attribute basis.
}
```

# Examples
An augment which grants all monsters in the ascent tier eight armor points.

```json
{
    "type": "apotheosis:attribute",
    "modifier": {
        "attribute": "minecraft:generic.armor",
        "operation": "add_value",
        "value": 8.0
    },
    "modifier_id": "apotheosis:ascent/armor",
    "sort_index": 100,
    "target": "monsters",
    "tier": "ascent"
}
```
