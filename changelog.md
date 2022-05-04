## 4.8.99E
* Removed i18n: true declaration from patchouli book due to formatting errors.
  * Book can still be translated, just not through language files.

## 4.8.99D
* Fixed spawners not invoking finalizeSpawn.
* Added i18n: true declaration to patchouli book.json file (by request).
* Added Enchanting Stats for both shelves and the table to ToP/HWYLA.

## 4.8.99C
* Fixed Spawner entity names not showing.
  * The name is no longer in the tooltip, and is now shown in the item name.
* Fixed adding nbt tags to spawner stacks that have no nbt.
* Spawners will now received colored names based on the contained entity type.
* The item that the Torch Placement affix uses can be configured.
* Fixed Boon of the Earth being applicable on axes instead of pickaxes.

## 4.8.99B
* Increased the weight for affix trades on wandering traders from 3 to 15.
  * Added a config to change the weights as well.
* Fixed affix loot item names being italic.
* Added the Silent spawner modifier which marks all spawned mobs as silent.
* Capturing can now be applied to axes.

## 4.8.99A (4.8.9999)
* Fixed a server startup crash caused by the spawner module.
* Fixed the Infuse Hellshelf / Seashelf advancements.
* Fixed a bug where Infused Hell/Sea shelves could be re-infused for no benefit.


## 4.8.999
* This is a Backport of some major 1.18.2 changes.
* Added the Enchantment Info Screen
* Added Rectification and Clues
* Added Rectification Shelves and Sightshelves.
  * Recipes look weird due to a lack of 1.18 content.  Glowstone replaces amethyst, glass replaces spyglass.
* Added Infusion Enchanting
  * Hellshelves and Seashelves can no longer be enchanted except via infusion enchanting.
* Internal data values for Quanta and Arcana have been multiplied by 10, update JSON files accordingly!
* Added the Library of Alexandria
* Fixed infinite enchantment dupe bug with libraries.
* Added a way to make Potion Charms unbreakable (requires ench module).
* Updated Life Mending to act like the 1.18.2 version.
* Scavenger bonus drops will now be converted by KoTA.
* The execute affix simply kills the target, instead of dealing Float.MAX_VALUE true damage.
* Revamped Spawner Modifiers to the extended 1.18 system.
  * They are now JSON recipes and support multiple stat changes per single modifier, as well as per-modifier stat caps.
* Removed the Ignore Spawn Cap modifier.
* Added the Ignore Light Levels and NoAI modifiers.
* Updated Spawner Modifier JEI plugin substantially.
* Added The One Probe compat
* Updated advancements to account for changes.
* The Chronicle of Shadows has been added - Patchouli is required.
  * There is no section for the Deadly Module, as it did not exist in 1.18 to be backported.
* Shear and Trident enchantments have been added.
* Chainsaw and Earth's Boon have been added.
* Deadly Module worldgen should now work properly even if YUNG's Better Dungeons is installed.

## 4.8.4
* E.Kim: Updated ko_kr.json (Korean Translation)
* Added the ability turn off mythic items being unbreakable.

## 4.8.3
* Added an extra tooltip tab to the Enchanting Table, which shows power range, xp cost, and item enchantability.
* PixVoxel: Add ko_kr.json (Korean Translation)
* Added better handling on the loading of certain apotheosis jsons.
* Made particles spawn for all valid apotheosis bookshelves.
* Fixed enchanting power having a hard cap of 200.
* Made it so anvils that fall on torches retain their enchantments.
* Potion charms are now marked as non-repairable.

## 4.8.2
* Fixed the affix shard recipe showing as no-input on dedicated servers.
* Prevented a crash if some mod puts an invalid value in the itemstack attribute modifier map.
* Moved the books on top of the enchantment library so it does not z-clip if a block is placed above it.

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