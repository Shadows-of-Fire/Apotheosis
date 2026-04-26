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
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Dist.CLIENT)
public class AffixItemEffectRenderer {

    private static final float GROW_IN_TICKS = 15F;
    private static final int ALPHA_ZERO = 0;
    private static final int ALPHA_LOW = 0x1F;
    private static final int ALPHA_MAX = 0x9F;

    @SubscribeEvent
    public static void submitBeams(SubmitCustomGeometryEvent e) {
        if (!AdventureConfig.enableAffixItemEffects) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Vec3 camPos = e.getLevelRenderState().cameraRenderState.pos;
        PoseStack pose = e.getPoseStack();
        SubmitNodeCollector collector = e.getSubmitNodeCollector();
        float partials = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        long gameTime = mc.level.getGameTime();

        for (Entity ent : mc.level.entitiesForRendering()) {
            if (!(ent instanceof ItemEntity item)) {
                continue;
            }

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

            float progress = Mth.clamp(item.tickCount - item.getData(Apoth.Attachments.AFFIX_EFFECT_START_TIME) + partials, 0, GROW_IN_TICKS) / GROW_IN_TICKS;

            double x = Mth.lerp(partials, item.xOld, item.getX());
            double y = Mth.lerp(partials, item.yOld, item.getY());
            double z = Mth.lerp(partials, item.zOld, item.getZ());

            // Translate to the item entity in camera space; shadow rendering relies on this outer translation.
            pose.pushPose();
            pose.translate(x - camPos.x, y - camPos.y, z - camPos.z);

            float beamHeight = renderData.beamHeight();
            if (beamHeight > 0) {
                // Center the beam under the item (the beam origin is at the NW corner of a 1x1 block).
                pose.pushPose();
                pose.translate(-item.getBbWidth() * 2, 0, -item.getBbWidth() * 2);

                float beamRadius = renderData.beamRadius();
                float glowRadius = renderData.glowRadius();
                float height = beamHeight * progress;

                // Render four segments of the beam to produce a transparency gradient (fade in, ramp up, solid, fade out).
                BeamRenderer.renderBeaconBeam(pose, collector, renderData.beamTexture(), renderData.glowTexture(), partials, 1, gameTime,
                    0.0F, Math.min(height, 0.5F), color(ALPHA_ZERO, color), color(ALPHA_LOW, color), beamRadius, glowRadius);
                height -= 0.5F;

                BeamRenderer.renderBeaconBeam(pose, collector, renderData.beamTexture(), renderData.glowTexture(), partials, 1, gameTime,
                    0.5F, Mth.clamp(height, 0, 0.5F), color(ALPHA_LOW, color), color(ALPHA_MAX, color), beamRadius, glowRadius);
                height -= 0.5F;

                BeamRenderer.renderBeaconBeam(pose, collector, renderData.beamTexture(), renderData.glowTexture(), partials, 1, gameTime,
                    1.0F, Mth.clamp(height, 0, 0.5F), color(ALPHA_MAX, color), color(ALPHA_MAX, color), beamRadius, glowRadius);
                height -= 0.5F;

                BeamRenderer.renderBeaconBeam(pose, collector, renderData.beamTexture(), renderData.glowTexture(), partials, 1, gameTime,
                    1.5F, Mth.clamp(height, 0, beamHeight), color(ALPHA_MAX, color), color(ALPHA_ZERO, color), beamRadius, glowRadius);

                pose.popPose();
            }

            ShadowData shadow = renderData.shadow();
            ShadowRenderer.renderShadow(pose, collector, item, partials, mc.level, shadow, ARGB.color(shadow.alpha(), color));

            pose.popPose();
        }
    }

    private static int color(int alpha, int color) {
        return ARGB.color(alpha, color);
    }

    @SubscribeEvent
    public static void spawnParticles(ClientTickEvent.Post e) {
        if (!AdventureConfig.enableAffixItemEffects) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }

        for (Entity ent : mc.level.entitiesForRendering()) {
            if (!(ent instanceof ItemEntity item)) {
                continue;
            }

            ItemStack stack = item.getItem();
            DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
            if (!rarityHolder.isBound() || !item.onGround()) {
                continue;
            }

            LootRarity rarity = rarityHolder.get();
            if (!rarity.renderData().particle().enabled()) {
                continue;
            }

            int delay = item.getData(Apoth.Attachments.AFFIX_EFFECT_NEXT_PARTICLE_TIME);
            if (item.tickCount - delay <= 0) {
                continue;
            }

            int color = rarity.color().getValue();
            RarityParticleData opt = new RarityParticleData(ARGB.red(color) / 255F, ARGB.green(color) / 255F, ARGB.blue(color) / 255F);
            RandomSource rand = item.getRandom();
            double spread = 0.1;
            mc.level.addParticle(opt,
                item.getX() - spread + rand.nextDouble() * 2 * spread,
                item.getY(),
                item.getZ() - spread + rand.nextDouble() * 2 * spread,
                0, 0.03 + 0.005 * rand.nextGaussian(), 0);
            item.setData(Apoth.Attachments.AFFIX_EFFECT_NEXT_PARTICLE_TIME, item.tickCount + 10 + rand.nextInt(15));
        }
    }

}
