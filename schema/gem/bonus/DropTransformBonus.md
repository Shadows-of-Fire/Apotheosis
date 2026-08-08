# Description
A Drop Transform Bonus is a [Gem Bonus](./GemBonus.md) which randomly converts certain dropped items into a different item.  

When loot is generated while the conditions match, a single roll is made against the purity's chance. On success, every dropped stack matching the input ingredient is replaced with a copy of the output item, preserving the original stack's count. The roll is made once per loot generation, so either all matching drops are converted, or none are.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)
3. [ItemStack](../../../../../../Placebo/blob/1.21/schema/ItemStack.md)

# Schema
```js
{
    "type": "apotheosis:drop_transform",
    "gem_class": GemClass,    // [Mandatory] || The set of loot categories this bonus applies to.
    "conditions": [           // [Mandatory] || A list of vanilla loot conditions which must match for the transform to occur.
        LootItemCondition
    ],
    "inputs": Ingredient,     // [Mandatory] || A vanilla ingredient matching the drops that are eligible for replacement. Must not be empty.
    "output": ItemStack,      // [Mandatory] || The replacement item. The count is overwritten with the original stack's count.
    "values": {               // [Mandatory] || The chance that the transform triggers, for each supported purity. Range: [0, 1].
        Purity: float
    },
    "desc": "string"          // [Mandatory] || A translation key for the tooltip line. Receives the chance (as a percentage) as an argument.
}
```

# Examples
A drop transform bonus for tools, which grants a chance for copper ore to drop raw gold instead of raw copper.

```json
{
    "type": "apotheosis:drop_transform",
    "conditions": [
        {
            "condition": "apotheosis:matches_block",
            "valid_blocks": "#c:ores/copper"
        }
    ],
    "desc": "gem.apotheosis:overworld/royalty.bonus.pickaxe",
    "gem_class": "apotheosis:breaker",
    "inputs": {
        "tag": "c:raw_materials/copper"
    },
    "output": {
        "count": 1,
        "id": "minecraft:raw_gold"
    },
    "values": {
        "flawed": 0.15,
        "normal": 0.2,
        "flawless": 0.25,
        "perfect": 0.4
    }
}
```
