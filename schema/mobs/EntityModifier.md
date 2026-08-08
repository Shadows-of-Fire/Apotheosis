# Description
An Entity Modifier is a change applied to a spawning mob. They are the underlying modifiers used by [Augmentations](./Augmentation.md).

# Dependencies
This object references the following objects:
1. [ChancedEffectInstance](../../../../../Placebo/blob/-/schema/ChancedEffectInstance.md)
2. [StepFunction](../../../../../Placebo/blob/-/schema/StepFunction.md)
3. [SetPredicate](../../../../../Placebo/blob/-/schema/SetPredicate.md)
4. [LootRarity](../loot/LootRarity.md)
5. [AffixLootEntry](../loot/AffixLootEntry.md)

# Subtypes
Entity Modifiers are subtyped, meaning each subtype declares a `"type"` key and its own parameters.

## Mob Effect Modifier
Applies a mob effect to the target entity.  

The effect is applied with infinite duration, unless the entity is a creeper, in which case the duration is reduced to five minutes.

### Schema
```js
{
    "type": "apotheosis:mob_effect",
    "effect": ChancedEffectInstance  // [Mandatory] || The effect to apply. Used in constant mode, meaning the chance is not read, and the amplifier only accepts an integer.
}
```

### Examples
A modifier which applies Strength I.

```json
{
    "type": "apotheosis:mob_effect",
    "effect": {
        "effect": "minecraft:strength",
        "amplifier": 0
    }
}
```

## Attribute Modifier
Applies an attribute modifier to the target entity.  

The modifier is silently ignored if the entity does not have the attribute.

### Schema
```js
{
    "type": "apotheosis:attribute",
    "attribute": "string",  // [Mandatory] || The registry name of the attribute to modify.
    "operation": "string",  // [Mandatory] || The operation of the modifier. One of "add_value", "add_multiplied_base", or "add_multiplied_total".
    "value": StepFunction   // [Mandatory] || The value of the attribute modifier.
}
```

### Examples
A modifier which increases max health by 20 to 40 points.

```json
{
    "type": "apotheosis:attribute",
    "attribute": "minecraft:generic.max_health",
    "operation": "add_value",
    "value": {
        "min": 20,
        "steps": 4,
        "step": 5
    }
}
```

## Gear Set Modifier
Applies a random gear set to the target entity, selected from the given set predicates.

### Schema
```js
{
    "type": "apotheosis:gear_set",
    "valid_gear_sets": [  // [Mandatory] || The list of set predicates controlling which gear sets may be equipped.
        SetPredicate
    ]
}
```

### Examples
A modifier which equips a random haven-tier melee gear set.

```json
{
    "type": "apotheosis:gear_set",
    "valid_gear_sets": [
        "#haven_melee"
    ]
}
```

## Random Affix Item Modifier
Generates a random affix item and equips it on the target entity, in the slot matching the item's loot category.  
The item is marked as a guaranteed drop.

### Schema
```js
{
    "type": "apotheosis:random_affix_item",
    "rarities": [  // [Optional] || A pool of rarities to select from. When empty, all rarities are used. Default value = empty list.
        LootRarity
    ],
    "entries": [   // [Optional] || A list of affix loot entry registry names to select from. When empty, all entries are used. Default value = empty list.
        "string"
    ]
}
```

### Examples
A modifier which equips a random affix item, using all available entries and rarities.

```json
{
    "type": "apotheosis:random_affix_item"
}
```
