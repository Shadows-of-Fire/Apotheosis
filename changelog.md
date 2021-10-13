## 4.8.2
* Fixed the affix shard recipe showing as no-input on dedicated servers.

## 4.8.1
* Affix Colors have been revamped!  The new color scheme should hopefully provide a smoother transition, and really give that "specialty" to ancient items (which will arrive in 4.9.0)
* Tomes have received new textures, and no longer use the enchanted book texture.
* Fixed scrapping tome tooltips not being gray
* Brutal Pillager spawners will now spawn pillagers with crossbows.
* Reduced the height range on deadly module worldgen
* Add a lot more error handling regarding the deadly module.
* Bytegm: Updated russian translation
* Aikini: Updated chinese translation
* The Enchantment Module tooltip error logger is now at the debug level.
* The Enchantment Library is now sorted alphabetically.
* Shields with the "arrow duplication" affix now actually only work on arrows (and not tridents).
* The enchantment library tooltip is now on the left of the gui.
* Added a tooltip to an enchanted tome so that users know they need to convert it to a book.
* Fixed all apoth arrows not working in dispensers.
* Fixed affix loot item trades from wandering traders being broken.


## 4.8.0
* Added the Enchantment Library - a brand new storage system for enchanted books!
* Fixed an issue where fletching tables were crashing when opened.
* Wandering Trader trades that produce no output will not be registered, instead of causing the trader to sell nothing.
* Enchantment Stat tooltips will use the default state of a block, if the placement state cannot be determined.

## 4.7.1
* Fixed an issue where datapack-loaded worldgen entries were not reading their weights, and crashing as a result.

## 4.7.0
* Bosses will no longer select invalid or empty items as their affix item.
* Having affixes on invalid items will no longer crash, though it will spam the log.
* Properly crash if an attribute modifier is attempted to be added to a null entity.
* Bytegm: Update ru_ru.json
* Aikini: Update zh_cn.json
* Updated to Official Mappings.
* Removed random nbt tag on the blaze powder in the Potion Charm recipe.
* The Piercing affix will no longer break the names of items.
* Capturing can no longer drop eggs for mobs that are blacklisted for spawner application.
* Added the option to always apply a curse to boss items.
* Fixed ConfiguredFeatures not being registered.
* Fix Occult Aversion reducing damage dealt by more than intended.
* Allowed modification to the ambient and visible status of ChancedEffectInstance.
* Allowed for the specification of custom NBT to be fed to generated bosses.
* Made rarity thresholds configurable.
* Fixed certain attribute modifiers being removed when they were a copy of existing ones.

## 4.6.1
* A crash will no longer occur when attempting to upgrade the Draw Speed affix
* Random Boss Transforms and Random Affix Loot can now be disabled.
* Nitess - add ru_ru.json
* Made the Affix Loot Pool Entry support loot conditions and functions.