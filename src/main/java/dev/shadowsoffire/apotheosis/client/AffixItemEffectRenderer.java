package dev.shadowsoffire.apotheosis.client;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRenderData;
import dev.shadowsoffire.apotheosis.loot.RarityRenderData.ShadowData;
import dev.shadowsoffire.apotheosis.particle.RarityParticleData;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Dist.CLIENT)
public class AffixItemEffectRenderer {

    @SubscribeEvent
    public static void render(RenderLevelStageEvent e) {
        if (e.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS || !AdventureConfig.enableAffixItemEffects) {
            return;
        }

        PoseStack pose = e.getPoseStack();
        Player p = Minecraft.getInstance().player;
        BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();

        for (Entity ent : Minecraft.getInstance().level.entitiesForRendering()) {
            if (ent instanceof ItemEntity item) {
                ItemStack stack = item.getItem();
                DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
                if (!rarityHolder.isBound() || !item.onGround()) {
                    item.setData(Apoth.Attachments.AFFIX_EFFECT_RENDER_STARTED, false);
                    continue;
                }

                if (!item.getData(Apoth.Attachments.AFFIX_EFFECT_RENDER_STARTED)) {
                    item.setData(Apoth.Attachments.AFFIX_EFFECT_RENDER_STARTED, true);
                    item.setData(Apoth.Attachments.AFFIX_EFFECT_START_TIME, item.tickCount);
                }

                LootRarity rarity = rarityHolder.get();
                RarityRenderData renderData = rarity.renderData();
                int color = rarity.color().getValue();

                float partials = e.getPartialTick().getGameTimeDeltaPartialTick(false);
                float progress = Mth.clamp(item.tickCount - item.getData(Apoth.Attachments.AFFIX_EFFECT_START_TIME) + partials, 0, 15) / 15F;

                Vec3 vec = e.getCamera().getPosition();
                double x = Mth.lerp(partials, item.xOld, item.getX());
                double y = Mth.lerp(partials, item.yOld, item.getY());
                double z = Mth.lerp(partials, item.zOld, item.getZ());

                // Translate to where the entity is in camera space.
                pose.pushPose();
                pose.translate(x - vec.x, y - vec.y, z - vec.z);

                // For the beam, translate to the center of the Item Entity.
                pose.pushPose();
                pose.translate(-item.getBbWidth() * 2, 0, -item.getBbWidth() * 2);

                final float beamHeight = renderData.beamHeight();
                final float beamRadius = renderData.beamRadius();
                final float glowRadius = renderData.glowRadius();
                final int alphaZero = 0;
                final int alphaLow = 0x1F;
                final int alphaMax = 0x9F;

                float height = beamHeight * progress;

                // TODO: These can be adjusted to reuse the same buffer, which should speed up the render a bit.
                // Right now they re-acquire the buffer on each call (and do so twice), which has poor batching and causes a lot of uploads.
                if (beamHeight > 0) {
                    // Render four segments of beacon beams to control the transparency gradient.
                    BeamRenderer.renderBeaconBeam(pose, buf, renderData.beamTexture(), renderData.glowTexture(), partials, 1, p.level().getGameTime(),
                        0, Math.min(height, 0.5F), color(alphaZero, color), color(alphaLow, color), beamRadius, glowRadius);
                    height -= 0.5F;

                    BeamRenderer.renderBeaconBeam(pose, buf, renderData.beamTexture(), renderData.glowTexture(), partials, 1, p.level().getGameTime(),
                        0.5F, Math.clamp(height, 0, 0.5F), color(alphaLow, color), color(alphaMax, color), beamRadius, glowRadius);
                    height -= 0.5F;

                    BeamRenderer.renderBeaconBeam(pose, buf, renderData.beamTexture(), renderData.glowTexture(), partials, 1, p.level().getGameTime(),
                        1, Math.clamp(height, 0, 0.5F), color(alphaMax, color), color(alphaMax, color), beamRadius, glowRadius);
                    height -= 0.5F;

                    BeamRenderer.renderBeaconBeam(pose, buf, renderData.beamTexture(), renderData.glowTexture(), partials, 1, p.level().getGameTime(),
                        1.5F, Math.clamp(height, 0, beamHeight), color(alphaMax, color), color(alphaZero, color), beamRadius, glowRadius);
                }

                pose.popPose();

                ShadowData data = renderData.shadow();

                ShadowRenderer.renderShadow(pose, buf, item, partials, item.level(), data, FastColor.ARGB32.color(data.alpha(), color));

                pose.popPose();

                if (renderData.particle().enabled()) {
                    int delay = item.getData(Apoth.Attachments.AFFIX_EFFECT_NEXT_PARTICLE_TIME);
                    if (progress == 1 && item.tickCount - delay > 0) {
                        var opt = new RarityParticleData(FastColor.ARGB32.red(color) / 255F, FastColor.ARGB32.green(color) / 255F, FastColor.ARGB32.blue(color) / 255F);
                        RandomSource rand = item.getRandom();
                        double spread = 0.1;
                        item.level().addParticle(opt, item.getX() - spread + rand.nextDouble() * 2 * spread, item.getY(),
                            item.getZ() - spread + rand.nextDouble() * 2 * spread, 0, 0.03 + 0.005 * rand.nextGaussian(), 0);
                        item.setData(Apoth.Attachments.AFFIX_EFFECT_NEXT_PARTICLE_TIME, item.tickCount + 10 + rand.nextInt(15));
                    }
                }
            }
        }
    }

    private static int color(int alpha, int color) {
        return FastColor.ARGB32.color(alpha, color);
    }

}
