package dev.shadowsoffire.apotheosis.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

public class MiscDatagenCode {

    private static final DynamicHolder<LootRarity> COMMON = RarityRegistry.INSTANCE.holder(Apotheosis.loc("common"));
    private static final DynamicHolder<LootRarity> UNCOMMON = RarityRegistry.INSTANCE.holder(Apotheosis.loc("uncommon"));
    private static final DynamicHolder<LootRarity> RARE = RarityRegistry.INSTANCE.holder(Apotheosis.loc("rare"));
    private static final DynamicHolder<LootRarity> EPIC = RarityRegistry.INSTANCE.holder(Apotheosis.loc("epic"));
    private static final DynamicHolder<LootRarity> MYTHIC = RarityRegistry.INSTANCE.holder(Apotheosis.loc("mythic"));
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void genAffixLootItems() {
        Set<ResourceLocation> overworld = ImmutableSet.of(new ResourceLocation("overworld"));
        Set<ResourceLocation> nether = ImmutableSet.of(new ResourceLocation("the_nether"));
        Set<ResourceLocation> end = ImmutableSet.of(new ResourceLocation("the_end"));

        BiConsumer<String, AffixLootEntry> writerFunc = (dim, entry) -> {
            if (entry.getType().isNone()) return;
            File file = new File(FMLPaths.GAMEDIR.get().toFile(), "datagen/" + dim + "/" + ForgeRegistries.ITEMS.getKey(entry.getStack().getItem()).getPath() + ".json");
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                JsonWriter jWriter = new JsonWriter(writer);
                jWriter.setIndent("    ");
                JsonElement json = AffixLootEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry).get().left().get();
                GSON.toJson(json, jWriter);
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };

        for (Item i : ForgeRegistries.ITEMS) {
            if (i instanceof TieredItem t && t.getTier() instanceof Tiers tier) {
                if (tier.ordinal() > Tiers.STONE.ordinal() && tier.ordinal() <= Tiers.IRON.ordinal()) {
                    AffixLootEntry entry = new AffixLootEntry(100 - 15 * (tier.ordinal() - 1), 1 + 3 * (tier.ordinal() - 1), new ItemStack(i), overworld, COMMON, RARE);
                    writerFunc.accept("overworld", entry);
                }

                if (tier.ordinal() >= Tiers.IRON.ordinal() && tier.ordinal() <= Tiers.DIAMOND.ordinal() || tier == Tiers.GOLD) {
                    int weight = tier == Tiers.GOLD ? 85 : 100 - 30 * (tier.ordinal() - 2);
                    int quality = tier == Tiers.GOLD ? 5 : 1 + 5 * (tier.ordinal() - 2);
                    AffixLootEntry entry = new AffixLootEntry(weight, quality, new ItemStack(i), nether, UNCOMMON, EPIC);
                    writerFunc.accept("the_nether", entry);
                }

                if (tier.ordinal() >= Tiers.DIAMOND.ordinal()) {
                    int weight = tier == Tiers.DIAMOND ? 100 : 70;
                    int quality = tier == Tiers.DIAMOND ? 5 : 10;
                    AffixLootEntry entry = new AffixLootEntry(weight, quality, new ItemStack(i), end, RARE, MYTHIC);
                    writerFunc.accept("the_end", entry);
                }
            }
            else if (i instanceof ArmorItem a && a.getMaterial() instanceof ArmorMaterials mat) {
                if (mat.ordinal() <= ArmorMaterials.IRON.ordinal()) {
                    AffixLootEntry entry = new AffixLootEntry(100 - 15 * mat.ordinal(), 1 + 2 * mat.ordinal(), new ItemStack(i), overworld, COMMON, RARE);
                    writerFunc.accept("overworld", entry);
                }

                if (mat.ordinal() >= ArmorMaterials.IRON.ordinal() && mat.ordinal() <= ArmorMaterials.DIAMOND.ordinal()) {
                    AffixLootEntry entry = new AffixLootEntry(100 - 15 * (mat.ordinal() - 2), 1 + 2 * (mat.ordinal() - 2), new ItemStack(i), nether, UNCOMMON, EPIC);
                    writerFunc.accept("the_nether", entry);
                }

                if (mat == ArmorMaterials.DIAMOND || mat == ArmorMaterials.NETHERITE) {
                    int weight = mat == ArmorMaterials.DIAMOND ? 100 : 70;
                    int quality = mat == ArmorMaterials.DIAMOND ? 5 : 10;
                    AffixLootEntry entry = new AffixLootEntry(weight, quality, new ItemStack(i), end, RARE, MYTHIC);
                    writerFunc.accept("the_end", entry);
                }
            }
        }
    }

}
