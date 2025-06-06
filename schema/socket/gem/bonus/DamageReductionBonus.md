# Description
DamageReductionBonus reduces incoming damage of a specific type when an item with this gem socketed is equipped. This makes armor or other equipped items more effective against specific damage sources like fire, magic, or physical damage.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. DamageType (Defined in this document)

# Schema
```js
{
    "type": "apotheosis:damage_reduction", // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,                 // [Mandatory] || The item types this gem can be applied to
    "damage_type": DamageType,             // [Mandatory] || The type of damage to reduce
    "values": {                            // [Mandatory] || Per-purity damage reduction percentages
        Purity: float                      // Value between 0 and 1, representing percentage of damage reduced
    }
}
```

DamageType must be one of the following values:
- `"physical"` - Physical damage (melee, etc.)
- `"magic"` - Magical damage (potions, certain mob abilities)
- `"fire"` - Fire damage (lava, fire blocks)
- `"fall"` - Fall damage
- `"explosion"` - Explosion damage
- `"projectile"` - Projectile damage (arrows, etc.)
- `"lightning"` - Lightning damage

# Examples

## Fire Protection Gem
A gem that reduces fire damage when socketed into armor.

```json
{
    "type": "apotheosis:damage_reduction",
    "gem_class": {
        "key": "armor",
        "types": ["helmet", "chestplate", "leggings", "boots"]
    },
    "damage_type": "fire",
    "values": {
        "chipped": 0.10,
        "flawed": 0.15,
        "normal": 0.20,
        "flawless": 0.25,
        "perfect": 0.30
    }
}
```

## Projectile Protection Gem
A gem that reduces damage from projectiles.

```json
{
    "type": "apotheosis:damage_reduction",
    "gem_class": {
        "key": "armor",
        "types": ["helmet", "chestplate", "leggings", "boots"]
    },
    "damage_type": "projectile",
    "values": {
        "normal": 0.15,
        "flawless": 0.20,
        "perfect": 0.25
    }
}
```
