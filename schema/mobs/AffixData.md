# Description
Affix Data controls if an [Elite](./Elite.md) will have one of its items converted into an affix item.  

When the chance roll passes, a random equipped item with a valid loot category is upgraded into an affix item, marked as a guaranteed drop, and the elite's name is recolored using the selected rarity.

# Dependencies
This object references the following objects:
1. [LootRarity](../loot/LootRarity.md)

# Schema
```js
{
    "affix_chance": float,  // [Mandatory] || The chance that one of the elite's items is converted into an affix item.
    "rarities": [           // [Optional]  || A pool of rarities to select from. When empty, all rarities are used. Default value = empty list.
        LootRarity
    ]
}
```

# Examples
Affix data granting a 45% chance for an affix item of any rarity.

```json
{
    "affix_chance": 0.45
}
```

Affix data granting a 25% chance for an affix item that is always rare or epic.

```json
{
    "affix_chance": 0.25,
    "rarities": [
        "apotheosis:rare",
        "apotheosis:epic"
    ]
}
```
