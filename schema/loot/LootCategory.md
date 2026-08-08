# Description
A Loot Category classifies an item for the affix loot system. The category of an item determines which affixes and gem bonuses may apply to it, and which equipment slots those bonuses are active in.  

Loot Categories are provided by a built-in registry, which means new categories can only be added by mods, not by datapacks.  

The category of an item is resolved as follows:
1. If the item has an entry in the [Loot Category Overrides](#loot-category-overrides) data map, that category is used.
2. Otherwise, each category checks the item in priority order, and the first matching category is used.
3. If no categories match, the item resolves to `apotheosis:none`, and is not eligible for affixes.

# Schema
In JSON, a loot category is represented as a single string.

```js
"string" // [Mandatory] || The registry name of the loot category.
```

If the namespace is omitted, it will default to `apotheosis` instead of `minecraft`.  

The list of built-in categories is as follows:

1. `bow` - Bows and crossbows. Active in either hand.
2. `breaker` - Items that can dig like a pickaxe or a shovel. Active in the main hand.
3. `helmet` - Items equippable in the head slot, excluding skulls and carved pumpkins. Active in the head slot.
4. `chestplate` - Items equippable in the chest slot. Active in the chest slot.
5. `leggings` - Items equippable in the legs slot. Active in the legs slot.
6. `boots` - Items equippable in the feet slot. Active in the feet slot.
7. `shield` - Items that can block like a shield. Active in either hand.
8. `trident` - Tridents. Active in the main hand.
9. `melee_weapon` - Items that can dig like a sword, or that provide more than one attack damage. Active in the main hand.
10. `shears` - Items that can dig like shears. Active in the main hand.
11. `none` - The fallback category, indicating an item is not eligible for affixes.

Categories are checked in ascending priority order. Most categories have a priority of 1000, while `melee_weapon` (2000) and `shears` (2500) are intentionally checked after the others.  

Note: Most consumers of this object will reject `apotheosis:none`. The only place it is accepted is the Loot Category Overrides data map, where it excludes an item from the affix loot system.

# Loot Category Overrides
The `apotheosis:loot_category_overrides` data map allows specifying a per-item loot category, bypassing the normal item checks.  

Overrides are placed in the file `data/<namespace>/data_maps/item/loot_category_overrides.json`, following the standard NeoForge data map format.

## Examples
An override forcing Iron Swords to be melee weapons, and excluding Shulker Shells from the affix loot system.
```json
{
    "values": {
        "minecraft:iron_sword": "apotheosis:melee_weapon",
        "minecraft:shulker_shell": "apotheosis:none"
    }
}
```
