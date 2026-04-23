//package dev.shadowsoffire.apotheosis.data.twilight;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.function.UnaryOperator;
//
//import dev.shadowsoffire.apotheosis.Apotheosis;
//import dev.shadowsoffire.apotheosis.data.GearSetProvider;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.core.HolderLookup.Provider;
//import net.minecraft.core.HolderLookup.RegistryLookup;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.data.PackOutput;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.enchantment.Enchantment;
//import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
//import twilightforest.init.TFItems;
//
//public class TwilightGearSetProvider extends GearSetProvider {
//
//    public TwilightGearSetProvider(PackOutput output, CompletableFuture<Provider> registries) {
//        super(output, registries);
//    }
//
//    @Override
//    public String getName() {
//        return "Twilight Gear Sets";
//    }
//
//    @Override
//    public void generate() {
//        HolderLookup.Provider registries = this.lookupProvider.join();
//        RegistryLookup<Enchantment> enchants = registries.lookup(Registries.ENCHANTMENT).get();
//
//        // Haven Sets
//
//        // Frontier Sets
//        addSet("frontier/twilight/ironwood", 35, 0, c -> c
//            .mainhand(new ItemStack(TFItems.IRONWOOD_SWORD.value()), 10)
//            .mainhand(new ItemStack(TFItems.IRONWOOD_AXE.value()), 10)
//            .mainhand(new ItemStack(TFItems.IRONWOOD_PICKAXE.value()), 10)
//            .mainhand(new ItemStack(TFItems.IRONWOOD_SHOVEL.value()), 10)
//            .offhand(new ItemStack(Items.SHIELD), 10)
//            .helmet(new ItemStack(TFItems.IRONWOOD_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.IRONWOOD_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.IRONWOOD_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.IRONWOOD_BOOTS.value()), 10)
//            .tag("frontier_melee"));
//
//        addSet("frontier/ranged/twilight/ironwood", 35, 0, c -> c
//            .mainhand(new ItemStack(Items.BOW), 16)
//            .mainhand(new ItemStack(Items.CROSSBOW), 4)
//            .helmet(new ItemStack(TFItems.IRONWOOD_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.IRONWOOD_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.IRONWOOD_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.IRONWOOD_BOOTS.value()), 10)
//            .tag("frontier_ranged"));
//
//        addSet("frontier/twilight/steeleaf", 35, 0, c -> c
//            .mainhand(new ItemStack(TFItems.STEELEAF_SWORD.value()), 10)
//            .mainhand(new ItemStack(TFItems.STEELEAF_AXE.value()), 10)
//            .mainhand(new ItemStack(TFItems.STEELEAF_PICKAXE.value()), 10)
//            .mainhand(new ItemStack(TFItems.STEELEAF_SHOVEL.value()), 10)
//            .offhand(new ItemStack(Items.SHIELD), 10)
//            .helmet(new ItemStack(TFItems.STEELEAF_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.STEELEAF_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.STEELEAF_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.STEELEAF_BOOTS.value()), 10)
//            .tag("frontier_melee"));
//
//        addSet("frontier/ranged/twilight/steeleaf", 35, 0, c -> c
//            .mainhand(new ItemStack(Items.BOW), 16)
//            .mainhand(new ItemStack(Items.CROSSBOW), 4)
//            .helmet(new ItemStack(TFItems.STEELEAF_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.STEELEAF_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.STEELEAF_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.STEELEAF_BOOTS.value()), 10)
//            .tag("frontier_ranged"));
//
//        addSet("frontier/twilight/knightmetal", 5, 2.5F, c -> c
//            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_SWORD.value()), 10)
//            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_AXE.value()), 10)
//            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_PICKAXE.value()), 10)
//            .offhand(new ItemStack(Items.SHIELD), 8)
//            .offhand(new ItemStack(TFItems.KNIGHTMETAL_SHIELD.value()), 2)
//            .helmet(new ItemStack(TFItems.KNIGHTMETAL_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.KNIGHTMETAL_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.KNIGHTMETAL_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.KNIGHTMETAL_BOOTS.value()), 10)
//            .tag("frontier_melee"));
//
//        // Ascent Sets
//        addSet("ascent/twilight/knightmetal", 35, 0, c -> c
//            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_SWORD.value()), 10)
//            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_AXE.value()), 10)
//            .mainhand(new ItemStack(TFItems.KNIGHTMETAL_PICKAXE.value()), 10)
//            .offhand(new ItemStack(Items.SHIELD), 8)
//            .offhand(new ItemStack(TFItems.KNIGHTMETAL_SHIELD.value()), 2)
//            .helmet(new ItemStack(TFItems.KNIGHTMETAL_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.KNIGHTMETAL_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.KNIGHTMETAL_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.KNIGHTMETAL_BOOTS.value()), 10)
//            .tag("ascent_melee"));
//
//        addSet("ascent/twilight/arctic", 25, 2.5F, c -> c
//            .mainhand(new ItemStack(TFItems.ICE_SWORD.value()), 10)
//            .offhand(new ItemStack(Items.SHIELD), 10)
//            .helmet(new ItemStack(TFItems.ARCTIC_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.ARCTIC_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.ARCTIC_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.ARCTIC_BOOTS.value()), 10)
//            .tag("ascent_melee"));
//
//        addSet("ascent/ranged/twilight/arctic", 25, 2.5F, c -> c
//            .mainhand(new ItemStack(Items.BOW), 12)
//            .mainhand(new ItemStack(Items.CROSSBOW), 10)
//            .mainhand(new ItemStack(TFItems.ICE_BOW.value()), 2)
//            .helmet(new ItemStack(TFItems.ARCTIC_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.ARCTIC_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.ARCTIC_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.ARCTIC_BOOTS.value()), 10)
//            .tag("ascent_ranged"));
//
//        addSet("ascent/twilight/fiery", 25, 2.5F, c -> c
//            .mainhand(new ItemStack(TFItems.FIERY_SWORD.value()), 10)
//            .mainhand(new ItemStack(TFItems.DIAMOND_MINOTAUR_AXE.value()), 10)
//            .mainhand(new ItemStack(TFItems.FIERY_PICKAXE.value()), 10)
//            .offhand(new ItemStack(Items.SHIELD), 10)
//            .helmet(new ItemStack(TFItems.FIERY_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.FIERY_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.FIERY_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.FIERY_BOOTS.value()), 10)
//            .tag("ascent_melee"));
//
//        addSet("ascent/ranged/twilight/fiery", 25, 2.5F, c -> c
//            .mainhand(new ItemStack(Items.BOW), 12)
//            .mainhand(new ItemStack(Items.CROSSBOW), 10)
//            .mainhand(new ItemStack(TFItems.SEEKER_BOW.value()), 2)
//            .helmet(new ItemStack(TFItems.FIERY_HELMET.value()), 10)
//            .chestplate(new ItemStack(TFItems.FIERY_CHESTPLATE.value()), 10)
//            .leggings(new ItemStack(TFItems.FIERY_LEGGINGS.value()), 10)
//            .boots(new ItemStack(TFItems.FIERY_BOOTS.value()), 10)
//            .tag("ascent_ranged"));
//
//        // Summit Sets
//        addSet("summit/twilight/enchanted_arctic", 40, 2.5F, c -> c
//            .mainhand(buffedItem(TFItems.ICE_SWORD.value(), enchants, 1.5F), 10)
//            .offhand(new ItemStack(Items.SHIELD), 10)
//            .helmet(buffedItem(TFItems.ARCTIC_HELMET.value(), enchants, 1.5F), 10)
//            .chestplate(buffedItem(TFItems.ARCTIC_CHESTPLATE.value(), enchants, 1.5F), 10)
//            .leggings(buffedItem(TFItems.ARCTIC_LEGGINGS.value(), enchants, 1.5F), 10)
//            .boots(buffedItem(TFItems.ARCTIC_BOOTS.value(), enchants, 1.5F), 10)
//            .tag("summit_melee"));
//
//        addSet("summit/ranged/twilight/enchanted_arctic", 40, 2.5F, c -> c
//            .mainhand(new ItemStack(Items.BOW), 12)
//            .mainhand(new ItemStack(Items.CROSSBOW), 10)
//            .mainhand(buffedItem(TFItems.ICE_BOW.value(), enchants, 1.5F), 2)
//            .helmet(buffedItem(TFItems.ARCTIC_HELMET.value(), enchants, 1.5F), 10)
//            .chestplate(buffedItem(TFItems.ARCTIC_CHESTPLATE.value(), enchants, 1.5F), 10)
//            .leggings(buffedItem(TFItems.ARCTIC_LEGGINGS.value(), enchants, 1.5F), 10)
//            .boots(buffedItem(TFItems.ARCTIC_BOOTS.value(), enchants, 1.5F), 10)
//            .tag("summit_ranged"));
//
//        addSet("summit/twilight/enchanted_fiery", 40, 2.5F, c -> c
//            .mainhand(buffedItem(TFItems.FIERY_SWORD.value(), enchants, 1.5F), 10)
//            .mainhand(buffedItem(TFItems.DIAMOND_MINOTAUR_AXE.value(), enchants, 1.5F), 10)
//            .mainhand(buffedItem(TFItems.FIERY_PICKAXE.value(), enchants, 1.5F), 10)
//            .offhand(new ItemStack(Items.SHIELD), 10)
//            .helmet(buffedItem(TFItems.FIERY_HELMET.value(), enchants, 1.5F), 10)
//            .chestplate(buffedItem(TFItems.FIERY_CHESTPLATE.value(), enchants, 1.5F), 10)
//            .leggings(buffedItem(TFItems.FIERY_LEGGINGS.value(), enchants, 1.5F), 10)
//            .boots(buffedItem(TFItems.FIERY_BOOTS.value(), enchants, 1.5F), 10)
//            .tag("summit_melee"));
//
//        addSet("summit/ranged/twilight/enchanted_fiery", 40, 2.5F, c -> c
//            .mainhand(new ItemStack(Items.BOW), 12)
//            .mainhand(new ItemStack(Items.CROSSBOW), 10)
//            .mainhand(buffedItem(TFItems.SEEKER_BOW.value(), enchants, 1.5F), 2)
//            .helmet(buffedItem(TFItems.FIERY_HELMET.value(), enchants, 1.5F), 10)
//            .chestplate(buffedItem(TFItems.FIERY_CHESTPLATE.value(), enchants, 1.5F), 10)
//            .leggings(buffedItem(TFItems.FIERY_LEGGINGS.value(), enchants, 1.5F), 10)
//            .boots(buffedItem(TFItems.FIERY_BOOTS.value(), enchants, 1.5F), 10)
//            .tag("summit_ranged"));
//
//        // Pinnacle
//        addSet("pinnacle/enchanted_yeti", 35, 5, c -> c
//            .mainhand(buffedItem(Items.NETHERITE_SWORD, enchants, 3F), 10)
//            .mainhand(buffedItem(Items.NETHERITE_AXE, enchants, 3F), 10)
//            .mainhand(buffedItem(Items.NETHERITE_PICKAXE, enchants, 3F), 10)
//            .mainhand(buffedItem(Items.NETHERITE_SHOVEL, enchants, 3F), 10)
//            .mainhand(buffedItem(TFItems.GIANT_SWORD.value(), enchants, 3F), 4)
//            .mainhand(buffedItem(TFItems.GLASS_SWORD.value(), enchants, 3F), 2)
//            .mainhand(buffedItem(TFItems.GIANT_PICKAXE.value(), enchants, 3F), 4)
//            .offhand(new ItemStack(TFItems.KNIGHTMETAL_SHIELD.value()), 10)
//            .helmet(buffedItem(TFItems.YETI_HELMET.value(), enchants, 3F), 10)
//            .chestplate(buffedItem(TFItems.YETI_CHESTPLATE.value(), enchants, 3F), 10)
//            .leggings(buffedItem(TFItems.YETI_LEGGINGS.value(), enchants, 3F), 10)
//            .boots(buffedItem(TFItems.YETI_BOOTS.value(), enchants, 3F), 10)
//            .tag("pinnacle_melee"));
//
//        addSet("apotheosis/ranged/enchanted_netherite", 35, 5, c -> c
//            .mainhand(buffedItem(TFItems.TRIPLE_BOW.value(), enchants, 3F), 2)
//            .mainhand(buffedItem(TFItems.ENDER_BOW.value(), enchants, 3F), 2)
//            .mainhand(buffedItem(TFItems.SEEKER_BOW.value(), enchants, 3F), 2)
//            .mainhand(buffedItem(TFItems.ICE_BOW.value(), enchants, 3F), 2)
//            .helmet(buffedItem(TFItems.YETI_HELMET.value(), enchants, 3F), 10)
//            .chestplate(buffedItem(TFItems.YETI_CHESTPLATE.value(), enchants, 3F), 10)
//            .leggings(buffedItem(TFItems.YETI_LEGGINGS.value(), enchants, 3F), 10)
//            .boots(buffedItem(TFItems.YETI_BOOTS.value(), enchants, 3F), 10)
//            .tag("pinnacle_ranged"));
//    }
//
//    @Override
//    protected void addSet(String name, int weight, float quality, UnaryOperator<GSBuilder> config) {
//        this.addConditionally(Apotheosis.loc(name), config.apply(new GSBuilder(weight, quality)).build(), new ModLoadedCondition("twilightforest"));
//    }
//
//}
