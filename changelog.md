## 5.4.0
* The Deadly Module is back!
* Not yet released, this is here for staging purposes.

## 5.3.5
* JEI will now display if spawner silk touch is disabled, always enabled, or requires a certain level.
  * Previously it only displayed if it required a certain level.
* Updated to Placebo 6.4.0
  * Some fixes made in Placebo impact Apotheosis content.
* Fixed spawners never invoking finalizeSpawn.
  * This would cause slimes to always be the smallest size, among other bugs.
* Fixed compat with Goblin Traders.
* Added tooltips for Enchanting Stats and the currently computed Table Stats to ToP/Jade
* Added the Silent spawner modifier.


## 5.3.4
* Updated to 1.18.2
* Fixed the Enchantment Library not saving if it was the only changed TE in a chunk.
* Fixed the Absolute Max Eterna not being updated when it changed.
* Added additional controls for max loot level of certain enchantments.
  * Also added the ability to remove books containing certain enchants from loot pools.
* Made table levels round eterna values, instead of clamping.
* Updated to new JEI and Jade API's.
  * Potion Charm recipes in JEI should be a bit more responsive now.

## 5.3.3
* Fixed Capturing not working with some modded spawn eggs.
* Fixed an issue with infusing the Enchantment Library.

## 5.3.2
* Upgraded to Placebo AutoSync and Container Data systems.
  * Should fix any issues relating to enchanting stats not showing on LAN worlds.

## 5.3.1
* Fixed book pages not loading when certain modules are disabled.
* Fixed a few other bugs occuring when the enchanting module is disabled.

## 5.3.0
* Added Patchouli Support! The Chronicle of Shadows now details everything about Apotheosis.
* Further updated the Spawner Modifiers JEI window.
* Improved TOP/Jade Spawner support.
* Made Icy Thorns incompatible with Thorns
* Fixed Rebounding being applicable to boots and helmets.
* Changed Reflective Defenses to do a percent of the original damage.
* Buffed Shield Bash's damage and reduced durability cost.
* Fixed Spawner Modifier recipes returning null to ItemStack methods.
* Fixed Potion Charm Curios compat.
* Fixed Spawners not clearing spawn potentials when an egg is applied.
* Enchantment Descriptions and Patchouli are going to be marked as required deps now, despite being optional deps.
  * This is so that they get automatically installed by CF.

## 5.2.2
* Fixed various blocks not breaking faster with the correct tool.
* Fixed various blocks not dropping when broken.

## 5.2.1
* Improved the Spawner Modifiers JEI window.
* Fixed a bug when viewing all recipes, and the potion charm recipe showed up (which crashed).
* Improved error handling when an invalid banned mob entry is detected.
* Fixed Endless Quiver translation.

## 5.2.0
* The Spawner Module has been rewritten!
* Spawner Modifiers are now a JSON-based recipe system, with two inputs to any number of stat changes.
* Removed the "Ignore Spawn Cap" modifier.
* Added the "No AI" modifier.
* Added the "Ignores Light" modifier.
* Fixed a bug with the Enchantment Library where books above level 16 broke everything.
* Made the enchantment library require Infused Hellshelves instead of standard.
* Added the Library of Alexandria, which goes up to level 31.
* Added a recipe to make Potion Charms unbreakable.
* Aikini: Updated chinese translation.
* Re-Added compat for quark ancient tomes.
* Fixed issues caused by disabling modules.
* Fixed advancements being granted at the wrong time.
* Added support for The One Probe.

## 5.1.1
* Fixed a crash caused by a missing null check on LootingLevelEvent#getDamageSource
* Added the Inert Trident and a new recipe for tridents.
* Removed the crafting recipe for XP Bottles
* Added an Infusion Enchanting recipe for XP bottles

## 5.1.0
* The Enchantment Module lands on 1.18.1!
* I'll try my best to list everything, but there's a LOT of changes, so expect some misses!
* Obliteration and Splitting now have a max level of I, and have a 100% success rate.
* Many more control flags have been added to the enchantment info config.
* Life Mending has been changed to use receied healing, rather than your current health bar.
* The Chainsaw enchant has been added (Axe Enchantment to cut down entire trees).
* The Earth's Boon enchant has been added (Pickaxe Enchantment to find ores in stone).
* There are new Shear Enchantments (Chromatic Aberration, Growth Serum, and Worker Exploitation).
* Fortune can now be applied to shears.
* Tridents can now receive Sharpness, Looting, and Piercing.
* The Spearfishing enchant has been added (Drops fish on thrown trident kill, 3.5%/level).
* Hell Infusion, Sea Infusion, and Occult Aversion have been removed.
* Added the Infusion Enchanting crafting system, which is how Infused Hell/Seashelves will be created.
* Added two new Enchanting Stats - Rectification and Clues
* Item Enchantability now no longer impacts power, but provides a direct Arcana boost.
* Endless Quiver has been moved from the Potion Module to the Enchanting Module.
* Added the "Available Enchantments" screen, which can be used while enchanting to see everything you might roll.
* Removed the Altar of the Sea
* Update Graphic: https://imgur.com/a/DfJKn4j

## 5.0.0
* Initial 1.18.1 port.  Does not contain the Enchantment or Deadly Modules.
* The Garden, Potion, Spawner, and Village modules are all present in a direct-port fashion.

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