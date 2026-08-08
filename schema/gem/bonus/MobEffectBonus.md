# Description
A Mob Effect Bonus is a [Gem Bonus](./GemBonus.md) which applies a mob effect when a specific event occurs, based on the target.  

If a cooldown is set, the effect cannot be reapplied until the cooldown has elapsed.  
If `"stack_on_reapply"` is set, reapplying the effect while it is already active will increase the amplifier (up to the stacking limit) and keep the longer of the two durations, instead of simply refreshing it.

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:mob_effect",
    "gem_class": GemClass,        // [Mandatory] || The set of loot categories this bonus applies to.
    "mob_effect": "string",       // [Mandatory] || The registry name of the effect to apply.
    "target": "string",           // [Mandatory] || The event and recipient of the effect. See the list below.
    "values": {                   // [Mandatory] || The effect data for each supported purity.
        Purity: {
            "duration": integer,  // [Mandatory] || The duration of the effect, in ticks.
            "amplifier": integer, // [Mandatory] || The amplifier of the effect. Zero-indexed, meaning 0 is level I.
            "cooldown": integer   // [Optional]  || The cooldown between applications, in ticks. Default value = 0.
        }
    },
    "stack_on_reapply": boolean,  // [Optional]  || If the effect stacks when reapplied. Default value = false.
    "stacking_limit": integer     // [Optional]  || The maximum amplifier when stacking. Default value = 255. Range: [1, 255].
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
A mob effect bonus for bows which applies stacking Bleeding to entities hit by arrows, up to Bleeding IV.

```json
{
    "type": "apotheosis:mob_effect",
    "gem_class": "apotheosis:bow",
    "mob_effect": "apothic_attributes:bleeding",
    "stack_on_reapply": true,
    "stacking_limit": 4,
    "target": "arrow_target",
    "values": {
        "flawless": {
            "duration": 160,
            "amplifier": 0,
            "cooldown": 40
        },
        "perfect": {
            "duration": 160,
            "amplifier": 1,
            "cooldown": 40
        }
    }
}
```
