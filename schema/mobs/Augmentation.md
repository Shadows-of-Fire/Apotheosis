# Description
An Augmentation is a non-exclusive modifier that may apply to any naturally spawned mob.  

Augmentations are applied very early in the mob spawn pipeline, immediately after world tier modifiers.  
Mobs first roll a global chance to be selected for augmenting. If a mob is selected, every loaded augmentation will attempt to apply, each rolling its own application chance and checking its own constraints and conditions.

# Dependencies
This object references the following objects:
1. [Constraints](../tier/Constraints.md)
2. [SpawnCondition](../util/SpawnCondition.md)
3. [EntityModifier](./EntityModifier.md)

# Schema
```js
{
    "application_chance": float,  // [Mandatory] || The chance that this augmentation is applied, after the mob has been selected for augmenting. Range: [0, 1].
    "constraints": Constraints,   // [Optional]  || Context-based restrictions on the application of this augmentation. Defaults to no constraints.
    "conditions": [               // [Optional]  || Entity-based restrictions, which must all pass. Default value = empty list.
        SpawnCondition
    ],
    "modifiers": [                // [Mandatory] || The list of modifiers applied to the target mob. Must not be empty.
        EntityModifier
    ]
}
```

# Examples
An augmentation with a 12% application chance, which grants a random affix item to any monster that was not spawned by a spawner.

```json
{
    "type": "apotheosis:augment",
    "application_chance": 0.12,
    "conditions": [
        {
            "type": "apotheosis:not",
            "spawn_condition": {
                "type": "apotheosis:spawn_type",
                "spawn_types": [
                    "spawner",
                    "trial_spawner"
                ]
            }
        },
        {
            "type": "apotheosis:is_monster"
        }
    ],
    "modifiers": [
        {
            "type": "apotheosis:random_affix_item"
        }
    ]
}
```
