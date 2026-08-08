# Description
A Gem Class is a named group of loot categories, defining the set of items that a [Gem Bonus](./bonus/GemBonus.md) may be applied to.  

The key of a gem class is displayed in gem tooltips, using the lang key `gem_class.<key>`.

# Dependencies
This object references the following objects:
1. [LootCategory](../loot/LootCategory.md)

# Schema
```js
{
    "key": "string",  // [Mandatory] || The name of this gem class. Used for tooltips via the lang key "gem_class.<key>".
    "types": [        // [Mandatory] || The loot categories in this class. Must not be empty. Accepts a list of category names, or a single #-prefixed tag name.
        LootCategory
    ]
}

OR

LootCategory          // [Mandatory] || A single loot category. The key of the class will be the path of the category name.
```

# Examples
A "light weapon" gem class covering melee weapons and tridents.

```json
{
    "key": "light_weapon",
    "types": [
        "apotheosis:melee_weapon",
        "apotheosis:trident"
    ]
}
```

A gem class for bows, using the single-category form.

```json
"apotheosis:bow"
```
