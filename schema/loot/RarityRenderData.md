# Description
Rarity Render Data defines the visual effects shown when an affix item of a given [Loot Rarity](./LootRarity.md) is dropped in-world.  

There are four effects: a vertical beam, a ground glow, a shadow underneath the item, and item particles.

# Schema
```js
{
    "beam_height": float,      // [Mandatory] || The height of the beam (and the glow) effect. A value of zero disables both. Range: [0, 256].
    "beam_radius": float,      // [Mandatory] || The radius of the beam effect. A value of zero disables the beam. Range: [0, 5].
    "beam_texture": "string",  // [Mandatory] || The texture of the beam effect. Ignored if the beam is disabled.
    "glow_radius": float,      // [Mandatory] || The radius of the glow effect. A value of zero disables the glow. Range: [0, 7].
    "glow_texture": "string",  // [Mandatory] || The texture of the glow effect. Ignored if the glow is disabled.
    "shadow": ShadowData,      // [Optional]  || The shadow rendered under the dropped item. Defaults to the standard shadow.
    "particle": ParticleData   // [Optional]  || Particle settings for the dropped item. Defaults to disabled.
}
```

All four effects are tinted using the color of the owning rarity.

When the entire render data object is omitted from a rarity, the default is a 3.5-block beam (radius 0.035) using `apotheosis:textures/rarity/beam.png`, a glow of radius 0.065 using `apotheosis:textures/rarity/glow.png`, the standard shadow, and no particles.

## Shadow Data
```js
{
    "size": float,        // [Mandatory] || The size of the shadow. A value of zero disables the shadow. Range: [0, 2].
    "alpha": integer,     // [Mandatory] || The opacity of the shadow. 0 is fully transparent, and 255 is fully opaque. Range: [0, 255].
    "texture": "string",  // [Mandatory] || The texture of the shadow. The texture should either be a square, or an X by N rectangle, where N is the number of frames in the animation.
    "frames": integer,    // [Optional]  || The number of frames in the shadow animation. If this is 1, the shadow will not animate. Default value = 1. Range: [1, 128].
    "frame_time": float   // [Optional]  || The time in ticks between frames of the shadow animation. Default value = 1. Range: [0.5, 40].
}
```

The standard shadow has a size of 0.35 and an alpha of 255, using the static texture `apotheosis:textures/rarity/shadow.png`.

## Particle Data
```js
{
    "enabled": boolean  // [Mandatory] || If particles are shown for the dropped item.
}
```

Currently, particle settings are a simple toggle.

# Examples
The render data for the mythic rarity, which uses the standard beam and glow, an animated shadow, and enables particles.

```json
{
    "beam_height": 3.5,
    "beam_radius": 0.035,
    "beam_texture": "apotheosis:textures/rarity/beam.png",
    "glow_radius": 0.065,
    "glow_texture": "apotheosis:textures/rarity/glow.png",
    "particle": {
        "enabled": true
    },
    "shadow": {
        "alpha": 255,
        "frame_time": 1.5,
        "frames": 7,
        "size": 0.4,
        "texture": "apotheosis:textures/rarity/shadow_t4.png"
    }
}
```

The render data for the common rarity, which disables the beam by setting the beam height to zero, and uses the default shadow.

```json
{
    "beam_height": 0.0,
    "beam_radius": 0.035,
    "beam_texture": "apotheosis:textures/rarity/beam.png",
    "glow_radius": 0.065,
    "glow_texture": "apotheosis:textures/rarity/glow.png"
}
```
