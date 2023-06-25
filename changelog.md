## Future
* [NYI] Gems can now be stored in the Gem Safe, a storage device similar to the Enchantment Library for Gems
* [NYI] Added affixes for Bursting Vitality and Grevious Wounds.

## 6.3.0
### Balance Changes
* Instant Health on Hit max level III => II. Chance of receiving level II reduced from 50% to 20%, and is also locked to mythic.
  * Instant Health III was too strong at all thresholds, and Instant Health II was too common.
* Endersurge Gem level boost reduced from 1/2/3 to 1/1/2.
  * Endersurge gems are extremely powerful, and +3 to all enchantments is a bit over the top.
  * Epic rarity is being left enabled to make the combine cost up to Ancient a bit more expensive.
* Most affixes have had their values rebalanced.
  * Common Items should no longer feel underwhelming or that their bonuses are irrelevant.
  * Uncommon to Rare items should feel similarly strong to how they were before.
  * Epic and Mythic items should feel weaker than before, and without such substantial power jumps from the prior rarities.
* The Blessed Affix (Max Health) can no longer be found on shields.
* The Gravitational Affix can no longer be found on leggings.
* The Stalwart (Knockback Resist) Affix will be stronger on shields.
* Heavy Weapons AS Reduction will no longer reduce attack speed below 0.4.
* Fixed a bug with Draw Speed that caused all values to be rounded up to the nearest 100 percent.
* Bosses will be significantly less tankier, but will do more damage and be faster.
* Overheal will now have a max value equal to half of your max health, instead of 20.
* Crit Chance will no longer trigger a vanilla (jump) crit, and will now roll on all damage.
  * Crit Damage will still impact vanilla (jump) crits.
* Armor Calculations have been changed.
  * Vanilla Calculations are: DR = clamp(armor - damage / (2 + toughness / 4), armor / 5, 20) / 25.
  * Old Apoth Calculations were: DR = clamp(1.25 * armor - damage / (2 + toughness / 4), armor * (.25 + toughness/200), 20) / 25.
  * New Apoth Calculations are: DR = 50 / (50 + armor).
  * Armor Toughness will now reduce Armor Bypass (both Armor Shred and Armor Pierce) by 0.5%/point, but no longer impacts damage calculations.
  * Comparisons: https://i.imgur.com/3yEnTyi.png
* Protection Calculations have been changed.
  * Vanilla Calculations are: DR = 4% * prot points, up to 80%.
  * Old Apoth Calculations were: DR = 4% * prot points, up to 80%, then an additional 0.33% * prot points, up to 95%.
  * New Apoth Calculations are: DR = 2.5% * prot points, up to 85%.
* Four new attributes for bypassing damage reduction were added: Armor Shred, Armor Pierce, Protection Shred, and Protection Pierce.
  * These are Percentage and Flat reductions for Armor and Protection, respectively.

### Other Changes
* Fixed Gem Loot Rules not being applied at all (gem drops were using the Affix Loot Rules).
* Affix Item and Gem drop chances have been reduced.
  * The chance for a random affix item to be added to an entity was erroneously at 24%, it has been reduced to 7.5%.
  * The "literal" Gem drop chances have not been changed, but the "effective" chance was reduced as a side effect of fixing the config.
* All gems will now be shown in JEI. The shown versions will be at max rarity with max facets.
* Fixed the crit chance / crit damage default value issue once again (was reintroduced in 6.2.0).
* The Salvaging Table can now be automated!
  * Inserted items will be automatically salvaged and placed in the output inventory.
  * The output inventory can be extracted from.
* Salvaging now yields a different amount of gem dust per rarity.
  * The `U` key (recipe lookup button) now works properly for gems.
* Added the `apotheosis:healing_received` attribute.
* Added a validator that ensures the proper number of affixes exist for any given category/rarity combination.
* Fixed a crash with boss spawners in invalid dimensions.

