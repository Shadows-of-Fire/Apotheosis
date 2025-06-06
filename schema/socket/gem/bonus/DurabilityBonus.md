# Description
DurabilityBonus is a gem bonus that reduces the durability damage taken by items. When a gem with this bonus is socketed into an item, the item will lose durability more slowly when used.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:durability",     // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,               // [Mandatory] || The item types this gem can be applied to
    "values": {                          // [Mandatory] || Per-purity durability bonus percentages
        Purity: float                    // Value between 0 and 1, representing percentage of durability damage ignored
    }
}
```

# Examples

## Basic Durability Gem
A gem that reduces durability damage by a percentage, with stronger effects at higher purities.

```json
{
    "type": "apotheosis:durability",
    "gem_class": {
        "key": "tools",
        "types": ["sword", "axe", "pickaxe", "shovel", "hoe"]
    },
    "values": {
        "cracked": 0.10,
        "chipped": 0.15,
        "flawed": 0.20,
        "normal": 0.25,
        "flawless": 0.35,
        "perfect": 0.50
    }
}
```

## Armor Durability Gem
A gem that reduces durability loss on armor pieces.

```json
{
    "type": "apotheosis:durability",
    "gem_class": {
        "key": "armor",
        "types": ["helmet", "chestplate", "leggings", "boots"]
    },
    "values": {
        "normal": 0.20,
        "flawless": 0.30,
        "perfect": 0.40
    }
}
```
