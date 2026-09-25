package dev.shadowsoffire.apotheosis.data.twilight;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.compat.enchanting.ApothicEnchantingCompat;
import dev.shadowsoffire.apotheosis.compat.spawners.ApothicSpawnersCompat;
import dev.shadowsoffire.apotheosis.data.GearSetProvider;
import dev.shadowsoffire.apothic_enchanting.Ench;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import twilightforest.init.TFItems;

public class TwilightGearSetProvider extends GearSetProvider {

    public TwilightGearSetProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "Twilight Gear Sets";
    }

    @Override
    public void generate() {
        HolderLookup.Provider registries = this.lookupProvider.join();
        RegistryLookup<Enchantment> enchants = registries.lookup(Registries.ENCHANTMENT).get();

        // Haven Sets

        // Frontier Sets
        addSet("frontier/twilight/ironwood", 35, 0, c -> c
            .mainhand(new ItemStack(TFItems.IRONWOOD_SWORD.value()), 10)
            .mainhand(new ItemStack(TFItems.IRONWOOD_AXE.value()), 10)
            .mainhand(new ItemStack(TFItems.IRONWOOD_PICKAXE.value()), 10)
            .mainhand(new ItemStack(TFItems.IRONWOOD_SHOVEL.value()), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(TFItems.IRONWOOD_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.IRONWOOD_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.IRONWOOD_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.IRONWOOD_BOOTS.value()), 10)
            .tag("frontier_melee"));

        addSet("frontier/ranged/twilight/ironwood", 35, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 16)
            .mainhand(new ItemStack(Items.CROSSBOW), 4)
            .helmet(new ItemStack(TFItems.IRONWOOD_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.IRONWOOD_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.IRONWOOD_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.IRONWOOD_BOOTS.value()), 10)
            .tag("frontier_ranged"));

        addSet("frontier/twilight/steeleaf", 35, 0, c -> c
            .mainhand(new ItemStack(TFItems.STEELEAF_SWORD.value()), 10)
            .mainhand(new ItemStack(TFItems.STEELEAF_AXE.value()), 10)
            .mainhand(new ItemStack(TFItems.STEELEAF_PICKAXE.value()), 10)
            .mainhand(new ItemStack(TFItems.STEELEAF_SHOVEL.value()), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(TFItems.STEELEAF_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.STEELEAF_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.STEELEAF_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.STEELEAF_BOOTS.value()), 10)
            .tag("frontier_melee"));

        addSet("frontier/ranged/twilight/steeleaf", 35, 0, c -> c
            .mainhand(new ItemStack(Items.BOW), 16)
            .mainhand(new ItemStack(Items.CROSSBOW), 4)
            .helmet(new ItemStack(TFItems.STEELEAF_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.STEELEAF_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.STEELEAF_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.STEELEAF_BOOTS.value()), 10)
            .tag("frontier_ranged"));

        addSet("frontier/twilight/knightmetal", 5, 2.5F, c -> c
            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_SWORD.value()), 10)
            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_AXE.value()), 10)
            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_PICKAXE.value()), 10)
            .offhand(new ItemStack(Items.SHIELD), 8)
            .offhand(new ItemStack(TFItems.KNIGHTMETAL_SHIELD.value()), 2)
            .helmet(new ItemStack(TFItems.KNIGHTMETAL_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.KNIGHTMETAL_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.KNIGHTMETAL_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.KNIGHTMETAL_BOOTS.value()), 10)
            .tag("frontier_melee"));

        // Ascent Sets
        addSet("ascent/twilight/knightmetal", 35, 0, c -> c
            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_SWORD.value()), 10)
            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_AXE.value()), 10)
            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_PICKAXE.value()), 10)
            .offhand(new ItemStack(Items.SHIELD), 8)
            .offhand(new ItemStack(TFItems.KNIGHTMETAL_SHIELD.value()), 2)
            .helmet(new ItemStack(TFItems.KNIGHTMETAL_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.KNIGHTMETAL_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.KNIGHTMETAL_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.KNIGHTMETAL_BOOTS.value()), 10)
            .tag("ascent_melee"));

        addSet("ascent/twilight/arctic", 25, 2.5F, c -> c
            .mainhand(new ItemStack(TFItems.ICE_SWORD.value()), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(TFItems.ARCTIC_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.ARCTIC_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.ARCTIC_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.ARCTIC_BOOTS.value()), 10)
            .tag("ascent_melee"));

        addSet("ascent/ranged/twilight/arctic", 25, 2.5F, c -> c
            .mainhand(new ItemStack(Items.BOW), 12)
            .mainhand(new ItemStack(Items.CROSSBOW), 10)
            .mainhand(new ItemStack(TFItems.ICE_BOW.value()), 2)
            .helmet(new ItemStack(TFItems.ARCTIC_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.ARCTIC_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.ARCTIC_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.ARCTIC_BOOTS.value()), 10)
            .tag("ascent_ranged"));

        addSet("ascent/twilight/fiery", 25, 2.5F, c -> c
            .mainhand(new ItemStack(TFItems.FIERY_SWORD.value()), 10)
            .mainhand(new ItemStack(TFItems.DIAMOND_MINOTAUR_AXE.value()), 10)
            .mainhand(new ItemStack(TFItems.FIERY_PICKAXE.value()), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(new ItemStack(TFItems.FIERY_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.FIERY_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.FIERY_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.FIERY_BOOTS.value()), 10)
            .tag("ascent_melee"));

        addSet("ascent/ranged/twilight/fiery", 25, 2.5F, c -> c
            .mainhand(new ItemStack(Items.BOW), 12)
            .mainhand(new ItemStack(Items.CROSSBOW), 10)
            .mainhand(new ItemStack(TFItems.SEEKER_BOW.value()), 2)
            .helmet(new ItemStack(TFItems.FIERY_HELMET.value()), 10)
            .chestplate(new ItemStack(TFItems.FIERY_CHESTPLATE.value()), 10)
            .leggings(new ItemStack(TFItems.FIERY_LEGGINGS.value()), 10)
            .boots(new ItemStack(TFItems.FIERY_BOOTS.value()), 10)
            .tag("ascent_ranged"));

        // Summit Sets
        addSet("summit/twilight/enchanted_arctic", 40, 2.5F, c -> c
            .mainhand(buffedItem(TFItems.ICE_SWORD.value(), enchants, 1.5F), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(buffedItem(TFItems.ARCTIC_HELMET.value(), enchants, 1.5F), 10)
            .chestplate(buffedItem(TFItems.ARCTIC_CHESTPLATE.value(), enchants, 1.5F), 10)
            .leggings(buffedItem(TFItems.ARCTIC_LEGGINGS.value(), enchants, 1.5F), 10)
            .boots(buffedItem(TFItems.ARCTIC_BOOTS.value(), enchants, 1.5F), 10)
            .tag("summit_melee"));

        addSet("summit/ranged/twilight/enchanted_arctic", 40, 2.5F, c -> c
            .mainhand(new ItemStack(Items.BOW), 12)
            .mainhand(new ItemStack(Items.CROSSBOW), 10)
            .mainhand(buffedItem(TFItems.ICE_BOW.value(), enchants, 1.5F), 2)
            .helmet(buffedItem(TFItems.ARCTIC_HELMET.value(), enchants, 1.5F), 10)
            .chestplate(buffedItem(TFItems.ARCTIC_CHESTPLATE.value(), enchants, 1.5F), 10)
            .leggings(buffedItem(TFItems.ARCTIC_LEGGINGS.value(), enchants, 1.5F), 10)
            .boots(buffedItem(TFItems.ARCTIC_BOOTS.value(), enchants, 1.5F), 10)
            .tag("summit_ranged"));

        addSet("summit/twilight/enchanted_fiery", 40, 2.5F, c -> c
            .mainhand(buffedItem(TFItems.FIERY_SWORD.value(), enchants, 1.5F), 10)
            .mainhand(buffedItem(TFItems.DIAMOND_MINOTAUR_AXE.value(), enchants, 1.5F), 10)
            .mainhand(buffedItem(TFItems.FIERY_PICKAXE.value(), enchants, 1.5F), 10)
            .offhand(new ItemStack(Items.SHIELD), 10)
            .helmet(buffedItem(TFItems.FIERY_HELMET.value(), enchants, 1.5F), 10)
            .chestplate(buffedItem(TFItems.FIERY_CHESTPLATE.value(), enchants, 1.5F), 10)
            .leggings(buffedItem(TFItems.FIERY_LEGGINGS.value(), enchants, 1.5F), 10)
            .boots(buffedItem(TFItems.FIERY_BOOTS.value(), enchants, 1.5F), 10)
            .tag("summit_melee"));

        addSet("summit/ranged/twilight/enchanted_fiery", 40, 2.5F, c -> c
            .mainhand(new ItemStack(Items.BOW), 12)
            .mainhand(new ItemStack(Items.CROSSBOW), 10)
            .mainhand(buffedItem(TFItems.SEEKER_BOW.value(), enchants, 1.5F), 2)
            .helmet(buffedItem(TFItems.FIERY_HELMET.value(), enchants, 1.5F), 10)
            .chestplate(buffedItem(TFItems.FIERY_CHESTPLATE.value(), enchants, 1.5F), 10)
            .leggings(buffedItem(TFItems.FIERY_LEGGINGS.value(), enchants, 1.5F), 10)
            .boots(buffedItem(TFItems.FIERY_BOOTS.value(), enchants, 1.5F), 10)
            .tag("summit_ranged"));

        // Pinnacle
        // Chase items, following the rules in GearSetProvider#CHASE_LEVEL: one enchantment per item at the chase level.
        addSet("pinnacle/enchanted_yeti", 35, 5, c -> chaseArmor(chaseTools(c, registries)
            .mainhand(chaseItem(TFItems.GIANT_SWORD.value(), registries, Enchantments.SHARPNESS), 4)
            .mainhand(chaseItem(TFItems.GLASS_SWORD.value(), registries, Enchantments.SHARPNESS), 2)
            .mainhand(chaseItem(TFItems.GIANT_PICKAXE.value(), registries, Enchantments.FORTUNE), 4)
            .offhand(new ItemStack(TFItems.KNIGHTMETAL_SHIELD.value()), 10),
            registries, TFItems.YETI_HELMET.value(), TFItems.YETI_CHESTPLATE.value(), TFItems.YETI_LEGGINGS.value(), TFItems.YETI_BOOTS.value())
            .tag("pinnacle_melee"));

        addSet("pinnacle/ranged/enchanted_yeti", 35, 5, c -> chaseArmor(c
            .mainhand(chaseItem(TFItems.TRIPLE_BOW.value(), registries, Enchantments.POWER), 2)
            .mainhand(chaseItem(TFItems.ENDER_BOW.value(), registries, Enchantments.POWER), 2)
            .mainhand(chaseItem(TFItems.SEEKER_BOW.value(), registries, Enchantments.POWER), 2)
            .mainhand(chaseItem(TFItems.ICE_BOW.value(), registries, Enchantments.POWER), 2),
            registries, TFItems.YETI_HELMET.value(), TFItems.YETI_CHESTPLATE.value(), TFItems.YETI_LEGGINGS.value(), TFItems.YETI_BOOTS.value())
            .tag("pinnacle_ranged"));

        // Apothic Enchanting variant, mirroring pinnacle/apothic/enchanted_netherite. Requires both Twilight Forest and Apothic Enchanting.
        // There is no ranged variant: the only ranged AE chase enchantment is for crossbows, which this set does not carry.
        addSet("pinnacle/apothic/enchanted_yeti", 14, 5, c -> apothicChaseArmor(chaseArmor(apothicChaseTools(c, registries)
            .mainhand(chaseItem(TFItems.GIANT_SWORD.value(), registries, Ench.Enchantments.SCAVENGER, REDUCED_CHASE_LEVEL), 4)
            .mainhand(chaseItem(TFItems.GIANT_PICKAXE.value(), registries, Ench.Enchantments.BOON_OF_THE_EARTH, REDUCED_CHASE_LEVEL), 4)
            .offhand(chaseItem(TFItems.KNIGHTMETAL_SHIELD.value(), registries, Ench.Enchantments.SHIELD_BASH), 5)
            .offhand(chaseItem(TFItems.KNIGHTMETAL_SHIELD.value(), registries, Ench.Enchantments.REFLECTIVE_DEFENSES), 5),
            registries, TFItems.YETI_HELMET.value(), TFItems.YETI_CHESTPLATE.value(), TFItems.YETI_LEGGINGS.value(), TFItems.YETI_BOOTS.value()),
            registries, TFItems.YETI_CHESTPLATE.value())
            .tag("pinnacle_melee"),
            new ModLoadedCondition(ApothicEnchantingCompat.MODID));

        // Apothic Spawners variant, mirroring pinnacle/spawners/enchanted_netherite. Requires both Twilight Forest and Apothic Spawners.
        addSet("pinnacle/spawners/enchanted_yeti", 7, 5, c -> chaseArmor(spawnersChaseTools(c, registries)
            .mainhand(chaseItem(TFItems.GIANT_SWORD.value(), registries, ApothicSpawnersCompat.CAPTURING, REDUCED_CHASE_LEVEL), 4)
            .offhand(new ItemStack(TFItems.KNIGHTMETAL_SHIELD.value()), 10),
            registries, TFItems.YETI_HELMET.value(), TFItems.YETI_CHESTPLATE.value(), TFItems.YETI_LEGGINGS.value(), TFItems.YETI_BOOTS.value())
            .tag("pinnacle_melee"),
            new ModLoadedCondition(ApothicSpawnersCompat.MODID));
    }

    @Override
    protected void addSet(String name, int weight, float quality, UnaryOperator<GSBuilder> config) {
        this.addConditionally(Apotheosis.loc(name), config.apply(new GSBuilder(weight, quality)).build(), new ModLoadedCondition("twilightforest"));
    }

    @Override
    protected void addSet(String name, int weight, float quality, UnaryOperator<GSBuilder> config, ICondition... conditions) {
        ICondition[] all = new ICondition[conditions.length + 1];
        all[0] = new ModLoadedCondition("twilightforest");
        System.arraycopy(conditions, 0, all, 1, conditions.length);
        this.addConditionally(Apotheosis.loc(name), config.apply(new GSBuilder(weight, quality)).build(), all);
    }

}
