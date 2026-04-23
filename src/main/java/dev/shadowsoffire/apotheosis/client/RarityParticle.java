package dev.shadowsoffire.apotheosis.client;

import dev.shadowsoffire.apotheosis.particle.RarityParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class RarityParticle extends SingleQuadParticle {

    public RarityParticle(RarityParticleData data, ClientLevel level, double x, double y, double z, double velX, double velY, double velZ, TextureAtlasSprite sprite) {
        super(level, x, y, z, velX, velY, velZ, sprite);
        this.rCol = data.red();
        this.gCol = data.green();
        this.bCol = data.blue();
        this.lifetime = 80;
        this.xd = velX;
        this.yd = velY;
        this.zd = velZ;
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 1;
        this.quadSize = 0.05F + 0.03F * (float) level.getRandom().nextGaussian();
    }

    @Override
    public int getLightCoords(float partialTicks) {
        return 15728880;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float p_217561_1_) {
        return 0.75F * this.quadSize * Mth.clamp((this.age + p_217561_1_) / this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 0.75F * (1 - (float) this.age / this.lifetime);
    }

    public static class Provider implements ParticleProvider<RarityParticleData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(RarityParticleData data, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new RarityParticle(data, level, x, y, z, vx, vy, vz, this.sprites.get(random));
        }
    }

}
