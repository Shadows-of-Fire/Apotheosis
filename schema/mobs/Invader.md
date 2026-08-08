# Description
An Invader is a preset entity with per-rarity stats that spawns with a full gear set and an equipped affix item.  

Invaders piggyback off of normal mob spawning, cancelling the original spawn and spawning the invader in its place, rather than using their own spawn mechanism.  
The per-dimension spawn chances and rules are controlled by the [Invader Spawn Rules](./InvaderSpawnRules.md) data map.  

When an invader spawns, a rarity is randomly selected from its stats map, and the stats for that rarity are applied.  
One equipped item is then converted into an affix item of the selected rarity, marked as a guaranteed drop, and the invader is renamed using the rarity color.

# Dependencies
This object references the following objects:
1. [BasicBossData](./BasicBossData.md)
2. [LootRarity](../loot/LootRarity.md)
3. [BossStats](./BossStats.md)
4. [InvaderSpawnRules](./InvaderSpawnRules.md)

# Schema
```js
{
    "basic_data": BasicBossData,  // [Mandatory] || Common boss data, such as weights, constraints, name, and gear sets.
    "entity": "string",           // [Mandatory] || Registry name of the entity type to spawn.
    "size": {                     // [Mandatory] || The size of the spawned entity, used to check that it fits at the target position. Should account for any mounts or supporting entities.
        "width": float,           // [Mandatory] || The width of the bounding box.
        "height": float           // [Mandatory] || The height of the bounding box.
    },
    "stats": {                    // [Mandatory] || A map of per-rarity boss stats. An invader may only generate with the rarities specified here.
        LootRarity: BossStats
    }
}
```

Note: If the basic data does not provide a gear set for the active world tier, the invader will instead generate and equip a single random affix item.

# Examples
A trimmed version of the Breeze invader, which spawns in the overworld during the summit and pinnacle tiers, and always generates with the mythic rarity.

```json
{
    "type": "apotheosis:invader",
    "basic_data": {
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
    },
    "entity": "minecraft:breeze",
    "size": {
        "height": 3.6,
        "width": 1.2
    },
    "stats": {
        "apotheosis:mythic": {
            "attribute_modifiers": [
                {
                    "attribute": "minecraft:generic.max_health",
                    "operation": "add_value",
                    "value": {
                        "min": 90.0,
                        "max": 140.0
                    }
                },
                {
                    "attribute": "minecraft:generic.armor",
                    "operation": "add_value",
                    "value": 12.0
                }
            ],
            "effects": [
                {
                    "amplifier": 1.0,
                    "effect": "minecraft:fire_resistance"
                }
            ],
            "enchant_chance": 1.0,
            "enchantment_levels": {
                "primary": 100,
                "secondary": 80
            }
        }
    }
}
```
