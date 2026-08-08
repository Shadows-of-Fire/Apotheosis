# Description
A Surface Type represents a positional check against the surface in the world.  

# Schema

In JSON, a surface type is represented as a single string.

```js
"string" // [Mandatory] || The surface type.
```

The list of surface types is as follows:

1. `needs_sky` - The sky must be visible at the target position. This will always fail if the dimension does not have a sky (like The End).
2. `needs_surface` - The position must be at or above the surface level (ignoring leaves).
3. `below_surface` - The position must be below the surface level.
4. `cannot_see_sky` - The sky must not be visible at the target position. This will always succeed if the dimension does not have a sky.
5. `surface_outer_end` - The surface must be visible, and either the X or Z coordinate must be more than 1024 blocks away from the origin.
6. `any` - Always passes.
7. `needs_sky_or_same_vertical_slice` - The sky must be visible at the target position, or the position must be within 8 blocks on the Y-axis of the nearest player.
