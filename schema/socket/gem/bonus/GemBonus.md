# Description
A Gem Bonus is the effect provided by a socketed gem. When a gem is socketed into an item, its bonus provides various benefits to the item. Gem bonuses can enhance attributes, add special abilities, or provide unique effects to the socketed item.

Gem Bonuses are subtyped, meaning each subtype declares a `"type"` key and its own parameters.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Subtypes
The following subtypes of Gem Bonus are available:

## Basic Bonuses
1. [AttributeBonus](./AttributeBonus.md) - Applies a single attribute modifier to the socketed item
2. [MultiAttrBonus](./MultiAttrBonus.md) - Applies multiple attribute modifiers to the socketed item
3. [DurabilityBonus](./DurabilityBonus.md) - Increases the durability of the socketed item
4. [DamageReductionBonus](./DamageReductionBonus.md) - Reduces damage taken when the socketed item is worn
5. [EnchantmentBonus](./EnchantmentBonus.md) - Increases the level of enchantments on the socketed item
6. [MobEffectBonus](./MobEffectBonus.md) - Applies mob effects when certain actions occur

## Special Bonuses
1. [AllStatsBonus](./special/AllStatsBonus.md) - Increases all basic stats of the socketed item
2. [BloodyArrowBonus](./special/BloodyArrowBonus.md) - Arrows cause bleeding damage over time
3. [DropTransformBonus](./special/DropTransformBonus.md) - Transforms certain item drops into other items
4. [FrozenDropsBonus](./special/FrozenDropsBonus.md) - Prevents item drops from despawning
5. [LeechBlockBonus](./special/LeechBlockBonus.md) - Restores health when breaking blocks
6. [MageSlayerBonus](./special/MageSlayerBonus.md) - Deals extra damage to magic-using enemies
7. [OmneticBonus](./special/OmneticBonus.md) - Allows tools to function as other tool types
8. [RadialBonus](./special/RadialBonus.md) - Allows mining blocks in an area around the target block
