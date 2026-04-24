package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.tags.PaintingVariantTags;

public class ApothPaintingTagsProvider extends PaintingVariantTagsProvider {

    public ApothPaintingTagsProvider(PackOutput output, CompletableFuture<Provider> provider) {
        super(output, provider, Apotheosis.MODID);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(Provider provider) {
        this.tag(PaintingVariantTags.PLACEABLE)
            .add(
                ApothPaintingsProvider.CRAIG,
                ApothPaintingsProvider.ENCHANTING_TABLE,
                ApothPaintingsProvider.GEMS,
                ApothPaintingsProvider.TOWER,
                ApothPaintingsProvider.WINDOW);
    }

}
