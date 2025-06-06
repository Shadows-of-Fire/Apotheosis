# Description
An Elite represents a configuration for transforming regular mobs into minibosses in the world. Elites are applied to existing entities with a chance to convert them into more powerful versions with special equipment, abilities, and loot.

# Dependencies
This object references the following objects:
1. [BasicBossData](./BasicBossData.md)
2. [BossStats](./BossStats.md)
3. [AffixData](./AffixData.md)
4. [EntityType](../../../../../Minecraft/blob/-/schema/EntityType.md)
5. [TagKey](../../../../../Minecraft/blob/-/schema/TagKey.md)

# Schema
```js
{
    "basic_data": BasicBossData,         // [Mandatory] || Basic boss information like weights, constraints, name, loot, etc.
    "success_chance": float,             // [Mandatory] || The chance (0-1) that a matching entity will be converted to an Elite.
    "entities": [                        // [Mandatory] || The entities this Elite configuration can apply to.
        EntityType                      
    ],
    "stats": BossStats,                  // [Mandatory] || Stats for the Elite, including attribute modifiers and effects.
    "affix_data": AffixData              // [Optional]  || Configuration for applying affixes to the Elite's equipment. Defaults to no affixes.
}
```

# Examples

## Basic Zombie Elite
A basic configuration that has a 10% chance to convert zombies into minibosses with enhanced stats.

```json
{
    "basic_data": {
        "weights": {
            "frontier": {
                "quality": 0.1,
                "weight": 100
            },
            "ascent": {
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
            ]
        }
    },
    "success_chance": 0.1,
    "entities": [
        "minecraft:zombie"
    ],
    "stats": {
        "enchant_chance": 0.7,
        "enchantment_levels": {
            "primary": 30,
            "secondary": 20
        },
        "effects": [
            {
                "chance": 1.0,
                "effect": "minecraft:resistance",
                "amplifier": 1
            }
        ],
        "attribute_modifiers": [
            {
                "attribute": "minecraft:generic.max_health",
                "operation": "add_value",
                "value": {
                    "min": 10.0,
                    "max": 20.0
                }
            },
            {
                "attribute": "minecraft:generic.attack_damage",
                "operation": "add_multiplied_base",
                "value": {
                    "min": 0.2,
                    "max": 0.5
                }
            }
        ]
    },
    "affix_data": {
        "affix_chance": 0.5,
        "rarities": [
            "uncommon",
            "rare"
        ]
    }
}
```

## Advanced Skeleton Archer Elite
A more complex configuration for skeleton archers with a higher chance to spawn in the Pinnacle tier with guaranteed mythic rarity equipment.

```json
{
    "basic_data": {
        "weights": {
            "summit": {
                "quality": 0.3,
                "weight": 50
            },
            "pinnacle": {
                "quality": 0.5,
                "weight": 100
            }
        },
        "constraints": {
            "dimensions": [
                "minecraft:overworld"
            ],
            "biomes": {
                "type": "whitelist",
                "values": [
                    "#minecraft:is_forest"
                ]
            }
        },
        "name": "Forest Archer",
        "bonus_loot": {
            "tables": [
                "apotheosis:treasure/boss_drops",
                "apotheosis:treasure/rare_boss_drops"
            ]
        },
        "valid_gear_sets": {
            "summit": [
                "rare_ranged",
                "epic_ranged"
            ],
            "pinnacle": [
                "epic_ranged",
                "mythic_ranged"
            ]
        },
        "nbt": {
            "Silent": 1,
            "PersistenceRequired": 1
        },
        "supporting_entities": [
            {
                "entity": "minecraft:skeleton",
                "x": 2,
                "y": 0,
                "z": 0
            },
            {
                "entity": "minecraft:skeleton",
                "x": -2,
                "y": 0,
                "z": 0
            }
        ],
        "finalize": true,
        "spawn_conditions": [
            {
                "type": "apotheosis:surface_type",
                "surface_type": "on_surface"
            }
        ]
    },
    "success_chance": 0.2,
    "entities": [
        "minecraft:skeleton"
    ],
    "stats": {
        "enchant_chance": 1.0,
        "enchantment_levels": {
            "primary": 50,
            "secondary": 30
        },
        "effects": [
            {
                "chance": 1.0,
                "effect": "minecraft:speed",
                "amplifier": 1
            },
            {
                "chance": 1.0,
                "effect": "minecraft:resistance",
                "amplifier": 1
            },
            {
                "chance": 0.5,
                "effect": "minecraft:invisibility",
                "amplifier": 0
            }
        ],
        "attribute_modifiers": [
            {
                "attribute": "minecraft:generic.max_health",
                "operation": "add_value",
                "value": {
                    "min": 20.0,
                    "max": 30.0
                }
            },
            {
                "attribute": "minecraft:generic.attack_damage",
                "operation": "add_multiplied_base",
                "value": {
                    "min": 0.5,
                    "max": 1.0
                }
            },
            {
                "attribute": "minecraft:generic.movement_speed",
                "operation": "add_multiplied_base",
                "value": {
                    "min": 0.2,
                    "max": 0.4
                }
            }
        ]
    },
    "affix_data": {
        "affix_chance": 1.0,
        "rarities": [
            "epic",
            "mythic"
        ]
    }
}
```
