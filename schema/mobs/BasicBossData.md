# Description
Basic boss information, shared between `Elite` and `Invader` entity types. This data structure defines how boss entities are configured, including their attributes, gear, supporting entities, and spawn conditions.

# Dependencies
This object references the following objects:
1. [TieredWeights](../tier/TieredWeights.md)
2. [Constraints](../tier/Constraints.md)
3. [WorldTier](../tier/WorldTier.md)
4. [SpawnCondition](../util/SpawnCondition.md)
5. [SupportingEntity](./SupportingEntity.md)
6. [Component](../../../../../Minecraft/blob/-/schema/Component.md)
7. [CompoundTag](../../../../../Placebo/blob/-/schema/CompoundTag.md)
8. [SetPredicate](../../../../../Placebo/blob/-/schema/gear/SetPredicate.md)
9. [BonusLootTables](../loot/BonusLootTables.md)

# Schema
```js
{
    "weights": TieredWeights,                // [Mandatory] || The weights for this element, relative to other objects of the same type.
    "constraints": Constraints,              // [Optional]  || Application constraints that may remove this element from the available pool. Defaults to no constraints.
    "name": Component,                       // [Optional]  || The entity name. May be a lang key. Empty or null will cause no name to be set. The special string "use_name_generation" will invoke name generation.
    "bonus_loot": BonusLootTables,           // [Optional]  || Any bonus loot tables that will be dropped by the entity.
    "valid_gear_sets": {                     // [Optional]  || Per-tier lists of SetPredicates controlling what gear sets may be applied.
        WorldTier: [                         // Not providing an entry for a tier will not equip anything. Individual predicates are logically OR'd.
            SetPredicate
        ]
    },
    "nbt": CompoundTag,                      // [Optional]  || Entity NBT to apply to the target mob.
    "mount": SupportingEntity,               // [Optional]  || An optional SupportingEntity that the target entity will start riding.
    "supporting_entities": [                 // [Optional]  || A list of entities to spawn alongside the entity.
        SupportingEntity
    ],
    "finalize": boolean,                     // [Optional]  || If Mob#finalizeSpawn will be called for the target entity. Defaults to false.
    "spawn_conditions": [                    // [Optional]  || Spawn conditions that are required for the boss to spawn. Multiple conditions are combined via logical AND.
        SpawnCondition
    ]
}
```

# Examples

## Basic Zombie Boss

```json
{
    "weights": {
        "frontier": {
            "quality": 0.1,
            "weight": 100
        },
        "ascent": {
            "quality": 0.1,
            "weight": 100
        },
        "summit": {
            "quality": 0.1,
            "weight": 100
        },
        "pinnacle": {
            "quality": 0.1,
            "weight": 100
        }
    },
    "constraints": {
        "dimensions": [
            "minecraft:overworld"
        ]
    },
    "name": "use_name_generation",
    "bonus_loot": {
        "tables": [
            "apotheosis:treasure/boss_drops"
        ]
    },
    "valid_gear_sets": {
        "frontier": [
            "common_melee",
            "uncommon_melee"
        ],
        "ascent": [
            "uncommon_melee",
            "rare_melee" 
        ],
        "summit": [
            "rare_melee",
            "epic_melee"
        ],
        "pinnacle": [
            "epic_melee",
            "mythic_melee"
        ]
    }
}
```

## Advanced Boss with Mount and Supporting Entities

```json
{
    "weights": {
        "pinnacle": {
            "quality": 0.5,
            "weight": 50
        }
    },
    "constraints": {
        "dimensions": [
            "minecraft:the_end"
        ]
    },
    "name": "The End Boss",
    "bonus_loot": {
        "tables": [
            "apotheosis:treasure/boss_drops",
            "apotheosis:treasure/rare_boss_drops"
        ]
    },
    "valid_gear_sets": {
        "pinnacle": [
            "mythic_ranged"
        ]
    },
    "nbt": {
        "Invulnerable": 0,
        "AbsorptionAmount": 20.0
    },
    "mount": {
        "entity": "minecraft:enderman",
        "nbt": {
            "Glowing": 1
        },
        "x": 0,
        "y": 0,
        "z": 0
    },
    "supporting_entities": [
        {
            "entity": "minecraft:endermite",
            "x": 2,
            "y": 0,
            "z": 0
        },
        {
            "entity": "minecraft:endermite",
            "x": -2,
            "y": 0,
            "z": 0
        },
        {
            "entity": "minecraft:endermite",
            "x": 0,
            "y": 0,
            "z": 2
        },
        {
            "entity": "minecraft:endermite",
            "x": 0,
            "y": 0,
            "z": -2
        }
    ],
    "finalize": true,
    "spawn_conditions": [
        {
            "type": "apotheosis:surface_type",
            "surface_type": "on_surface"
        }
    ]
}
```
