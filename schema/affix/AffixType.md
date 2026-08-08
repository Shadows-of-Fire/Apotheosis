# Description
An Affix Type is a coarse grouping of [Affixes](./Affix.md), used by [Loot Rules](../loot/LootRule.md) to select which pool of affixes will be applied to an item.  

Each affix declares its type in its [definition](./AffixDefinition.md).

# Schema
In JSON, an affix type is represented as a single string. It may take one of the following values:

1. `"stat"` - Passive stat affixes, such as attribute modifiers. Conventionally, every item receives at least one stat affix.
2. `"basic_effect"` - Minor triggered effects, weaker than full abilities.
3. `"ability"` - Powerful and unique effects, usually reserved for the higher rarities.

The meaning of each type is purely conventional. The type of an affix only controls which loot rules can select it.
