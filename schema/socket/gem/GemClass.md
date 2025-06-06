# Description
A Gem Class defines the set of item types that a gem can be applied to. This comes in the form of a named group of LootCategories.

# Dependencies
This object references the following objects:
1. [LootCategory](../../loot/LootCategory.md)

# Schema
Gem Classes can be specified in two ways:

## Simple Form
When the gem can only be applied to a single item category, you can use a simple string.

```js
LootCategory    // A single loot category string like "sword" or "helmet"
```

## Extended Form
When the gem can be applied to multiple item categories, use the object form.

```js
{
    "key": string,           // [Mandatory] || A unique identifier for this gem class
    "types": [              // [Mandatory] || The list of item categories this gem can be applied to
        LootCategory
    ]
}
```

# Examples

## Single Category
A gem that can only be applied to swords.

```json
"sword"
```

## Multiple Categories
A gem that can be applied to all weapons.

```json
{
    "key": "weapons",
    "types": [
        "sword",
        "axe",
        "trident",
        "bow",
        "crossbow"
    ]
}
```

## Armor Set
A gem that can be applied to all armor pieces.

```json
{
    "key": "armor_set",
    "types": [
        "helmet",
        "chestplate",
        "leggings",
        "boots"
    ]
}
```
