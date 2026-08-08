# Description
An Attribute Bonus is a [Gem Bonus](./GemBonus.md) which applies a single attribute modifier to the socketed item.  

The modifier is active in every equipment slot matching the item's loot category, and its value is selected from the values map using the gem's [Purity](../Purity.md).

# Dependencies
This object references the following objects:
1. [GemClass](../GemClass.md)
2. [Purity](../Purity.md)

# Schema
```js
{
    "type": "apotheosis:attribute",
    "gem_class": GemClass,  // [Mandatory] || The set of loot categories this bonus applies to.
    "attribute": "string",  // [Mandatory] || The registry name of the attribute to modify.
    "operation": "string",  // [Mandatory] || The operation of the modifier. One of "add_value", "add_multiplied_base", or "add_multiplied_total".
    "values": {             // [Mandatory] || The modifier value for each supported purity.
        Purity: float
    }
}
```

# Examples
An attribute bonus which grants flat attack damage on melee weapons and tridents.

```json
{
    "type": "apotheosis:attribute",
    "attribute": "minecraft:generic.attack_damage",
    "gem_class": {
        "key": "light_weapon",
        "types": [
            "apotheosis:melee_weapon",
            "apotheosis:trident"
        ]
    },
    "operation": "add_value",
    "values": {
        "cracked": 1.0,
        "chipped": 2.0,
        "flawed": 3.5,
        "normal": 5.0,
        "flawless": 7.0,
        "perfect": 10.0
    }
}
```
