package dev.shadowsoffire.apotheosis.loot;

import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.resources.ResourceLocation;

/**
 * Render Data for {@link LootRarity}, which defines what kinds of effects will be shown when an affix item of that rarity is dropped in-world.
 * 
 * @param beamHeight  The height of the beam (and the glow) effect. Zero or less will disable both.
 * @param beamRadius  The radius of the beam effect. Zero or less will disable the beam.
 * @param beamTexture The texture of the beam effect. Ignored if {@code beamRadius} is zero or less.
 * @param glowRadius  The radius of the glow effect. Zero or less will disable the glow.
 * @param glowTexture The texture of the glow effect. Ignored if {@code glowRadius} is zero or less.
 * @param shadow      The shadow data for the item. This is used to render a shadow under the item when it is dropped in-world.
 */
public record RarityRenderData(float beamHeight, float beamRadius, ResourceLocation beamTexture, float glowRadius, ResourceLocation glowTexture, ShadowData shadow, ParticleData particle) {

    public static final RarityRenderData DEFAULT = new RarityRenderData(3.5F, 0.035F, Apotheosis.loc("textures/rarity/beam.png"), 0.065F, Apotheosis.loc("textures/rarity/glow.png"), ShadowData.DEFAULT, ParticleData.DEFAULT);

    public static final Codec<RarityRenderData> CODEC = RecordCodecBuilder.create(instance -> instance
        .group(
            Codec.floatRange(0, 256).fieldOf("beam_height").forGetter(RarityRenderData::beamHeight),
            Codec.floatRange(0, 5).fieldOf("beam_radius").forGetter(RarityRenderData::beamRadius),
            ResourceLocation.CODEC.fieldOf("beam_texture").forGetter(RarityRenderData::beamTexture),
            Codec.floatRange(0, 7).fieldOf("glow_radius").forGetter(RarityRenderData::glowRadius),
            ResourceLocation.CODEC.fieldOf("glow_texture").forGetter(RarityRenderData::glowTexture),
            ShadowData.CODEC.optionalFieldOf("shadow", ShadowData.DEFAULT).forGetter(RarityRenderData::shadow),
            ParticleData.CODEC.optionalFieldOf("particle", ParticleData.DEFAULT).forGetter(RarityRenderData::particle))
        .apply(instance, RarityRenderData::new));

    /**
     * The shadow data for a {@link RarityRenderData}.
     * 
     * @param size      The size of the shadow. This is some kind of fixed magic number. If the size is 0, the shadow will not be rendered.
     * @param alpha     The opacity of the shadow (0 is transparent, 1 is opaque).
     * @param texture   The texture of the shadow. The texture should either be a square or an X by N rectangle, where N is the number of frames in the animation.
     * @param frames    The number of frames in the shadow animation. If this is 1, the shadow will not animate.
     * @param frameTime The time in ticks between frames of the shadow animation.
     */
    public static record ShadowData(float size, int alpha, ResourceLocation texture, int frames, float frameTime) {

        public static final ShadowData DEFAULT = new ShadowData(0.35F, 0xFF, Apotheosis.loc("textures/rarity/shadow.png"), 1, 1);

        public static final Codec<ShadowData> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                Codec.floatRange(0, 2).fieldOf("size").forGetter(ShadowData::size),
                Codec.intRange(0, 255).fieldOf("alpha").forGetter(ShadowData::alpha),
                ResourceLocation.CODEC.fieldOf("texture").forGetter(ShadowData::texture),
                Codec.intRange(1, 128).fieldOf("frames").orElse(1).forGetter(ShadowData::frames),
                Codec.floatRange(0.5F, 40).fieldOf("frame_time").orElse(1F).forGetter(ShadowData::frameTime))
            .apply(instance, ShadowData::new));

        public static class Builder {
            private int alpha = DEFAULT.alpha;
            private float size = DEFAULT.size;
            private ResourceLocation texture = DEFAULT.texture;
            private int frames = DEFAULT.frames;
            private float frameTime = DEFAULT.frameTime;

            public Builder size(float size) {
                this.size = size;
                return this;
            }

            public Builder alpha(int alpha) {
                this.alpha = alpha;
                return this;
            }

            public Builder texture(ResourceLocation texture) {
                this.texture = texture;
                return this;
            }

            public Builder frames(int frames) {
                this.frames = frames;
                return this;
            }

            public Builder frameTime(float frameTime) {
                this.frameTime = frameTime;
                return this;
            }

            public ShadowData build() {
                return new ShadowData(this.size, this.alpha, this.texture, this.frames, this.frameTime);
            }
        }

    }

    /**
     * Particle settings. TODO: Flesh this out.
     */
    public static record ParticleData(boolean enabled) {
        public static final ParticleData DEFAULT = new ParticleData(false);

        public static final Codec<ParticleData> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                Codec.BOOL.fieldOf("enabled").forGetter(ParticleData::enabled))
            .apply(instance, ParticleData::new));
    }

    public static class Builder {
        private float beamRadius = DEFAULT.beamRadius;
        private float beamHeight = DEFAULT.beamHeight;
        private ResourceLocation beamTexture = DEFAULT.beamTexture;
        private float glowRadius = DEFAULT.glowRadius;
        private ResourceLocation glowTexture = DEFAULT.glowTexture;
        private ShadowData shadow = DEFAULT.shadow;
        private boolean hasParticles = DEFAULT.particle.enabled;

        public Builder beamHeight(float height) {
            this.beamHeight = height;
            return this;
        }

        public Builder beamRadius(float radius) {
            this.beamRadius = radius;
            return this;
        }

        public Builder beamTexture(ResourceLocation texture) {
            this.beamTexture = texture;
            return this;
        }

        public Builder glowRadius(float radius) {
            this.glowRadius = radius;
            return this;
        }

        public Builder glowTexture(ResourceLocation texture) {
            this.glowTexture = texture;
            return this;
        }

        public Builder shadow(UnaryOperator<ShadowData.Builder> config) {
            this.shadow = config.apply(new ShadowData.Builder()).build();
            return this;
        }

        public Builder particle(boolean particle) {
            this.hasParticles = particle;
            return this;
        }

        public RarityRenderData build() {
            return new RarityRenderData(this.beamHeight, this.beamRadius, this.beamTexture, this.glowRadius, this.glowTexture, this.shadow, new ParticleData(this.hasParticles));
        }
    }
}
