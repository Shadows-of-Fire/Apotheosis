# Description
Basic Boss Data is the set of common boss information shared between [Invaders](./Invader.md) and [Elites](./Elite.md).  

It holds the weights and constraints for the owning object, as well as the name, gear, NBT, and supporting entities applied to the spawned mob.

# Dependencies
This object references the following objects:
1. [TieredWeights](../tier/TieredWeights.md)
2. [Constraints](../tier/Constraints.md)
3. [BonusLootTables](../util/BonusLootTables.md)
4. [WorldTier](../tier/WorldTier.md)
5. [SetPredicate](../../../../../Placebo/blob/-/schema/SetPredicate.md)
6. [CompoundTag](../../../../../Placebo/blob/-/schema/CompoundTag.md)
7. [SupportingEntity](./SupportingEntity.md)
8. [SpawnCondition](../util/SpawnCondition.md)

# Schema
```js
{
    "weights": TieredWeights,       // [Mandatory] || The weights for the owning object, relative to other objects of the same type.
    "constraints": Constraints,     // [Optional]  || Constraints that may remove the owning object from the available pool. Defaults to no constraints.
    "name": Component,              // [Optional]  || The entity's name. Either a literal string, a lang key, or a text component. Default value = no name.
    "bonus_loot": BonusLootTables,  // [Optional]  || Bonus loot tables that will be rolled when the entity is killed. Default value = empty list.
    "valid_gear_sets": {            // [Optional]  || Per-tier lists of set predicates controlling which gear sets may be equipped. Predicates in a list are logically OR'd. Default value = empty map.
        WorldTier: [
            SetPredicate
        ]
    },
    "nbt": CompoundTag,             // [Optional]  || Entity NBT applied to the spawned mob. Default value = no NBT.
    "mount": SupportingEntity,      // [Optional]  || A mount that the mob will ride. Default value = no mount.
    "supporting_entities": [        // [Optional]  || A list of entities spawned alongside the mob. Default value = empty list.
        SupportingEntity
    ],
    "finalize": boolean,            // [Optional]  || If vanilla spawn finalization will be run for the mob. Default value = false.
    "spawn_conditions": [           // [Optional]  || Conditions required for the mob to spawn. Multiple conditions are combined with logical AND. Default value = empty list.
        SpawnCondition
    ]
}
```

The special name string `"use_name_generation"` invokes Apotheosis's random name generator instead of setting a fixed name.  

Note: If no gear sets are provided for the active world tier, no equipment will be applied.

# Examples
Basic boss data for an overworld ranged invader, which is available in the summit and pinnacle tiers, uses a generated name, and drops two bonus loot tables.

```json
{
    "bonus_loot": [
        "apotheosis:entity/boss_drops",
        "apotheosis:entity/rare_boss_drops"
    ],
    "constraints": {
        "dimensions": [
            "minecraft:overworld"
        ]
    },
    "name": "use_name_generation",
    "valid_gear_sets": {
        "summit": [
            "#summit_ranged"
        ],
        "pinnacle": [
            "#pinnacle_ranged"
        ]
    },
    "weights": {
        "summit": {
            "quality": 1.5,
            "weight": 40
        },
        "pinnacle": {
            "quality": 1.5,
            "weight": 40
        }
    }
}
```
