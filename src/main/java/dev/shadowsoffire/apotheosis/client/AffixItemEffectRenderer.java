package dev.shadowsoffire.apotheosis.client;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRenderData;
import dev.shadowsoffire.apotheosis.loot.RarityRenderData.ShadowData;
import dev.shadowsoffire.apotheosis.particle.RarityParticleData;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

@EventBusSubscriber(modid = Apotheosis.MODID, bus = Bus.GAME, value = Dist.CLIENT)
public class AffixItemEffectRenderer {

    @SubscribeEvent
    public static void render(RenderLevelStageEvent e) {
        if (e.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) {
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
                    item.getPersistentData().putBoolean("apoth.beam_render", false);
                    continue;
                }

                if (!item.getPersistentData().getBoolean("apoth.beam_render")) {
                    item.getPersistentData().putBoolean("apoth.beam_render", true);
                    item.getPersistentData().putInt("apoth.beam_start_time", item.tickCount);
                }

                LootRarity rarity = rarityHolder.get();
                RarityRenderData renderData = rarity.renderData();
                int color = rarity.color().getValue();

                float partials = e.getPartialTick().getGameTimeDeltaPartialTick(false);
                float progress = Mth.clamp(item.tickCount - item.getPersistentData().getInt("apoth.beam_start_time") + partials, 0, 15) / 15F;

                Vec3 vec = e.getCamera().getPosition();
                double x = Mth.lerp(partials, item.xOld, item.getX());
                double y = Mth.lerp(partials, item.yOld, item.getY());
                double z = Mth.lerp(partials, item.zOld, item.getZ());

                pose.pushPose();
                pose.translate(x - vec.x, y - vec.y, z - vec.z);

                pose.pushPose();
                pose.translate(-item.getBbWidth() * 2, 0, -item.getBbWidth() * 2);

                final float beamHeight = renderData.beamHeight();
                final float beamRadius = renderData.beamRadius();
                final float glowRadius = renderData.glowRadius();

                int alphaZero = 0;
                int alphaLow = 0x1F << 24;
                int alphaMax = 0x9F << 24;

                float height = beamHeight * progress;

                if (beamHeight > 0) {
                    renderBeaconBeam(pose, buf, renderData.beamTexture(), renderData.glowTexture(), partials, 1, p.level().getGameTime(),
                        0, Math.min(height, 0.5F), alphaZero | color, alphaLow | color, beamRadius, glowRadius);
                    height -= 0.5F;

                    renderBeaconBeam(pose, buf, renderData.beamTexture(), renderData.glowTexture(), partials, 1, p.level().getGameTime(),
                        0.5F, Math.clamp(height, 0, 0.5F), alphaLow | color, alphaMax | color, beamRadius, glowRadius);
                    height -= 0.5F;

                    renderBeaconBeam(pose, buf, renderData.beamTexture(), renderData.glowTexture(), partials, 1, p.level().getGameTime(),
                        1, Math.clamp(height, 0, 0.5F), alphaMax | color, alphaMax | color, beamRadius, glowRadius);
                    height -= 0.5F;

                    renderBeaconBeam(pose, buf, renderData.beamTexture(), renderData.glowTexture(), partials, 1, p.level().getGameTime(),
                        1.5F, Math.clamp(height, 0, beamHeight), alphaMax | color, alphaZero | color, beamRadius, glowRadius);
                }

                pose.popPose();

                ShadowData data = renderData.shadow();

                double cameraDist = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(item.getX(), item.getY(), item.getZ());
                float weight = (float) ((1.0 - cameraDist / 256.0) * (double) 0.75F);
                if (weight > 0.0F) {
                    renderShadow(pose, buf, item, weight, partials, item.level(), data, FastColor.ARGB32.color(data.alpha(), color));
                }

                pose.popPose();

                if (renderData.particle().enabled()) {
                    int delay = item.getPersistentData().getInt("apoth.particle_delay");
                    if (progress == 1 && item.tickCount - delay > 0) {
                        var opt = new RarityParticleData(FastColor.ARGB32.red(color) / 255F, FastColor.ARGB32.green(color) / 255F, FastColor.ARGB32.blue(color) / 255F);
                        RandomSource rand = item.getRandom();
                        double spread = 0.1;
                        item.level().addParticle(opt, item.getX() - spread + rand.nextDouble() * 2 * spread, item.getY(),
                            item.getZ() - spread + rand.nextDouble() * 2 * spread, 0, 0.03 + 0.005 * rand.nextGaussian(), 0);
                        item.getPersistentData().putInt("apoth.particle_delay", item.tickCount + 10 + rand.nextInt(15));
                    }
                }
            }
        }
    }

    private static void renderShadow(
        PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, ShadowData data, int color) {
        float size = data.size();
        if (size <= 0) {
            return;
        }

        double x = Mth.lerp((double) partialTicks, entity.xOld, entity.getX());
        double y = Mth.lerp((double) partialTicks, entity.yOld, entity.getY());
        double z = Mth.lerp((double) partialTicks, entity.zOld, entity.getZ());
        float f = Math.min(weight / 0.5F, size);
        int xMin = Mth.floor(x - (double) size);
        int xMax = Mth.floor(x + (double) size);
        int yMin = Mth.floor(y - 2);
        int yMax = Mth.floor(y);
        int zMin = Mth.floor(z - (double) size);
        int zMax = Mth.floor(z + (double) size);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vtx = buffer.getBuffer(RenderType.entityShadow(data.texture()));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int zi = zMin; zi <= zMax; zi++) {
            for (int xi = xMin; xi <= xMax; xi++) {
                pos.set(xi, 0, zi);
                ChunkAccess chunkaccess = level.getChunk(pos);

                for (int yi = yMin; yi <= yMax; yi++) {
                    pos.setY(yi);
                    renderBlockShadow(pose, vtx, entity, chunkaccess, partialTicks, level, pos, x, y, z, data, color);
                }
            }
        }
    }

    private static void renderBlockShadow(
        PoseStack.Pose pose,
        VertexConsumer vertexConsumer,
        Entity entity,
        ChunkAccess chunk,
        float partialTicks,
        LevelReader level,
        BlockPos pos,
        double x,
        double y,
        double z,
        ShadowData data,
        int color) {
        BlockPos blockpos = pos;
        BlockState blockstate = chunk.getBlockState(blockpos);
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            VoxelShape voxelshape = blockstate.getShape(chunk, blockpos);
            if (!voxelshape.isEmpty() && Block.isFaceFull(voxelshape, Direction.DOWN)) {
                final int frames = data.frames();
                final float frameTime = data.frameTime();
                final float frame = (int) ((Minecraft.getInstance().level.getGameTime() + partialTicks) / frameTime % frames);

                float size = data.size();
                AABB aabb = voxelshape.bounds();
                aabb = aabb.intersect(new AABB(entity.getX() - pos.getX() - size, 0, entity.getZ() - pos.getZ() - size, entity.getX() - pos.getX() + size, aabb.maxY, entity.getZ() - pos.getZ() + size));
                double minX = (double) pos.getX() + aabb.minX;
                double maxX = (double) pos.getX() + aabb.maxX;
                double minY = (double) pos.getY() + aabb.maxY;
                double minZ = (double) pos.getZ() + aabb.minZ;
                double maxZ = (double) pos.getZ() + aabb.maxZ;
                float xi = (float) (minX - x);
                float xp = (float) (maxX - x);
                float yi = (float) (minY - y);
                float zi = (float) (minZ - z);
                float zp = (float) (maxZ - z);

                float u1 = -xi / 2.0F / size + 0.5F;
                float u2 = -xp / 2.0F / size + 0.5F;
                float v1 = -zi / 2.0F / size + 0.5F;
                float v2 = -zp / 2.0F / size + 0.5F;

                shadowVertex(pose, vertexConsumer, color, xi, yi, zi, u1, v1 / frames + frame / frames);
                shadowVertex(pose, vertexConsumer, color, xi, yi, zp, u1, v2 / frames + frame / frames);
                shadowVertex(pose, vertexConsumer, color, xp, yi, zp, u2, v2 / frames + frame / frames);
                shadowVertex(pose, vertexConsumer, color, xp, yi, zi, u2, v1 / frames + frame / frames);
            }
        }
    }

    private static void shadowVertex(
        PoseStack.Pose pose, VertexConsumer consumer, int color, float offsetX, float offsetY, float offsetZ, float u, float v) {
        Vector3f vector3f = pose.pose().transformPosition(offsetX, offsetY, offsetZ, new Vector3f());
        consumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z(), color, u, v, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
    }

    public static void renderBeaconBeam(
        PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation beamLocation, ResourceLocation glowLocation, float partialTick, float textureScale,
        long gameTime, float yOffset, float height, int colorBot, int colorTop, float beamRadius, float glowRadius) {

        if (height < 0) {
            return;
        }

        float i = yOffset + height;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float f = (float) Math.floorMod(gameTime, 40) + partialTick;
        float f1 = height < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f3 = 0.0F;
        float f5 = 0.0F;
        float f6 = -beamRadius;
        float f7 = 0.0F;
        float f8 = 0.0F;
        float f9 = -beamRadius;
        float f10 = 0.0F;
        float f11 = 1.0F;
        float f12 = -1.0F + f2;
        float f13 = (float) height * textureScale * (0.5F / beamRadius) + f12;
        renderPart(
            poseStack,
            bufferSource.getBuffer(RenderType.beaconBeam(beamLocation, true)),
            colorBot,
            colorTop,
            yOffset,
            i,
            0.0F,
            beamRadius,
            beamRadius,
            0.0F,
            f6,
            0.0F,
            0.0F,
            f9,
            0.0F,
            1.0F,
            f13,
            f12);
        poseStack.popPose();
        f3 = -glowRadius;
        float f4 = -glowRadius;
        f5 = -glowRadius;
        f6 = -glowRadius;
        f10 = 0.0F;
        f11 = 1.0F;
        f12 = -1.0F + f2;
        f13 = (float) height * textureScale + f12;
        renderPart(
            poseStack,
            bufferSource.getBuffer(RenderType.beaconBeam(glowLocation, true)),
            FastColor.ARGB32.color(FastColor.ARGB32.alpha(colorBot) / 2, colorBot),
            FastColor.ARGB32.color(FastColor.ARGB32.alpha(colorTop) / 2, colorTop),
            yOffset,
            i,
            f3,
            f4,
            glowRadius,
            f5,
            f6,
            glowRadius,
            glowRadius,
            glowRadius,
            0.0F,
            1.0F,
            f13,
            f12);
        poseStack.popPose();
    }

    private static void renderPart(
        PoseStack poseStack,
        VertexConsumer consumer,
        int colorBot,
        int colorTop,
        float minY,
        float maxY,
        float x1,
        float z1,
        float x2,
        float z2,
        float x3,
        float z3,
        float x4,
        float z4,
        float minU,
        float maxU,
        float minV,
        float maxV) {
        PoseStack.Pose posestack$pose = poseStack.last();
        renderQuad(
            posestack$pose, consumer, colorBot, colorTop, minY, maxY, x1, z1, x2, z2, minU, maxU, minV, maxV);
        renderQuad(
            posestack$pose, consumer, colorBot, colorTop, minY, maxY, x4, z4, x3, z3, minU, maxU, minV, maxV);
        renderQuad(
            posestack$pose, consumer, colorBot, colorTop, minY, maxY, x2, z2, x4, z4, minU, maxU, minV, maxV);
        renderQuad(
            posestack$pose, consumer, colorBot, colorTop, minY, maxY, x3, z3, x1, z1, minU, maxU, minV, maxV);
    }

    private static void renderQuad(
        PoseStack.Pose pose,
        VertexConsumer consumer,
        int colorBot,
        int colorTop,
        float minY,
        float maxY,
        float minX,
        float minZ,
        float maxX,
        float maxZ,
        float minU,
        float maxU,
        float minV,
        float maxV) {
        addVertex(pose, consumer, colorTop, maxY, minX, minZ, maxU, minV);
        addVertex(pose, consumer, colorBot, minY, minX, minZ, maxU, maxV);
        addVertex(pose, consumer, colorBot, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, consumer, colorTop, maxY, maxX, maxZ, minU, minV);
    }

    private static void addVertex(
        PoseStack.Pose pose, VertexConsumer consumer, int color, float y, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880)
            .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

}
