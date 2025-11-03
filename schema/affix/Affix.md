# Description
An Affix is a powerful enchantment-like modifier that can be applied to items, providing various bonuses and special abilities. Affixes are central to the Apotheosis loot system, enabling the creation of tiered magic items with unique properties.

Unlike traditional enchantments, Affixes use a float level from 0 to 1 (sometimes up to 2.0) that defines their relative power level. This allows for more granular scaling of effects compared to the integer-based levels of enchantments.

# Dependencies
This object references the following objects:
1. [AffixDefinition](./AffixDefinition.md)
2. [LootCategory](../loot/LootCategory.md)
3. [LootRarity](../loot/LootRarity.md)

# Base Schema
All affixes share common properties defined in their definition:

```js
{
    "type": string,                       // [Mandatory] || The type of the affix (the registry name of the affix codec)
    "definition": AffixDefinition,        // [Mandatory] || The affix definition that controls type, exclusivity, and weights
    // Additional properties based on affix subtype
}
```

# Common Implementations
Affixes come in many different forms, each with their own unique schemas and functionality:

1. [AttributeAffix](./AttributeAffix.md) - Enhances item attributes like attack damage or speed
2. [CleavingAffix](./effect/CleavingAffix.md) - Allows melee attacks to hit multiple targets
3. [CatalyzingAffix](./effect/CatalyzingAffix.md) - Increases effects of potions
4. [DamageReductionAffix](./effect/DamageReductionAffix.md) - Reduces incoming damage of specific types
5. [TelepathicAffix](./effect/TelepathicAffix.md) - Instantly collects block drops
6. [ThunderstruckAffix](./effect/ThunderstruckAffix.md) - Summons lightning when attacking
7. [SpectralShotAffix](./effect/SpectralShotAffix.md) - Makes projectiles pass through blocks

Each of these affixes has its own specific schema documentation that details its unique properties and functionality.
