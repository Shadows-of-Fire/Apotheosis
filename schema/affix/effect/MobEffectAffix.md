# Description
A Mob Effect Affix is an [Affix](../Affix.md) which applies a mob effect when a specific event occurs, based on the target.  

If a cooldown is set, the effect cannot be reapplied until the cooldown has elapsed.  
If `"stack_on_reapply"` is set, reapplying the effect while it is already active will increase the amplifier (up to the stacking limit) and keep the longer of the two durations, instead of simply refreshing it.

# Dependencies
This object references the following objects:
1. [AffixDefinition](../AffixDefinition.md)
2. [LootRarity](../../loot/LootRarity.md)
3. [StepFunction](../../../../../../Placebo/blob/-/schema/StepFunction.md)
4. [LootCategory](../../loot/LootCategory.md)

# Schema
```js
{
    "type": "apotheosis:mob_effect",
    "definition": AffixDefinition,     // [Mandatory] || The definition of this affix.
    "mob_effect": "string",            // [Mandatory] || The registry name of the effect to apply.
    "target": "string",                // [Mandatory] || The event and recipient of the effect. See the list below.
    "values": {                        // [Mandatory] || The effect data for each supported rarity.
        LootRarity: {
            "duration": StepFunction,  // [Mandatory] || The duration of the effect, in ticks. The output of the function will be truncated to an integer.
            "amplifier": StepFunction, // [Mandatory] || The amplifier of the effect. Zero-indexed, meaning 0 is level I. The output of the function will be truncated to an integer.
            "cooldown": integer        // [Optional]  || The cooldown between applications, in ticks. Default value = 0.
        }
    },
    "types": [                         // [Mandatory] || The loot categories this affix may roll on. When empty, all categories are allowed.
        LootCategory
    ],
    "stack_on_reapply": boolean,       // [Optional]  || If the effect stacks when reapplied. Default value = false.
    "stacking_limit": integer          // [Optional]  || The maximum amplifier when stacking. Default value = 255. Range: [1, 255].
}
```

The target uses the naming scheme `<event>_<recipient>`. The list of targets is as follows:

1. `attack_self` - When the user attacks an entity, applied to the user.
2. `attack_target` - When the user attacks an entity, applied to the entity.
3. `hurt_self` - When the user is hurt, applied to the user.
4. `hurt_attacker` - When the user is hurt, applied to the attacker.
5. `break_self` - When the user breaks a block, applied to the user.
6. `arrow_self` - When an arrow fired by the user hits an entity, applied to the user.
7. `arrow_target` - When an arrow fired by the user hits an entity, applied to the entity.
8. `block_self` - When the user blocks damage with a shield, applied to the user.
9. `block_attacker` - When the user blocks damage with a shield, applied to the direct attacker.
10. `projectile_self` - When any projectile fired by the user hits an entity, applied to the user.
11. `projectile_target` - When any projectile fired by the user hits an entity, applied to the entity.

# Examples
The Withering affix, which applies Wither to attackers whose blows are blocked by the shield.

```json
{
    "type": "apotheosis:mob_effect",
    "definition": {
        "affix_type": "basic_effect",
        "exclusive_set": [],
        "weights": {
            "quality": 0.1,
            "weight": 25
        }
    },
    "mob_effect": "minecraft:wither",
    "target": "block_attacker",
    "types": [
        "apotheosis:shield"
    ],
    "values": {
        "apotheosis:epic": {
            "duration": {
                "min": 40.0,
                "max": 100.0,
                "step": 20.0
            },
            "amplifier": {
                "min": 0.0,
                "max": 1.0,
                "step": 0.5
            }
        },
        "apotheosis:mythic": {
            "duration": {
                "min": 60.0,
                "max": 160.0,
                "step": 20.0
            },
            "amplifier": {
                "min": 0.0,
                "max": 2.0,
                "step": 0.25
            }
        }
    }
}
```
