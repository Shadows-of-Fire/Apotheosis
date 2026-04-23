package dev.shadowsoffire.apotheosis.client;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.effect.RadialAffix;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.RadialBonus;
import dev.shadowsoffire.apotheosis.util.RadialUtil;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialData;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

/**
 * Hooks to assist in rendering the crumbling block effect on blocks that will be broken by the radial effect (affix or gem bonus).
 * <p>
 * Largely inspired by the implementation from Tinker's Construct (MIT License).
 * https://github.com/SlimeKnights/TinkersConstruct/blob/1.20.1/src/main/java/slimeknights/tconstruct/tools/client/ToolRenderEvents.java
 */
@EventBusSubscriber(modid = Apotheosis.MODID, value = Dist.CLIENT)
public class RadialProgressTracker {

    @Nullable
    private static CacheKey lastKey = null;
    private static Set<BlockPos> knownAOEBlocks = Set.of();

    private static Set<BlockPos> getAOEBlocks() {
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        HitResult res = Minecraft.getInstance().hitResult;
        ItemStack tool = player == null ? ItemStack.EMPTY : player.getMainHandItem();

        if (level == null
            || player == null
            || res == null
            || res.getType() != Type.BLOCK
            || tool.isEmpty()
            || !RadialState.isRadialMiningEnabled(player)) {
            lastKey = null;
            knownAOEBlocks = Set.of();
            return knownAOEBlocks;
        }

        BlockHitResult blockTrace = (BlockHitResult) res;
        BlockPos pos = blockTrace.getBlockPos();
        Direction dir = blockTrace.getDirection();

        CacheKey key = new CacheKey(pos, tool, dir, player.getDirection());

        if (lastKey != null && lastKey.equals(key)) {
            return knownAOEBlocks;
        }

        lastKey = key;

        RadialData afxData = RadialAffix.getRadialData(tool);
        RadialData gemData = RadialBonus.getRadialData(tool);

        Set<BlockPos> positions = new HashSet<>();

        if (afxData != null) {
            positions.addAll(RadialUtil.getBrokenBlocks(player, dir, pos, afxData));
        }

        if (gemData != null) {
            positions.addAll(RadialUtil.getBrokenBlocks(player, dir, pos, gemData));
        }

        knownAOEBlocks = positions;
        return knownAOEBlocks;
    }

    public static void breakClientBlocks(MultiPlayerGameMode mode, BlockPos srcPos) {
        if (lastKey != null && lastKey.pos.equals(srcPos)) {
            Level level = Minecraft.getInstance().level;
            for (BlockPos pos : getAOEBlocks()) {
                // TODO: Re-enable this when we figure out a way to spawn less of the break particles.
                // Until then, this is off, because spawning in a 7x7's worth of particles is a bit much.
                // mode.destroyBlock(pos);
                level.removeBlock(pos, false); // We still want to remove the block, until we solve that problem though.
            }
            lastKey = null;
            knownAOEBlocks = Set.of();
        }
    }

    @SubscribeEvent
    public static void submitOutlines(SubmitCustomGeometryEvent e) {
        Set<BlockPos> blocks = getAOEBlocks();
        if (blocks.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        Vec3 camPos = e.getLevelRenderState().cameraRenderState.pos;
        PoseStack pose = e.getPoseStack();
        SubmitNodeCollector collector = e.getSubmitNodeCollector();

        for (BlockPos pos : blocks) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            VoxelShape shape = state.getShape(level, pos);
            if (shape.isEmpty()) continue;

            double x = pos.getX() - camPos.x;
            double y = pos.getY() - camPos.y;
            double z = pos.getZ() - camPos.z;

            collector.submitCustomGeometry(pose, RenderTypes.lines(), (p, buffer) -> {
                shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                    org.joml.Vector3f normal = new org.joml.Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1)).normalize();
                    buffer.addVertex(p, (float) (x1 + x), (float) (y1 + y), (float) (z1 + z))
                        .setColor(0, 0, 0, 102).setNormal(p, normal).setLineWidth(2F);
                    buffer.addVertex(p, (float) (x2 + x), (float) (y2 + y), (float) (z2 + z))
                        .setColor(0, 0, 0, 102).setNormal(p, normal).setLineWidth(2F);
                });
            });
        }

        submitCrumbling(e, blocks, mc, level, camPos, pose, collector);
    }

    /**
     * Mirrors the progress of the player's current destruction target onto each AOE block,
     * producing the vanilla crumbling overlay on the blocks that will also be destroyed.
     */
    private static void submitCrumbling(SubmitCustomGeometryEvent e, Set<BlockPos> blocks, Minecraft mc, Level level, Vec3 camPos, PoseStack pose, SubmitNodeCollector collector) {
        MultiPlayerGameMode controller = mc.gameMode;
        if (controller == null || !controller.isDestroying()) return;

        Player player = mc.player;
        if (player == null || lastKey == null) return;

        BlockPos target = lastKey.pos;
        int progress = -1;
        for (BlockBreakingRenderState entry : e.getLevelRenderState().blockBreakingRenderStates) {
            if (entry.blockPos().equals(target)) {
                progress = entry.progress();
                break;
            }
        }
        if (progress < 0) return;

        BlockState targetState = level.getBlockState(target);
        if (!RadialUtil.isEffective(targetState, player, target)) return;

        BlockStateModelSet models = mc.getModelManager().getBlockStateModelSet();

        for (BlockPos pos : blocks) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            BlockStateModel model = models.get(state);
            pose.pushPose();
            pose.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            collector.submitBreakingBlockModel(pose, model, state.getSeed(pos), progress);
            pose.popPose();
        }
    }

    private static final class CacheKey {
        private final BlockPos pos;
        private final ItemStack tool;
        private final Direction hitDir;
        private final Direction playerDir;

        private CacheKey(BlockPos pos, ItemStack tool, Direction hitDir, Direction playerDir) {
            this.pos = pos;
            this.tool = tool;
            this.hitDir = hitDir;
            this.playerDir = playerDir;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CacheKey other)) return false;
            return this.pos.equals(other.pos)
                && this.hitDir == other.hitDir
                && this.playerDir == other.playerDir
                && ItemStack.isSameItemSameComponents(this.tool, other.tool);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, hitDir, playerDir, tool.getItem(), tool.getComponentsPatch());
        }
    }

}
