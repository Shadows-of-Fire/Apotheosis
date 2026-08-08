# Description
Purity Weights are the global mapping that controls the [Purity](./Purity.md) of spawned gems, specified per world tier.  

Unlike most data files, purity weights are a singleton: only the file `apotheosis:weights` (at `data/apotheosis/purity_weights/weights.json`) is read. Datapacks modify the weights by overriding that file. If additional files are added to the folder, an error is logged and they are ignored.  

If the file is missing entirely, all purity weights are set to zero, and only cracked gems will spawn.

# Dependencies
This object references the following objects:
1. [WorldTier](../tier/WorldTier.md)
2. [Purity](./Purity.md)
3. [Weight](../tier/Weight.md)

# Schema
```js
{
    "weights": {          // [Mandatory] || A map of per-tier purity weights. Any purity omitted from a tier receives a weight of zero in that tier.
        WorldTier: {
            Purity: Weight
        }
    }
}
```

# Examples
A trimmed version of the default purity weights, showing the haven and pinnacle tiers. Low purities dominate in haven, while pinnacle skews heavily towards the higher purities.

```json
{
    "type": "apotheosis:purity_weights",
    "weights": {
        "haven": {
            "cracked": {
                "weight": 550
            },
            "chipped": {
                "quality": 2.5,
                "weight": 410
            },
            "flawed": {
                "quality": 5.0,
                "weight": 40
            },
            "normal": {
                "weight": 0
            },
            "flawless": {
                "weight": 0
            },
            "perfect": {
                "weight": 0
            }
        },
        "pinnacle": {
            "cracked": {
                "weight": 0
            },
            "chipped": {
                "weight": 0
            },
            "flawed": {
                "weight": 100
            },
            "normal": {
                "weight": 330
            },
            "flawless": {
                "weight": 470
            },
            "perfect": {
                "quality": 5.0,
                "weight": 100
            }
        }
    }
}
```
