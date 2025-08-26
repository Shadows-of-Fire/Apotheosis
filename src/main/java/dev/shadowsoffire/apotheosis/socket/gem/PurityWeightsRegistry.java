package dev.shadowsoffire.apotheosis.socket.gem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.PurityWeightsRegistry.PurityWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weight;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.resources.ResourceLocation;

public final class PurityWeightsRegistry extends DynamicRegistry<PurityWeights> {

    public static final PurityWeightsRegistry INSTANCE = new PurityWeightsRegistry();
    public static final ResourceLocation TARGET_FILE = Apotheosis.loc("weights");

    private static final Map<Purity, TieredWeights> ERRORED = Map.of(
        Purity.CRACKED, TieredWeights.forAllTiers(1, 0),
        Purity.CHIPPED, TieredWeights.EMPTY,
        Purity.FLAWED, TieredWeights.EMPTY,
        Purity.NORMAL, TieredWeights.EMPTY,
        Purity.FLAWLESS, TieredWeights.EMPTY,
        Purity.PERFECT, TieredWeights.EMPTY);

    private Map<Purity, TieredWeights> parsedWeights = Map.of();

    public PurityWeightsRegistry() {
        super(Apotheosis.LOGGER, "purity_weights", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("purity_weights"), PurityWeights.CODEC);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.parsedWeights = Map.of();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        if (this.registry.size() > 1) {
            this.logger.error("Additional purity weights files have been loaded. Only {} will be parsed.", TARGET_FILE);
        }

        if (this.registry.containsKey(TARGET_FILE)) {
            PurityWeights weights = this.registry.get(TARGET_FILE);

            Map<Purity, TieredWeights> weightMap = new HashMap<>();
            for (Purity p : Purity.ALL_PURITIES) {
                var builder = TieredWeights.builder();
                for (WorldTier tier : WorldTier.values()) {
                    Map<Purity, Weight> tierMap = weights.weights().getOrDefault(tier, Map.of());
                    Weight weight = tierMap.getOrDefault(p, Weight.ZERO);
                    builder.with(tier, weight);
                }
                weightMap.put(p, builder.build());
            }

            this.parsedWeights = Collections.unmodifiableMap(weightMap);
        }
        else {
            this.logger.error("Purity weights file {} not loaded! All purity weights will be set to zero, meaning only cracked gems will spawn.", TARGET_FILE);
        }
    }

    /**
     * Returns the active weights, or an errored variant if nothing was loaded.
     * <p>
     * The errored variant will only produce {@link Purity#CRACKED}.
     */
    public static Map<Purity, TieredWeights> getWeights() {
        return INSTANCE.parsedWeights.isEmpty() ? ERRORED : INSTANCE.parsedWeights;
    }

    public static record PurityWeights(Map<WorldTier, Map<Purity, Weight>> weights) implements CodecProvider<PurityWeights> {

        public static final Codec<PurityWeights> CODEC = WorldTier.mapCodec(Purity.mapCodec(Weight.CODEC.codec()).codec())
            .fieldOf("weights")
            .xmap(PurityWeights::new, PurityWeights::weights)
            .codec();

        @Override
        public Codec<? extends PurityWeights> getCodec() {
            return CODEC;
        }
    }

}
