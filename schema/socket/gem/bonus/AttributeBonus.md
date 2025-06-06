# Description
AttributeBonus is one of the most common Gem Bonus implementations, which adds a single attribute modifier to items when socketed. These modifiers can enhance properties like attack damage, attack speed, armor, movement speed, etc. The strength of the attribute modification depends on the gem's purity.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. [Attribute](../../../../../Minecraft/blob/-/schema/Attribute.md)

# Schema
```js
{
    "type": "apotheosis:attribute",      // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,               // [Mandatory] || The item types this gem can be applied to
    "attribute": Attribute,              // [Mandatory] || The attribute to modify
    "operation": Operation,              // [Mandatory] || The attribute modifier operation
    "values": {                          // [Mandatory] || Per-purity attribute values
        Purity: double
    }
}
```

The attribute operation (`operation`) must be one of the following values:
- `"add_value"` - Adds a flat value to the attribute
- `"add_multiplied_base"` - Adds a percentage of the base value
- `"add_multiplied_total"` - Adds a percentage of the total value (after other modifiers)

# Examples

## Attack Damage Gem
A gem that increases attack damage by a percentage, with stronger effects at higher purities.

```json
{
    "type": "apotheosis:attribute",
    "gem_class": "sword",
    "attribute": "minecraft:generic.attack_damage",
    "operation": "add_multiplied_base",
    "values": {
        "cracked": 0.05,
        "chipped": 0.1,
        "flawed": 0.15,
        "normal": 0.2,
        "flawless": 0.25,
        "perfect": 0.3
    }
}
```

## Armor Gem
A gem that increases armor when socketed into armor pieces.

```json
{
    "type": "apotheosis:attribute",
    "gem_class": {
        "key": "armor",
        "types": ["helmet", "chestplate", "leggings", "boots"]
    },
    "attribute": "minecraft:generic.armor",
    "operation": "add_value",
    "values": {
        "cracked": 1.0,
        "chipped": 1.5,
        "flawed": 2.0,
        "normal": 2.5,
        "flawless": 3.0,
        "perfect": 4.0
    }
}
```
