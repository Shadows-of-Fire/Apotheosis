# Description
A Loot Rarity specifies the rules for generating affix items. Adding a new rarity is no simple task, as you will need to supply any and all affixes that the rarity requires for each loot category that exists.  

In addition, you will need to wire up reforging recipes and invaders that link back to your new rarity. You may also wish to setup gem cutting recipes that use your rarity material.  

Finally, rarities are mostly independent of one another. Many datapacks can add new rarities without interfering with each other.  

# Dependencies
This object references the following objects:
1. [Color](../../../../../Placebo/blob/1.21/schema/Color.md)
2. [ItemStack](../../../../../Placebo/blob/1.21/schema/ItemStack.md)
3. [TieredWeights](../tier/TieredWeights.md)
4. [LootRule](./LootRule.md)
5. [RarityRenderData](./RarityRenderData.md)

# Schema
```js
{
    "color": Color,                  // [Mandatory] || The color of this rarity. Used in various places to identify it. This color should be unique and visible on different backgrounds.
    "material": ItemStack,           // [Mandatory] || The rarity material for this rarity. Must not be empty.
    "weights": TieredWeights,        // [Mandatory] || Tier-specific weights for this rarity, relative to other rarities.
    "rules": [                       // [Mandatory] || The list of loot rules this rarity will apply during reforging.
        LootRule
    ],
    "sort_index": integer,           // [Optional]  || A magic number used to order the rarities when they are displayed together in a list. Lower numbers are displayed first. Default value = 1000. Range: [0, 2000].
    "render_data": RarityRenderData, // [Optional]  || Visual effects shown when an affix item of this rarity is dropped in-world. Defaults to the standard beam, glow, and shadow effects.
    "invader_sound": "string"        // [Optional]  || Registry name of the sound event played when an invader spawns with this rarity. Default value = "minecraft:block.end_portal.spawn".
}
```

Note: Per-category loot rule overrides are not part of the rarity object. To specify different rules for certain loot categories, use a [RarityOverride](./RarityOverride.md).

# Examples
The common loot rarity. This rarity has a gray color and uses `apotheosis:mysterious_scrap_metal` as the rarity material.  
It applies one stat, and has a 25% chance to apply a second stat. It is highly-weighted in early world tiers, eventually falling off entirely.  
It also disables the beam effect (by setting the beam height to zero) and silences the invader spawn sound.

```json
{
    "type": "apotheosis:rarity",
    "color": "#808080",
    "invader_sound": "minecraft:intentionally_empty",
    "material": "apotheosis:mysterious_scrap_metal",
    "render_data": {
        "beam_height": 0.0,
        "beam_radius": 0.035,
        "beam_texture": "apotheosis:textures/rarity/beam.png",
        "glow_radius": 0.065,
        "glow_texture": "apotheosis:textures/rarity/glow.png"
    },
    "rules": [
        {
            "type": "apotheosis:affix",
            "affix_type": "stat"
        },
        {
            "type": "apotheosis:chanced",
            "chance": 0.25,
            "rule": {
                "type": "apotheosis:affix",
                "affix_type": "stat"
            }
        }
    ],
    "sort_index": 300,
    "weights": {
        "haven": {
            "weight": 600
        },
        "frontier": {
            "weight": 290
        },
        "ascent": {
            "weight": 100
        },
        "summit": {
            "weight": 0
        },
        "pinnacle": {
            "weight": 0
        }
    }
}
```
