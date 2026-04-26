package dev.shadowsoffire.apotheosis.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

/**
 * Dispatches rendering of gem items to a per-gem baked item model based on the ID of the {@link Gem}
 * stored on the stack via {@link Components#GEM}.
 * <p>
 * The per-gem models are registered as standalone models during {@code ModelEvent.RegisterStandalone}
 * (see {@code AdventureModuleClient.addGemModels}). That handler scans {@code assets/<ns>/models/item/gems/**}
 * and publishes one {@link StandaloneModelKey} per gem into {@link #GEM_MODEL_KEYS}. When a gem is
 * bound, the looked-up standalone model renders; otherwise the configured fallback is used.
 */
public class GemModel implements ItemModel {

    public static final Map<DynamicHolder<Gem>, StandaloneModelKey<ItemModel>> GEM_MODEL_KEYS = new ConcurrentHashMap<>();

    private final ItemModel fallback;

    public GemModel(ItemModel fallback) {
        this.fallback = fallback;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
        ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        DynamicHolder<Gem> holder = stack.get(Components.GEM);
        if (holder != null && holder.isBound()) {
            StandaloneModelKey<ItemModel> key = GEM_MODEL_KEYS.get(holder);
            if (key != null) {
                ItemModel model = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
                if (model != null) {
                    model.update(output, stack, resolver, displayContext, level, owner, seed);
                    return;
                }
            }
        }
        this.fallback.update(output, stack, resolver, displayContext, level, owner, seed);
    }

    public record Unbaked(ItemModel.Unbaked fallback) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ItemModels.CODEC.fieldOf("fallback").forGetter(Unbaked::fallback))
            .apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return new GemModel(this.fallback.bake(context, transformation));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.fallback.resolveDependencies(resolver);
        }
    }
}
