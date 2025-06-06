# Description
BonusLootTables defines additional loot tables that can be dropped by an entity. When an entity with bonus loot tables is killed, each specified loot table will be rolled and the resulting items will be added to the entity's normal drops.

# Dependencies
This object references the following objects:
1. [ResourceKey](../../../../../Minecraft/blob/-/schema/ResourceKey.md)
2. [LootTable](../../../../../Minecraft/blob/-/schema/LootTable.md)

# Schema
```js
{
    "tables": [                   // [Mandatory] || The list of loot table resource keys
        ResourceKey<LootTable>
    ]
}
```

# Examples

## Basic Boss Drops
Configure an entity to drop items from the basic boss drops loot table.

```json
{
    "tables": [
        "apotheosis:treasure/boss_drops"
    ]
}
```

## Multiple Loot Tables
Configure an entity to drop items from multiple loot tables.

```json
{
    "tables": [
        "apotheosis:treasure/boss_drops",
        "apotheosis:treasure/rare_boss_drops",
        "minecraft:chests/end_city_treasure"
    ]
}
```
