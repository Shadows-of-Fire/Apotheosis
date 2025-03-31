package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ApothTagsProvider extends EnchantmentTagsProvider {

    public ApothTagsProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Apotheosis.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {

    }

}
