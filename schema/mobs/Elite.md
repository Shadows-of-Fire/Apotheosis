# Description
An Elite is an upgraded variant of a normal mob, converted from a natural spawn.  

When a naturally spawning mob matches the entity list of an elite, and the success chance roll passes, the mob is transformed: its stats are applied, its name and gear set are set from the basic data, and any mounts or supporting entities are spawned alongside it.  

Unlike Invaders, elites use a single stats object, and only generate an affix item if the affix data allows it.

# Dependencies
This object references the following objects:
1. [BasicBossData](./BasicBossData.md)
2. [BossStats](./BossStats.md)
3. [AffixData](./AffixData.md)

# Schema
```js
{
    "basic_data": BasicBossData,  // [Mandatory] || Common boss data, such as weights, constraints, name, and gear sets.
    "success_chance": float,      // [Mandatory] || The chance that a matching spawn is converted into this elite, after it has been selected. Range: [0, 1].
    "entities": HolderSet,        // [Mandatory] || The entity types this elite may apply to. Either a single entity type registry name, a list of names, or a #-prefixed tag name.
    "stats": BossStats,           // [Mandatory] || The stats applied to the converted mob.
    "affix_data": AffixData       // [Optional]  || Controls if one of the elite's items becomes an affix item. Defaults to no affix item.
}
```

# Examples
Craig, an elite goat that spawns in the overworld during the pinnacle tier, accompanied by a jeb_ sheep.

```json
{
    "type": "apotheosis:elite",
    "basic_data": {
        "constraints": {
            "dimensions": [
                "minecraft:overworld"
            ]
        },
        "name": {
            "color": "rainbow",
            "translate": "elite.apotheosis.craig"
        },
        "nbt": {
            "HasLeftHorn": 1,
            "HasRightHorn": 0,
            "IsScreamingGoat": 1
        },
        "spawn_conditions": [
            {
                "type": "apotheosis:not",
                "spawn_condition": {
                    "type": "apotheosis:spawn_type",
                    "spawn_types": [
                        "spawn_egg"
                    ]
                }
            }
        ],
        "supporting_entities": [
            {
                "entity": "minecraft:sheep",
                "nbt": {
                    "CustomName": "jeb_"
                }
            }
        ],
        "valid_gear_sets": {
            "pinnacle": [
                "#pinnacle_melee"
            ]
        },
        "weights": {
            "pinnacle": {
                "quality": 0.1,
                "weight": 100
            }
        }
    },
    "entities": "minecraft:goat",
    "stats": {
        "attribute_modifiers": [
            {
                "attribute": "minecraft:generic.max_health",
                "operation": "add_value",
                "value": 4096.0
            },
            {
                "attribute": "minecraft:generic.attack_damage",
                "operation": "add_multiplied_total",
                "value": {
                    "min": 4.0,
                    "max": 10.0
                }
            },
            {
                "attribute": "minecraft:generic.scale",
                "operation": "add_value",
                "value": 3.0
            }
        ],
        "effects": [
            {
                "amplifier": 1.0,
                "effect": "minecraft:fire_resistance"
            },
            {
                "amplifier": 1.0,
                "effect": "minecraft:glowing"
            }
        ],
        "enchant_chance": 1.0,
        "enchantment_levels": {
            "primary": 100,
            "secondary": 100
        }
    },
    "success_chance": 0.005
}
```
