package dev.shadowsoffire.apotheosis.client;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.shadowsoffire.apotheosis.affix.effect.RadialAffix;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.RadialBonus;
import dev.shadowsoffire.apotheosis.util.RadialUtil;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialData;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialState;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.HoverEvent.ItemStackInfo;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Hooks to assist in rendering the crumbling block effect on blocks that will be broken by the radial effect (affix or gem bonus).
 * <p>
 * Largely inspired by the implementation from Tinker's Construct (MIT License).
 * https://github.com/SlimeKnights/TinkersConstruct/blob/1.20.1/src/main/java/slimeknights/tconstruct/tools/client/ToolRenderEvents.java
 */
public class RadialProgressTracker {

    /** Maximum number of blocks to render, so perf doesn't tank for huge AOEs */
    private static final int MAX_BLOCKS = 100;

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

        CacheKey key = new CacheKey(pos, player.getMainHandItem(), dir, player.getDirection());

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

    /**
     * Renders the outline on the extra blocks
     *
     * @param e the highlight event
     */
    @SubscribeEvent
    public static void renderBlockHighlights(RenderHighlightEvent.Block e) {
        Set<BlockPos> extraBlocks = getAOEBlocks();
        if (extraBlocks.isEmpty()) {
            return;
        }

        // set up renderer
        LevelRenderer levelRender = e.getLevelRenderer();
        PoseStack matrices = e.getPoseStack();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexBuilder = buffers.getBuffer(RenderType.lines());
        matrices.pushPose();

        // start drawing
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        Entity viewEntity = renderInfo.getEntity();
        Level level = viewEntity.level();
        Vec3 vector3d = renderInfo.getPosition();
        double x = vector3d.x();
        double y = vector3d.y();
        double z = vector3d.z();

        int rendered = 0;
        for (BlockPos pos : extraBlocks) {
            levelRender.renderHitOutline(matrices, vertexBuilder, viewEntity, x, y, z, pos, level.getBlockState(pos));
            if (rendered++ > MAX_BLOCKS) {
                break;
            }
        }

        matrices.popPose();
        buffers.endBatch();
    }

    /**
     * Updates the values stored in {@link LevelRenderer#destructionProgress} for the blocks that are being destroyed by the radial effect.
     * <p>
     * We can run this here, because this event handler fires <i>just</i> before vanilla will render the crumbling effect.
     */
    @SubscribeEvent
    public static void renderStage(RenderLevelStageEvent e) {
        if (e.getStage() != Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }

        Set<BlockPos> extraBlocks = getAOEBlocks();
        if (extraBlocks.isEmpty()) {
            return;
        }

        // validate required variables are set
        MultiPlayerGameMode controller = Minecraft.getInstance().gameMode;
        if (controller == null || !controller.isDestroying()) {
            return;
        }

        // find breaking progress
        BlockPos target = lastKey.pos;
        BlockDestructionProgress progress = null;
        for (Int2ObjectMap.Entry<BlockDestructionProgress> entry : e.getLevelRenderer().destroyingBlocks.int2ObjectEntrySet()) {
            if (entry.getValue().getPos().equals(target)) {
                progress = entry.getValue();
                break;
            }
        }

        if (progress == null) {
            return;
        }

        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        BlockState state = level.getBlockState(target);

        // must not be broken, and the tool definition must be effective
        if (!RadialUtil.isEffective(state, player, target)) {
            return;
        }

        // set up buffers
        PoseStack matrices = e.getPoseStack();
        matrices.pushPose();
        MultiBufferSource.BufferSource vertices = Minecraft.getInstance().renderBuffers().crumblingBufferSource();
        VertexConsumer vertexBuilder = vertices.getBuffer(ModelBakery.DESTROY_TYPES.get(progress.getProgress()));

        // finally, render the blocks
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        double x = renderInfo.getPosition().x;
        double y = renderInfo.getPosition().y;
        double z = renderInfo.getPosition().z;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        int rendered = 0;
        for (BlockPos pos : extraBlocks) {

            matrices.pushPose();
            matrices.translate(pos.getX() - x, pos.getY() - y, pos.getZ() - z);
            PoseStack.Pose entry = matrices.last();
            VertexConsumer blockBuilder = new SheetedDecalTextureGenerator(vertexBuilder, entry, 1);
            ModelData modelData = level.getModelData(pos);
            dispatcher.renderBreakingTexture(level.getBlockState(pos), pos, level, matrices, blockBuilder, modelData);
            matrices.popPose();
            rendered++;

            if (rendered++ > MAX_BLOCKS) {
                break;
            }
        }

        // finish rendering
        matrices.popPose();
        vertices.endBatch();
    }

    private static record CacheKey(BlockPos pos, ItemStackInfo tool, Direction hitDir, Direction playerDir) {

        private CacheKey(BlockPos pos, ItemStack tool, Direction hitDir, Direction playerDir) {
            this(pos, new ItemStackInfo(tool), hitDir, playerDir);
        }
    }

}
