package dev.shadowsoffire.apotheosis.client;

import dev.shadowsoffire.apotheosis.particle.RarityParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

public class RarityParticle extends TextureSheetParticle {

    public RarityParticle(RarityParticleData data, ClientLevel level, double x, double y, double z, double velX, double velY, double velZ) {
        super(level, x, y, z, velX, velY, velZ);
        this.rCol = data.red();
        this.gCol = data.green();
        this.bCol = data.blue();
        this.lifetime = 80;
        this.xd = velX;
        this.yd = velY;
        this.zd = velZ;
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 1;
        this.quadSize = 0.05F + 0.03F * (float) level.random.nextGaussian();
    }

    @Override
    protected int getLightColor(float partialTicks) {
        return LightTexture.pack(15, 15);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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

}
