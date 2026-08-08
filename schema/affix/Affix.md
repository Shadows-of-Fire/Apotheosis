# Description
An Affix is a construct very similar to an enchantment, providing bonuses to affix items.  

Affixes are not applied directly. Instead, [Loot Rules](../loot/LootRule.md) apply them when an affix item is generated or reforged, selecting randomly from all affixes that are applicable to the item's loot category and rarity.  

Every affix on an item is stored with a level, which is a float from 0 to 1 that defines its relative power level, compared to max. What the level means is up to the individual affix. Most affixes map the level through a per-rarity [StepFunction](../../../../../Placebo/blob/1.21/schema/StepFunction.md) to produce their final values, meaning both the rarity and the level control the strength of the affix. The level of an affix can be rerolled in the Augmenting Table.

# Dependencies
This object references the following objects:
1. [AffixDefinition](./AffixDefinition.md)

# Subtypes
Affixes are subtyped, meaning each subtype declares a `"type"` key and its own parameters.  

In addition to the type key, every affix provides a definition, which holds the information common to all affixes:

```js
{
    "type": "string",              // [Mandatory] || The type of this affix.
    "definition": AffixDefinition, // [Mandatory] || The definition of this affix, holding its affix type, exclusivities, and weights.
    ...                            // Additional type-specific parameters.
}
```

The list of affix types is as follows:

1. [Attribute Affix](./AttributeAffix.md) - Applies an attribute modifier to the item.
2. [Multi-Attribute Affix](./effect/MultiAttrAffix.md) - Applies multiple attribute modifiers with a single description.
3. [Mob Effect Affix](./effect/MobEffectAffix.md) - Applies a mob effect when an event occurs.
4. [Damage Reduction Affix](./effect/DamageReductionAffix.md) - Reduces incoming damage of a specific type.
5. [Catalyzing Affix](./effect/CatalyzingAffix.md) - Grants Strength when blocking an explosion.
6. [Cleaving Affix](./effect/CleavingAffix.md) - Grants a chance for melee attacks to strike nearby enemies.
7. [Enlightened Affix](./effect/EnlightenedAffix.md) - Allows placing torches from the tool, at the cost of durability.
8. [Executing Affix](./effect/ExecutingAffix.md) - Instantly slays enemies below a health threshold.
9. [Festive Affix](./effect/FestiveAffix.md) - Grants a chance for slain enemies to explode into bonus drops.
10. [Magical Arrow Affix](./effect/MagicalArrowAffix.md) - Makes arrows deal magic damage.
11. [Omnetic Affix](./effect/OmneticAffix.md) - Makes a tool function as an entire toolset.
12. [Psychic Affix](./effect/PsychicAffix.md) - Reflects blocked projectile damage back at the shooter.
13. [Radial Affix](./effect/RadialAffix.md) - Makes a tool mine an area of blocks.
14. [Retreating Affix](./effect/RetreatingAffix.md) - Launches the user backwards when blocking a close-range attack.
15. [Spectral Shot Affix](./effect/SpectralShotAffix.md) - Grants a chance to fire bonus spectral arrows.
16. [Stoneforming Affix](./effect/StoneformingAffix.md) - Converts mined stone-like blocks into a block of the user's choosing.
17. [Telepathic Affix](./effect/TelepathicAffix.md) - Teleports drops directly to the player.
18. [Thunderstruck Affix](./effect/ThunderstruckAffix.md) - Makes melee attacks chain damage to all nearby enemies.
19. [Enchantment Affix](./effect/EnchantmentAffix.md) - Grants bonus enchantment levels.
