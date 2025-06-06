# Description
MultiAttrBonus is a complex gem bonus that applies multiple attribute modifiers to a single item. Unlike standard attribute bonuses, this allows for creating gems that affect multiple attributes simultaneously, with specific value ranges for each attribute and a custom description format.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. [Attribute](../../../../../Minecraft/blob/-/schema/Attribute.md)

# Schema
```js
{
    "type": "apotheosis:multi_attribute", // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,                // [Mandatory] || The item types this gem can be applied to
    "modifiers": [                        // [Mandatory] || The list of attribute modifiers
        {
            "attribute": Attribute,       // [Mandatory] || The attribute to modify
            "operation": Operation,       // [Mandatory] || The attribute modifier operation
            "values": {                   // [Mandatory] || Per-purity attribute values
                Purity: float
            }
        }
    ],
    "desc": string                        // [Mandatory] || The description format string with %s placeholders for each modifier
}
```

The attribute operation (`operation`) must be one of the following values:
- `"add_value"` - Adds a flat value to the attribute
- `"add_multiplied_base"` - Adds a percentage of the base value
- `"add_multiplied_total"` - Adds a percentage of the total value (after other modifiers)

# Examples

## Dual Attribute Gem
A gem that increases both attack damage and attack speed.

```json
{
    "type": "apotheosis:multi_attribute",
    "gem_class": "sword",
    "modifiers": [
        {
            "attribute": "minecraft:generic.attack_damage",
            "operation": "add_multiplied_base",
            "values": {
                "flawed": 0.10,
                "normal": 0.15,
                "flawless": 0.20,
                "perfect": 0.25
            }
        },
        {
            "attribute": "minecraft:generic.attack_speed",
            "operation": "add_value",
            "values": {
                "flawed": 0.2,
                "normal": 0.3,
                "flawless": 0.4,
                "perfect": 0.5
            }
        }
    ],
    "desc": "Increases damage and attack speed by %s and %s"
}
```

## Protection Gem
A gem that adds both armor and armor toughness when socketed.

```json
{
    "type": "apotheosis:multi_attribute",
    "gem_class": {
        "key": "armor",
        "types": ["helmet", "chestplate", "leggings", "boots"]
    },
    "modifiers": [
        {
            "attribute": "minecraft:generic.armor",
            "operation": "add_value",
            "values": {
                "normal": 2.0,
                "flawless": 3.0,
                "perfect": 4.0
            }
        },
        {
            "attribute": "minecraft:generic.armor_toughness",
            "operation": "add_value",
            "values": {
                "normal": 1.0,
                "flawless": 1.5,
                "perfect": 2.0
            }
        }
    ],
    "desc": "Increases armor by %s and armor toughness by %s"
}
```