## 6.2.1
* Fixed an issue with mounted bosses causing a crash.
* Fixed misspelling of Grievous.
* Added the miniboss exclusion `"apotheosis:surface_type"`, allowing restricting certain minibosses to areas with surface / sky view.
* Added a config for bosses receiving the glowing effect.

## 6.2.0

### General
* Updated to Placebo 7.2.0.
  * This means everything uses codecs now, so error logging and validation should be way better.
* rtxyd: Updated chinese translation.

### Adventure Module
* Legacy Gems have now been removed!
  * All legacy gems will transition to an errored state, and will only be usable for gem dust.
  * Items with legacy gems will swap to having empty sockets.
* Bosses now have a `"mount"` field.
* Increased the output of the Vial of Extraction and Vial of Expulsion to 2 per craft (recipe unchanged).
* Reduced the frequency at which the name parts list is used.
* Resolved a dupe issue with sockets.
* Added minibosses, a method of converting normal mob spawns to empowered variants.
* Creative players will no longer trigger boss spawner blocks.
* Updated the name list for the adventure module. Regenerate names.cfg to get the updates.
* Affix data now uses the Placebo CachedObject system, which should improve performance substantially.
* All Gems will now be shown in JEI. The ones shown will be at max rarity and max facets.
* Fixed an issue where reading affixes from NBT could trigger an infinite loop.
* Fixed an issue where the Cleaving affix could hit dead targets, killing them twice.
* Anim Mallon: Added a config to disable boss auto-aggro.
* Boss auto-aggro has been disabled by default, and when enabled, will no longer target creative players.
* Fixed Piercing not working properly on Tridents.
* Gem rarities are now configurable on a per-dimension basis, meaning gems of certain rarities can be locked (without locking the entire gem to another dimension).
* Epic bosses will no longer spawn in the Overworld, and Mythic bosses will no longer spawn in the Nether or Twilight.
* Apoth worldgen will no longer spawn in the Deep Dark.
* Fixed an issue caused by Loot Pinata and Keep Inventory.
* Fixed passengers not working on bosses.
* Added the Simple Reforging Table, allowing reforging Rare or weaker items pre-netherite.
* Added the Vial of Unnaming, which can remove the affix name components from an item.
* Loot Pinata will no longer work on Gateways.

### Enchanting Module
* Added the Improved Tome of Scrapping and the Tome of Extraction
  * The Improved Tome of Scrapping will pull all enchantments.
  * The Tome of Extraction will pull all enchantments and will not destroy the source item.
  * The XP Cost of the basic Tome of Scrapping has been reduced. The higher tier tomes will have increased costs.

### Spawner Module
* Entities from a NoAI Spawner will no longer be able to teleport.

### Potion Module
* Added Bursting Vitality, a potion effect which increases healing received by 20% per level.
* Added Grevious Wounds, a potion effect which decreases healing received by 40% per level.
* Apotheosis potion items will now show a tooltip displaying their effects.
  * Might move this to a separate mod akin to Enchantment Descriptions. (Yes, I know JEED exists).

## 6.1.5
* Fixed an issue where boss names were failing to translate.
* Fixed an issue with the salvaging recipe returning a null itemstack.
* Reforging no longer uses the enchanting seed.
* Fixed Legacy Gems not providing any bonuses (again).
  * Legacy Gems will be permanently removed in 6.2.0.

## 6.1.4
* Fixed a network encoding error in SalvagingRecipe$OutputData.

