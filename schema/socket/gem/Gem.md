# Gem

## Description
Gem is the main configuration object for gem items in Apotheosis. Gems are special items that can be socketed into weapons, tools, and armor to provide bonuses. Each gem has specific categories it can be applied to, and provides different bonuses depending on the purity of the gem and the item type it's socketed into.

## Dependencies
This object references the following objects:
1. [TieredWeights](../util/TieredWeights.md) - Controls the drop rates of the gem based on different tiers
2. [Constraints](../util/Constraints.md) - Defines when and where the gem can appear
3. [Purity](./Purity.md) - Determines the quality level of the gem
4. [GemBonus](./bonus/GemBonus.md) - Defines the bonuses provided by the gem when socketed
5. [LootCategory](../loot/LootCategory.md) - Determines the item types this gem can be socketed into

## Schema
```js
{
    "weights": TieredWeights,          // [Mandatory] || Controls the drop rates of the gem based on different tiers
    "constraints": Constraints,         // [Optional] || Defines when and where the gem can appear (default: empty constraints)
    "min_purity": Purity,              // [Optional] || The minimum purity this gem can have (default: "cracked")
    "bonuses": [                       // [Mandatory] || List of bonuses this gem provides when socketed
        GemBonus
    ],
    "unique": boolean                  // [Optional] || If true, only one of this gem can be socketed in an item (default: false)
}
```

## Examples

### Basic Gem
A standard gem that provides attribute bonuses to tools.

```json
{
    "weights": {
        "low": 10,
        "medium": 8,
        "high": 5,
        "epic": 3,
        "mythic": 1
    },
    "min_purity": "flawed",
    "bonuses": [
        {
            "type": "apotheosis:attribute",
            "gem_class": {
                "key": "tools",
                "types": ["pickaxe", "axe", "shovel", "hoe"]
            },
            "attribute": "minecraft:generic.attack_damage",
            "operation": "ADDITION",
            "values": {
                "flawed": 1.0,
                "normal": 1.5,
                "flawless": 2.0,
                "perfect": 3.0
            }
        },
        {
            "type": "apotheosis:attribute",
            "gem_class": {
                "key": "weapons",
                "types": ["sword"]
            },
            "attribute": "minecraft:generic.attack_damage",
            "operation": "ADDITION",
            "values": {
                "flawed": 2.0,
                "normal": 3.0,
                "flawless": 4.0,
                "perfect": 5.0
            }
        }
    ]
}
```

### Unique Gem with Constraints
A unique gem that can only be found in certain locations and provides special bonuses.

```json
{
    "weights": {
        "epic": 5,
        "mythic": 10
    },
    "constraints": {
        "dimensions": ["minecraft:the_nether"],
        "structures": ["minecraft:fortress"],
        "biomes": {"type": "minecraft:is_nether"}
    },
    "min_purity": "normal",
    "bonuses": [
        {
            "type": "apotheosis:durability",
            "gem_class": {
                "key": "armor",
                "types": ["helmet", "chestplate", "leggings", "boots"]
            },
            "values": {
                "normal": 0.30,
                "flawless": 0.45,
                "perfect": 0.60
            }
        },
        {
            "type": "apotheosis:damage_reduction",
            "gem_class": {
                "key": "armor",
                "types": ["helmet", "chestplate", "leggings", "boots"]
            },
            "source": {"tag": "minecraft:is_fire"},
            "values": {
                "normal": 0.1,
                "flawless": 0.15,
                "perfect": 0.20
            }
        }
    ],
    "unique": true
}
```
