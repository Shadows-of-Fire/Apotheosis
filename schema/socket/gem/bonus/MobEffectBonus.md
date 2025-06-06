# Description
MobEffectBonus applies mob effects (potions) to entities based on specific triggers. These bonuses can apply effects to the gem holder, the target, or both when certain actions occur such as hitting an entity, being hit, etc.

# Dependencies
This object references the following objects:
1. [GemBonus](./GemBonus.md)
2. [GemClass](../GemClass.md)
3. [Purity](../Purity.md)
4. [MobEffect](../../../../../Minecraft/blob/-/schema/MobEffect.md)

# Schema
```js
{
    "type": "apotheosis:mob_effect",      // [Mandatory] || The bonus type identifier
    "gem_class": GemClass,                // [Mandatory] || The item types this gem can be applied to
    "mob_effect": MobEffect,              // [Mandatory] || The mob effect to apply
    "target": Target,                     // [Mandatory] || The target of the mob effect
    "values": {                           // [Mandatory] || Per-purity effect data
        Purity: {
            "duration": integer,           // [Mandatory] || The duration of the effect in ticks
            "amplifier": integer,          // [Mandatory] || The amplifier of the effect (level - 1)
            "cooldown": integer            // [Optional]  || The cooldown in ticks. Default: 0
        }
    },
    "stack_on_reapply": boolean,          // [Optional]  || Whether the effect stacks on reapplication. Default: false
    "stacking_limit": integer             // [Optional]  || The maximum number of stacks. Default: 255
}
```

Target must be one of the following values:
- `"attack_self"` - Applies the effect to the holder when they attack
- `"attack_target"` - Applies the effect to the target when the holder attacks them
- `"hurt_self"` - Applies the effect to the holder when they are hurt
- `"hurt_attacker"` - Applies the effect to the attacker when the holder is hurt
- `"break_self"` - Applies the effect to the holder when they break a block
- `"arrow_self"` - Applies the effect to the holder when an arrow they shot hits
- `"arrow_target"` - Applies the effect to the target when hit by an arrow
- `"block_self"` - Applies the effect to the holder when blocking with a shield
- `"block_attacker"` - Applies the effect to the attacker when the holder blocks with a shield
- `"projectile_self"` - Applies the effect to the holder when any projectile they shot hits
- `"projectile_target"` - Applies the effect to the target when hit by any projectile

# Examples

## Strength Gem
A gem that gives the holder Strength when they attack.

```json
{
    "type": "apotheosis:mob_effect",
    "gem_class": "sword",
    "mob_effect": "minecraft:strength",
    "target": "attack_self",
    "values": {
        "flawed": {
            "duration": 100,
            "amplifier": 0
        },
        "normal": {
            "duration": 100,
            "amplifier": 1
        },
        "flawless": {
            "duration": 100,
            "amplifier": 2
        },
        "perfect": {
            "duration": 140,
            "amplifier": 2
        }
    }
}
```

## Poison Gem
A gem that poisons enemies when hit, with a cooldown.

```json
{
    "type": "apotheosis:mob_effect",
    "gem_class": {
        "key": "weapons",
        "types": ["sword", "axe", "trident"]
    },
    "mob_effect": "minecraft:poison",
    "target": "attack_target",
    "values": {
        "normal": {
            "duration": 80,
            "amplifier": 0,
            "cooldown": 100
        },
        "flawless": {
            "duration": 100,
            "amplifier": 1,
            "cooldown": 80
        },
        "perfect": {
            "duration": 120,
            "amplifier": 1,
            "cooldown": 60
        }
    }
}
```

## Stacking Slowness Gem
A gem that applies stacking slowness when blocking attacks with a shield.

```json
{
    "type": "apotheosis:mob_effect",
    "gem_class": "shield",
    "mob_effect": "minecraft:slowness",
    "target": "block_attacker",
    "stack_on_reapply": true,
    "stacking_limit": 3,
    "values": {
        "flawless": {
            "duration": 60,
            "amplifier": 0
        },
        "perfect": {
            "duration": 80,
            "amplifier": 0
        }
    }
}
```
