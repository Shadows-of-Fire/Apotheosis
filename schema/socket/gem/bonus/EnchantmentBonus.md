# Description
EnchantmentBonus increases the level of enchantments on an item when a gem with this bonus is socketed. The bonus can add levels to a specific enchantment, to all existing enchantments, or only to items that already have the specified enchantment.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. [Enchantment](../../../../../Minecraft/blob/-/schema/Enchantment.md)

# Schema
```js
{
    "type": "apotheosis:enchantment",     // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,                // [Mandatory] || The item types this gem can be applied to
    "enchantment": Enchantment,           // [Mandatory] || The enchantment to enhance
    "mode": Mode,                         // [Optional]  || The enhancement mode. Default: "single"
    "values": {                           // [Mandatory] || Per-purity level increases
        Purity: integer                   // Value between 1 and 127, representing levels to add
    }
}
```

Mode must be one of the following values:
- `"single"` - Adds levels to the specified enchantment, even if it didn't exist on the item
- `"existing"` - Adds levels to the specified enchantment, but only if it already exists on the item
- `"global"` - Adds levels to all existing enchantments on the item

# Examples

## Sharpness Gem
A gem that increases the level of Sharpness when socketed into weapons.

```json
{
    "type": "apotheosis:enchantment",
    "gem_class": {
        "key": "weapons",
        "types": ["sword", "axe"]
    },
    "enchantment": "minecraft:sharpness",
    "values": {
        "flawed": 1,
        "normal": 2,
        "flawless": 3,
        "perfect": 4
    }
}
```

## Protection Enhancement Gem
A gem that increases the level of all existing Protection enchantments on armor.

```json
{
    "type": "apotheosis:enchantment",
    "gem_class": {
        "key": "armor",
        "types": ["helmet", "chestplate", "leggings", "boots"]
    },
    "enchantment": "minecraft:protection",
    "mode": "existing",
    "values": {
        "normal": 1,
        "flawless": 2,
        "perfect": 3
    }
}
```

## Global Enchantment Amplifier
A gem that increases the level of all existing enchantments on an item.

```json
{
    "type": "apotheosis:enchantment",
    "gem_class": {
        "key": "tools",
        "types": ["sword", "axe", "pickaxe", "shovel", "hoe"]
    },
    "enchantment": "minecraft:unbreaking",
    "mode": "global",
    "values": {
        "flawless": 1,
        "perfect": 2
    }
}
```
