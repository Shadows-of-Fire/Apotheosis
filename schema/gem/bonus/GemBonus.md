# Description
A Gem Bonus is a single effect provided by a [Gem](../Gem.md). When a gem is socketed into an item, the bonus whose gem class contains the item's loot category becomes active.  

Most bonuses hold their strength in a `"values"` map keyed by [Purity](../Purity.md). A bonus only supports the purities present in its values map, and a gem cannot be socketed into a matching item unless the relevant bonus supports the gem's purity.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Subtypes
Gem Bonuses are subtyped, meaning each subtype declares a `"type"` key and its own parameters. Most subtypes also declare a gem class:

```js
{
    "type": "string",       // [Mandatory] || The type of this gem bonus.
    "gem_class": GemClass,  // [Mandatory] || The set of loot categories this bonus applies to.
    ...                     // Additional type-specific parameters.
}
```

Note: The [Bloody Arrow](./BloodyArrowBonus.md) and [Leech Block](./LeechBlockBonus.md) bonuses have fixed gem classes, and do not declare the `"gem_class"` field.

The list of gem bonus types is as follows:

1. [Attribute Bonus](./AttributeBonus.md) - Applies an attribute modifier.
2. [Multi-Attribute Bonus](./MultiAttrBonus.md) - Applies multiple attribute modifiers with a single description.
3. [Durability Bonus](./DurabilityBonus.md) - Grants a chance to ignore durability damage.
4. [Damage Reduction Bonus](./DamageReductionBonus.md) - Reduces incoming damage of a specific type.
5. [Enchantment Bonus](./EnchantmentBonus.md) - Grants bonus enchantment levels.
6. [Mob Effect Bonus](./MobEffectBonus.md) - Applies a mob effect when an event occurs.
7. [Bloody Arrow Bonus](./BloodyArrowBonus.md) - Empowers arrows at the cost of the shooter's health.
8. [Leech Block Bonus](./LeechBlockBonus.md) - Heals the user when blocking damage.
9. [All Stats Bonus](./AllStatsBonus.md) - Applies the same modifier to a list of attributes.
10. [Drop Transform Bonus](./DropTransformBonus.md) - Randomly converts certain drops into another item.
11. [Mage Slayer Bonus](./MageSlayerBonus.md) - Converts a portion of incoming magic damage into healing.
12. [Frozen Drops Bonus](./FrozenDropsBonus.md) - Grants bonus loot from kills dealt primarily via cold damage.
13. [Omnetic Bonus](./OmneticBonus.md) - Makes a tool function as an entire toolset.
14. [Radial Bonus](./RadialBonus.md) - Makes a tool mine an area of blocks.
15. [Fortification Bonus](./FortificationBonus.md) - Grants fortification shields when hurt. Requires Twilight Forest.
16. [Ore Magnet Bonus](./OreMagnetBonus.md) - Allows using the tool as an Ore Magnet. Requires Twilight Forest.
17. [Treasure Goblin Bonus](./TreasureGoblinBonus.md) - Chance to summon a Treasure Goblin when attacking. Requires Twilight Forest.
