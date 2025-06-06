# Description
The MobEffectAffix applies potion effects under specific conditions, such as when attacking, being hurt, or blocking. This highly versatile affix allows for a wide range of trigger conditions and can apply effects to either the wielder or targets.

# Dependencies
This object references the following objects:
1. [Affix](../Affix.md)
2. [AffixDefinition](../AffixDefinition.md)
3. [LootCategory](../../loot/LootCategory.md)
4. [LootRarity](../../loot/LootRarity.md)
5. [MobEffect](../../../../../../Minecraft/blob/-/schema/MobEffect.md)
6. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)
7. [Target](../../util/Target.md)

# Schema
```js
{
    "type": "apotheosis:mob_effect",     // [Mandatory] || The affix type identifier
    "definition": AffixDefinition,       // [Mandatory] || The affix definition
    "mob_effect": MobEffect,             // [Mandatory] || The potion effect to apply
    "target": Target,                    // [Mandatory] || When and to whom the effect applies
    "values": {                          // [Mandatory] || Per-rarity effect configuration
        LootRarity: {
            "duration": StepFunction,    // Duration of the effect in ticks
            "amplifier": StepFunction,   // Amplifier (level) of the effect
            "cooldown": int              // [Optional] || Cooldown in ticks before the effect can be applied again (0 = no cooldown)
        }
    },
    "types": [                           // [Mandatory] || List of item categories this affix can be applied to
        LootCategory
    ],
    "stack_on_reapply": boolean,         // [Optional] || Whether the effect stacks by increasing amplifier when reapplied. Defaults to false.
    "stacking_limit": int                // [Optional] || Maximum amplifier when stacking. Defaults to 255.
}
```

The `target` must be one of the following values:
- `"attack_self"` - Applies the effect to the wielder when they attack
- `"attack_target"` - Applies the effect to the target when the wielder attacks them
- `"hurt_self"` - Applies the effect to the wielder when they are hurt
- `"hurt_attacker"` - Applies the effect to the attacker when the wielder is hurt
- `"break_self"` - Applies the effect to the wielder when they break a block
- `"arrow_self"` - Applies the effect to the wielder when their arrow hits a target
- `"arrow_target"` - Applies the effect to the target when hit by an arrow
- `"block_self"` - Applies the effect to the wielder when they block damage with a shield
- `"block_attacker"` - Applies the effect to the attacker when the wielder blocks with a shield
- `"projectile_self"` - Applies the effect to the wielder when any projectile they shot hits a target
- `"projectile_target"` - Applies the effect to the target when hit by any projectile

# Examples

## Basic Strength On Attack Affix
A basic affix that grants the wielder strength when they attack an enemy.

```json
{
    "type": "apotheosis:mob_effect",
    "definition": {
        "affix_type": "basic_effect",
        "exclusive_set": [],
        "weights": {
            "rare": {
                "weight": 10
            },
            "epic": {
                "weight": 15
            }
        }
    },
    "mob_effect": "minecraft:strength",
    "target": "attack_self",
    "values": {
        "rare": {
            "duration": {
                "min": 100,
                "max": 140
            },
            "amplifier": {
                "min": 0,
                "max": 0
            },
            "cooldown": 200
        },
        "epic": {
            "duration": {
                "min": 140,
                "max": 200
            },
            "amplifier": {
                "min": 0,
                "max": 1
            },
            "cooldown": 200
        }
    },
    "types": [
        "weapon"
    ]
}
```

## Wither On Hit Affix
An affix that applies the wither effect to enemies when hit, with stacking capabilities.

```json
{
    "type": "apotheosis:mob_effect",
    "definition": {
        "affix_type": "ability",
        "exclusive_set": [
            "apotheosis:poison_hit"
        ],
        "weights": {
            "summit": {
                "weight": 5,
                "quality": 1.2
            },
            "pinnacle": {
                "weight": 8,
                "quality": 1.5
            }
        }
    },
    "mob_effect": "minecraft:wither",
    "target": "attack_target",
    "values": {
        "epic": {
            "duration": {
                "min": 60,
                "max": 100
            },
            "amplifier": {
                "min": 0,
                "max": 1
            }
        },
        "mythic": {
            "duration": {
                "min": 80,
                "max": 120
            },
            "amplifier": {
                "min": 1,
                "max": 2
            }
        }
    },
    "types": [
        "weapon",
        "bow",
        "crossbow"
    ],
    "stack_on_reapply": true,
    "stacking_limit": 4
}
```
