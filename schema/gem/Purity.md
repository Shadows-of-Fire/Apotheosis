# Description
Purity represents a fixed set of gem tiers. Gems are expected to have increasingly powerful stats with each purity level.  

Most gem bonuses hold a map of per-purity values, and a gem may only be used at purities that its bonuses provide values for.

# Schema
In JSON, a purity is represented as a single string. Purities may take one of the following values, in ascending order:

1. `"cracked"`
2. `"chipped"`
3. `"flawed"`
4. `"normal"`
5. `"flawless"`
6. `"perfect"`

The purity of newly spawned gems is selected using the weights in the [PurityWeights](./PurityWeights.md) file.
