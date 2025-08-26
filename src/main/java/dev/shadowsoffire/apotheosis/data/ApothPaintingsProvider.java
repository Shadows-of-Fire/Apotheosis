package dev.shadowsoffire.apotheosis.data;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class ApothPaintingsProvider {

    public static final ResourceKey<PaintingVariant> CRAIG = create("craig");
    public static final ResourceKey<PaintingVariant> ENCHANTING_TABLE = create("enchanting_table");
    public static final ResourceKey<PaintingVariant> GEMS = create("gems");
    public static final ResourceKey<PaintingVariant> TOWER = create("tower");
    public static final ResourceKey<PaintingVariant> WINDOW = create("window");

    public static void bootstrap(BootstrapContext<PaintingVariant> ctx) {
        register(ctx, CRAIG, 1, 2);
        register(ctx, ENCHANTING_TABLE, 2, 2);
        register(ctx, GEMS, 1, 1);
        register(ctx, TOWER, 1, 2);
        register(ctx, WINDOW, 2, 4);
    }

    private static void register(BootstrapContext<PaintingVariant> context, ResourceKey<PaintingVariant> key, int width, int height) {
        context.register(key, new PaintingVariant(width, height, key.location()));
    }

    private static ResourceKey<PaintingVariant> create(String name) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, Apotheosis.loc(name));
    }

}