## 6.1.3
* Adjusted the Salvaging Table to be recipe-backed, allowing for easier configuration and extensibility.
* The Salvaging Table now has JEI Support!
* Gems can now be processed in the Salvaging Table, yielding 1-2 dust each.
* Added support for GameStages.
* Fixed Endersurge gems applying +2 levels to everything instead of only existing enchantments.
* Fixed a crash that happened when trying to upgrade socketed items in a smithing table (ex diamond->netherite).
* Fixed the Thunderstruck (damage to nearby enemies) affix.
* mc-kaishixiaxue: Updated Chinese translation, including the Chronicle of Shadows.
* rtxyd: Also updated Chinese translation.
* SKZGx: Updated Ukranian translation.
* Made the translation keys for top-level affix naming configurable (https://github.com/Shadows-of-Fire/Apotheosis/issues/818)
* Added dimensional rarity rules for Gems.

## 6.1.2
* Converted Affixes to use Codecs for serialization, which produces better error reporting.
* Added a few additional dimension-specific gems.
* Omnetic items will now also work as a Hoe and Sword.
* Increased the performance of ShieldBreakerTest by not reconstructing the context zombies.
* Fixed Legacy Gems not working.
* Fixed an issue that caused Unique gems to not provide their bonuses.
* Increased the performance of LootCategory.forItem which is a fairly hot path.
* Fixed a crash that could happen if a legacy gem with no nbt showed up.
* Reduced required forge version to 43.2.0
* Made Reforging Costs configurable.

## 6.1.1
* Hotfix that fixes common items always having four sockets.

## 6.1.0
* Gems have been rewritten! Gems are now backed by an all new system that allows for far more customization than before.
  * New Gems have been added, with all sorts of new features and stat ranges. Old gems will be referred to as Legacy Gems, and will not have rarities nor be upgradable.
  * Purity has been replaced by Facets, a value indicating the number of cuts in the gem. A Fully-Faceted Gem will be referred to as a "Flawless" Gem and will Glow.
  * Gems now have individual rarities, indicated by name color.  Gems of a higher rarity will be stronger.
  * Gems are now able to be dimensionally-locked.
  * Gems may also now be Unique, meaning you can only insert one of those gems into any given item. (You may still use more than one overall, but only one per item).
  * Gems may now provide different bonuses when socketed into different types of items.
  * You can increase the number of facets that a gem has in the Gem Cutting Table.
  * Two Flawless (Max-Facet) gems of the same type and rarity can be upgraded to the next rarity in the Gem Cutting Table.
* Bee and Goat bosses have been removed. Those had a good run, but were ultimately not good for anything.
* The cooldown field now works properly for HURT_SELF and ATTACK_SELF potion affixes.
* The default boss spawn cooldown has been increased to 1800 ticks (from 400).
* Potentially fixed earlier - NBT tags that were appearing on random mob drops will no longer appear.
* Life Mending will now work on curios.
* Potion Charms can be set to only function in curios slots.
* Fixed an issue where the Enchantment Library would not display certain applicable enchantments (like sharpness on axes).
* The particular loot rules for each rarity may now be adjusted. This means you can set the number of affixes, sockets, and durability reduction for each rarity.
* Eugene: Added Ukranian translation.

## 6.0.3
* Fixed potion affixes not spawning.
* Fixed an issue where a crash could occur if you walked into a dimension with no adventure module entries (gems/affixes/bosses).

## 6.0.2
* Forward-Ported all changes from 5.7.6.
* Config files have been updated to be more user-friendly.
* Splitting and Obliteration will now work properly.
* The Adventure Module can now be disabled again.
* RiggZh: Updated Chinese Translation

## 6.0.1
* Forward-Ported all changes from 5.7.1-5.7.4
* Fixed typo in the lang key for the Feathery affix.
* Fixed a server disconnect present in 5.7.4.
* Made the new loot pool entries able to specify a subset of gems/entries for selection.

## 6.0.0
* Updated to Minecraft 1.19.2
  * Source version - 5.7.1

## 5.7.4
* Fixed a dupe bug caused by the Vial of Arcane Extraction.
* Fixed some Z-fighting in the Reforging Table screen.

## 5.7.3
* Fixed the radial affix not loading.
* Added the apotheosis:random_gem and apotheosis:random_affix_item Loot Pool Entry Types.
* Gems will now report their Purity (strength relative to max value) on their tooltip.
  * Should help with knowing how strong a gem is relative to others of the same stat.
* Added the Sigil of Socketing, which can apply sockets to items.
* Fixed an issue where not enough effect-type affixes were available for rare armor items.
* Loot Pinata will no longer work on equipment items.

## 5.7.2
* Fixed certain affixes not loading.
* Fixed a crash that may happen if a boss refuses to allow a custom name to be set.

## 5.7.1
* Affixes are now data-driven! Each affix has its own json file located in the `affixes` subdirectory.
  * Through this, you can now configure the values for each affix at each rarity level.
  * You can also add new potion and attribute affixes for modded attributes/effects.
  * This means if you don't like the numbers, or you think something is over/underpowered - just change it!
* Many affix default values have been rebalanced.
  * For the most part, this was a reduction in affix levels.
* Current HP Damage is no longer armor-piercing.
* The Reforging Table will no longer accept more than 1 item in the primary slot.
* Heavy Weapons will have their attack speed reduced based on rarity instead of a flat value.
* Critical hits have been reworked (again).
  * The first 50% of bonus crit chance is now ignored on a vanilla (jump) crit, but crit damage is increased by 50%.
  * Overcrits will now scale worse
* Anvils can no longer accidentally void gems.
* Bosses are now immune to suffocation damage.
* Fixed a crash that happened if a boss prevents custom names.

## 5.7.0
* As noted in-line below, you should regenerate all or part of your /config/apotheosis/adventure.cfg file.

### Features
* Added a new Affix Conversion Loot Modifier!
  * This modifier gives all loot-generated gear pieces a chance to be rolled as an affix item.
  * The chance is configurable on a per-loot-table basis and the rarities are dimensional.
  * This reduces the burden of messing with the affix loot entries table when large swathes of gear already generate.
  * ATTENTION PACKDEVS! If you do not update your config you will start seeing any sticks dropped by a loot table being given affixes!
* Added Gem Dust, a magical resource obtained by smashing Apotheotic Gems with an anvil.
* Added the Vial of Searing Expulsion and Vial of Arcane Extraction, which allow for gem removal.
* Added the Scrapping Table, which allows you to break down affix items into rarity materials.
* Added the Reforging Table, which allows you to reroll the affixes on an item (including items you have crafted yourself)!
  * Reforging an item costs rarity materials, gem dust, and experience.
  * Items can only be rolled as their primary type detected automatically or set through the config.
* Bosses will now glow their rarity color on spawn, instead of white.
  * Glow duration increased from 20 seconds to 2 minutes.
* There is now a boss cooldown timer, defaulting to 20 seconds.
  * Regardless of the boss spawn chance, no boss may spawn until the timer has elapsed since the last boss spawn.
* Bosses will now report their rarity and boss-specific bonus modifiers in Jade/TOP.
* The Boss Announcement Volume can now be configured per-client.

### Bugfixes
* Made Current HP damage deal armor-piercing physical damage instead of magic damage.
* Fixed gems dropping off of non-monsters.
* Fixed rarities on affix loot items not clamping properly.
  * Bosses always worked as expected, but random items were ignoring the min and max values.
* Updated default loot rules from chests.* to .*chests.* so certain modded chests aren't missed.
* Used a different event for surface boss spawns, which should resolve the "infinite boss spawns" that is being seen in some environments.
  * This means only "real" spawns will actually attempt to trigger a boss, instead of all potentials, which means that boss numbers need to be adjusted.
  * You may want to allow the spawn chance section of your configs regenerate.
* AHilyard: Fixed a transform issue with other tooltip components and apoth sockets.
* Fixed a crash when a boss forcibly rejects the custom boss name.
* Fixed the Executing affix leaving fake entities.
* Fixed the Rebounding enchantment being unavailable.
* Fixed spawners not fully resetting entity NBT when changing mob types.
* Cleaving attacks on monsters will no longer hit any non-monster entities.
* Current HP damage will no longer be applied if the attack speed bar is not at least 75% charged.
  * It will still be scaled by the attack strength, so attacking exactly at 75% charge will yield 75% of the full-strength damage.

### Misc
* Added more default boss names.
  * Your /config/apotheosis/names.cfg file will not update automatically, you may want to delete it to receive the new entries!
* ZHAY10086: Updated chinese translation.
* Socket tooltips will now always be added to the socketed item.
* Updated enchanting stat descriptions in the Enchanting Table.

### Balance
* Nerfed critical hits again.  Since crits are multiplicative, higher values are very dangerous.
  * Crit Chance Gem Range: ~~[5%, 80%]~~ -> [5%, 25%]
  * Crit Damage Gem Range: ~~[5%, 30%]~~ -> [5%, 15%]
* Changed how the crit attributes interact with vanilla crits.
  * Vanilla crits will now be separate from the attributes, but will be multiplicative with them.
  * Crit Damage will increase vanilla crit damage, but it will not apply twice if a standard crit also occurs.
  * Overcrits are still possible.
* Endermite boss weight reduced from 80 to 10.
  * This change was made since an endermite boss constantly engages all endermen nearby.
* Instant potion affixes will now have a cooldown between activations.

## 5.6.1
* Hotfix for a crash caused by dimensions without affix loot entries.

## 5.6.0
* Consider this to be "Major Balance Pass #1"
* Bosses now have per-rarity scaling!
  * This means that bosses will have adequately scaled difficulties based on their rewards.
  * The old system simply had rarity selection be independent of boss stats.
  * Bosses may seem significantly weaker (except mythic and perhaps epic bosses).
* Fixed the executing affix crashing clients.
* Fixed a few adventure.cfg options being in the wrong categories.
  * This may invalidate some config changes - re-check them!
* Fixed twilight Affix Loot Entries not having the correct conditions.
* Fixed generation attempts configs not being read from the file.
* Fixed an issue where elemental damages were causing mobs to not drop loot or play death sounds.
* Made it so gems can have per-dimension drops instead of all being global (like bosses and affix loot).
  * This has not yet been implemented on individual gem jsons.
* Made bosses respect luck through quality levels (higher luck can bring forth stronger bosses).
* Natural bosses will glow briefly on spawn.
* Restricted rarity levels on a per-dimension bases.
  * Overworld: Common to Rare
  * Nether and Twilght: Uncommon to Epic
  * End: Rare to Mythic
  * Bosses do not explicitly follow those guidelines.
* Added more configs to adjust boss announcements.
* Reduced gem stats across the board, and adjusted them to be + % Base instead of + % Total.
* Adjusted default boss spawn rates.
* Nerfed crit affixes.  Multiplicative things are scary, mk?

## 5.5.1
* Hotfix for some more really weird array-list-index bugs that should be impossible.

## 5.5.0
* Added dimensional bosses - each dimension will now have their own boss spawn tables.
  * Overworld bosses will be weaker than Nether bosses, which will be weaker than End bosses.
  * This creates a sort of progression between dimensions, and stops things like Netherite gear being obtained at the start of gameplay.
* Added a ton more entries to the boss tables, compared to the previous 6 total unique boss entities.
* Added builtin support for Twilight Forest.  Gear found here is rouhgly on-par with the nether.
* Added leather, chain, and gold to the boss armor tables (in their appropriate dimensions).
* Bosses still need more work, mostly stat scaling with rarity and some other things, stay tuned for that. (eta 5.5.x or 5.6.0)
* Fixed affix items spawning on non-monsters (bats, fish, etc).
* Added dimensional affix loot tables.  Gems are currently still global, will address if that needs per-dim logic later.
* Updated the way that the selection process for affix loot (and gems) works in chests.
  * You can now specify pairs of regex patterns and drop chances for loot tables matching that pattern, allowing more fine-grained control.
* Balance is still a disaster!  Hopefully addressing that soon.
  * Thoughts so far:
  * Attribute bonus numbers across the board probably need to be reduced.
  * Gems need to be swapped from + %Total to + %Base, since + %Total is multiplicative and gem-spamming is silly.
  * Critical Strike attributes need nerfs also due to multiplicative nonsense.
  * Potentially per-dimming rarities, similar to how items and bosses were per-dimmed this update.

## 5.4.2
* This update never released, this is technically part of 5.5.0's changelog, but it was written separately.
* Fixed a crash that occurred when the Adventure Module is disabled.
* Fixed the APOTH_REMOVE_MARKER showing up in so many places
  * Technically this only occurs when something isn't fully handling tooltips, but I can sort of mitigate it on my end.
  * It still shows up in some cases, but that requires the other mods to fix their tooltip handling.
* Added compat with Gateways to Eternity.  An example is provided at data/apotheosis/gateways/boss_gate_small.json
* Added a config option for the Gem Boss Bonus drop change, which is added to the normal drop chance for bosses.
  * Previously it was a flat 33% and could not be changed.
* Added a config option to prevent block damage from explosive arrows.
* Made the boss "nbt" field get applied both before {as load()} and after {as readAdditionalSaveData()} the boss is modified.
* Fixed some crashes involving Uncommon pickaxes.

## 5.4.1
* Disabled debug logging for adventure module world generation.
  * This isn't really a big deal but it's very log-spammy.
* Fixed a crash that would occur when using a spyglass (or other charged item).
* Fixed boss spawners not operating at all.

## 5.4.0
* The Deadly Module is back!
  * However, it's actually called the Adventure Module now.
  * The module mainly contains a fully revamped Affix Loot system, and verbatim ports of the Boss and Rogue Spawner systems.
  * Reworks to Bosses and Rogue Spawners will be coming later.
  * Affix Loot can be obtained from bosses, naturally spawned mobs, loot chests, and wandering traders.
* Fixed Silent modifier not showing in spawner tooltips.
* Fixed a crash when modules were disabled.
* Fixed shield bash applying when in the offhand
* Fixed crashes if you somehow managed to obtain the Infusion enchantment
  * You aren't supposed to be able to get this by any means.
* Added a config to allow certain potions to be marked as "Extended Duration" for potion charms.
  * This helps prevent weirdness with stuff that breaks when it expires and reapplies rapidly.
* Draconic Endshelves have been buffed to 5 Eterna (was 4) so you only need 2 total dragon heads to max out.
* More Wandering Traders should spawn, but they'll also despawn a bit faster.
  * They can also spawn underground.
* Made enchantment tables ignore water during checks.

## 5.3.6
* HolyDiana: Updated Chinese Translation.
* Fixed issues with modded anvils.
* Added a speed limit to Chainsaw so it can't stall servers.
* Fixed issues with recipe types.
* Fixed modified spawners not saving.

## 5.3.5
* The Up-Back-Porting Update!
  * Because this includes all the changes that were made during the 1.16.5 backports.
* JEI will now display if spawner silk touch is disabled, always enabled, or requires a certain level.
  * Previously it only displayed if it required a certain level.
* Updated to Placebo 6.4.0
  * Some fixes made in Placebo impact Apotheosis content.
* Fixed spawners never invoking finalizeSpawn.
  * This would cause slimes to always be the smallest size, among other bugs.
* Fixed compat with Goblin Traders.
* Added tooltips for Enchanting Stats and the currently computed Table Stats to ToP/Jade
* Added the Silent spawner modifier.
* Spawner entity names will now be shown in the item name.
* Added Filters to the Enchantment Library!
  * You can filter on both enchantment name and applicable item.
* Added a config option for the Chronicle of Shadows.
* Allowed Capturing on Axes.
* Made KoTA convert extra drops from Scavenger and Spearfishing.

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