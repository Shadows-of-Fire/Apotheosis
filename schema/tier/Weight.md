# Description
A Weight is a simple combination of a weight and quality value.

# Schema
```js
{
    "weight": integer,  // [Mandatory] || The weight of this object. This value is relative to other objects of the same kind. Range: [0, 65536]
    "quality": float,   // [Optional]  || Quality of this object. Used when a luck level is present in the selection context. Default value = 0. Range: [-16, 16]
}
```

# Examples
A Weight specifying a weight of 25 and a quality of 1.
```json
{
    "quality": 1.0,
    "weight": 25
}
```

A Weight specifying a weight of 10, and implicitly specifying a quality of 0.
```json
{
    "weight": 10
}
```
