# Description
An affix type is a preset category name, used by loot rules to gather affixes of a specific type when generating items.

Currently, affix type is hardcoded with three entries, but in the future, it will become dynamic (so new types can be added).

# Schema
AffixType is a string enum with the following possible values:

```js
"stat" | "basic_effect" | "ability"
```

# Values

## `stat`
Affixes that apply attribute modifiers to items, such as increased attack damage, movement speed, or health. These are generally simple numerical bonuses.

## `basic_effect`
Affixes that provide simple effects like potion effects, resistance to damage types, or basic triggers.

## `ability`
Affixes that add complex abilities or special functionality to items, such as cleaving, teleportation, or other advanced effects.

# Examples

```json
"affix_type": "stat"
```

```json
"affix_type": "ability"
```
