# Description
DropTransformBonus is a special gem bonus that transforms dropped items matching specific criteria into different items. When an entity drops loot, this bonus has a chance to convert specified items into different ones based on loot conditions.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. LootItemCondition - Minecraft's loot condition system

# Schema
```js
{
    "type": "apotheosis:drop_transform_bonus", // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,                    // [Mandatory] || The item types this gem can be applied to
    "conditions": LootItemCondition,          // [Mandatory] || Conditions that must be met for transformation to occur
    "inputs": Ingredient,                     // [Mandatory] || Input items that can be transformed
    "output": ItemStack,                      // [Mandatory] || The item to transform into
    "values": {                              // [Mandatory] || Per-purity chance values
        Purity: float                        // Value between 0 and 1, representing chance to transform
    },
    "desc": string                           // [Mandatory] || Translation key for the description
}
```

# Examples

## Coal to Diamond Transformation
A gem that has a chance to transform coal into diamonds when mining.

```json
{
    "type": "apotheosis:drop_transform_bonus",
    "gem_class": {
        "key": "mining/pickaxe",
        "types": ["pickaxe"]
    },
    "conditions": {
        "condition": "minecraft:match_tool",
        "predicate": {
            "enchantments": [
                {
                    "enchantment": "minecraft:fortune",
                    "levels": {
                        "min": 1
                    }
                }
            ]
        }
    },
    "inputs": {
        "items": ["minecraft:coal"]
    },
    "output": {
        "id": "minecraft:diamond"
    },
    "values": {
        "flawed": 0.05,
        "normal": 0.10,
        "flawless": 0.15,
        "perfect": 0.20
    },
    "desc": "bonus.apotheosis.coal_to_diamond.desc"
}
```

## Mob Drop Enhancement
A gem that transforms rotten flesh into leather with a chance based on purity.

```json
{
    "type": "apotheosis:drop_transform_bonus",
    "gem_class": {
        "key": "combat/melee",
        "types": ["sword", "axe"]
    },
    "conditions": {
        "condition": "minecraft:entity_properties",
        "entity": "this",
        "predicate": {
            "type": "minecraft:zombie"
        }
    },
    "inputs": {
        "items": ["minecraft:rotten_flesh"]
    },
    "output": {
        "id": "minecraft:leather"
    },
    "values": {
        "chipped": 0.15,
        "flawed": 0.25,
        "normal": 0.35,
        "flawless": 0.45,
        "perfect": 0.55
    },
    "desc": "bonus.apotheosis.flesh_to_leather.desc"
}
```
