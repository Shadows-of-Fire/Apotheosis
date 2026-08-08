# Description
A Rarity Override replaces the default loot rules of one or more [Loot Rarities](./LootRarity.md) for a single loot category.  

When an item is reforged, the game first checks if a rarity override exists for the item's category. If the override specifies rules for the target rarity, those rules are used instead of the rarity's own rules.  

The target loot category is determined by the file path. A file at `data/<datapack namespace>/rarity_override/<category namespace>/<category path>.json` applies to the category `<category namespace>:<category path>`. For example, a file at `data/mypack/rarity_override/apotheosis/bow.json` applies to `apotheosis:bow`.  

Only one override may exist per category. If multiple files target the same category, a warning is logged and only one of them will apply.

# Dependencies
This object references the following objects:
1. [LootCategory](./LootCategory.md)
2. [LootRarity](./LootRarity.md)
3. [LootRule](./LootRule.md)

# Schema
```js
{
    "category": LootCategory,  // [Mandatory] || The category this override applies to. Must match the category specified by the file path, or the file will fail to load.
    "overrides": {             // [Mandatory] || A map from rarity registry name to the replacement loot rules for that rarity. Rarities that are omitted continue using their normal rules.
        LootRarity: [
            LootRule
        ]
    }
}
```

# Examples
A trimmed version of the shears override, which replaces the reforging rules for the common and mythic rarities when reforging shears.

```json
{
    "type": "apotheosis:rarity_override",
    "category": "apotheosis:shears",
    "overrides": {
        "apotheosis:common": [
            {
                "type": "apotheosis:socket",
                "min": 0,
                "max": 1
            },
            {
                "type": "apotheosis:affix",
                "affix_type": "stat"
            },
            {
                "type": "apotheosis:durability",
                "min": 0.05,
                "max": 0.1
            }
        ],
        "apotheosis:mythic": [
            {
                "type": "apotheosis:socket",
                "min": 1,
                "max": 2
            },
            {
                "type": "apotheosis:affix",
                "affix_type": "stat"
            },
            {
                "type": "apotheosis:affix",
                "affix_type": "basic_effect"
            },
            {
                "type": "apotheosis:affix",
                "affix_type": "basic_effect"
            },
            {
                "type": "apotheosis:select",
                "chance": 0.99,
                "if_false": {
                    "type": "apotheosis:component",
                    "components": {
                        "!apotheosis:durability_bonus": {},
                        "minecraft:unbreakable": {}
                    }
                },
                "if_true": {
                    "type": "apotheosis:durability",
                    "min": 0.45,
                    "max": 0.75
                }
            }
        ]
    }
}
```
