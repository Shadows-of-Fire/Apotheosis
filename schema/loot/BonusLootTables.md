# Description
BonusLootTables defines additional loot tables that can be dropped by an entity. When an entity with bonus loot tables is killed, each specified loot table will be rolled and the resulting items will be added to the entity's normal drops.

# Schema
```js
[
    "string"    // [Mandatory] || The list of loot table registry names.
]

