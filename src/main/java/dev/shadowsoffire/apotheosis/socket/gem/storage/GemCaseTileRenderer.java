package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseAnimationState.PositionInfo;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GemCaseTileRenderer implements BlockEntityRenderer<GemCaseTile, GemCaseTileRenderer.State> {

    private static final float PX = 1F / 16F;
    private static final float SCALE = 1F / 6F;

    private final ItemModelResolver itemModelResolver;

    public GemCaseTileRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GemCaseTile blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.entries.clear();

        if (blockEntity.getLevel() == null) return;

        Direction facing = blockEntity.getBlockState().getValue(GemCaseBlock.FACING);
        state.facingAngle = switch (facing) {
            case NORTH -> 0F;
            case EAST -> 270F;
            case SOUTH -> 180F;
            case WEST -> 90F;
            default -> 0F;
        };

        GemCaseAnimationState anim = blockEntity.getAnimationState();
        int gemIndex = 0;

        for (var entry : blockEntity.gems.entrySet()) {
            if (gemIndex >= 16) break;
            DynamicHolder<Gem> holder = entry.getKey();
            if (!holder.isBound()) continue;

            EnumMap<Purity, Integer> purityMap = entry.getValue();
            Purity highestStocked = null;
            for (Purity p : Purity.values()) {
                if (purityMap.get(p) > 0) {
                    if (highestStocked == null || p.ordinal() > highestStocked.ordinal()) {
                        highestStocked = p;
                    }
                }
            }
            if (highestStocked == null) continue;

            ItemStack stack = GemItem.createStack(holder.get(), highestStocked, 1);
            ItemStackRenderState renderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(renderState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode() + gemIndex);

            PositionInfo info = anim.getPosition(gemIndex, partialTicks);
            state.entries.add(new Entry(renderState, info, gemIndex));
            gemIndex++;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for (Entry entry : state.entries) {
            int slot = entry.position.baseSlot();
            float gridX = (slot % 4) + entry.position.offsetX();
            float gridZ = (slot / 4) + entry.position.offsetZ();

            poseStack.pushPose();

            poseStack.translate(0.5F, 0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.facingAngle));
            poseStack.translate(-0.5F, 0F, -0.5F);

            poseStack.translate(0F, 1F, 0F);

            poseStack.scale(SCALE, SCALE, SCALE);

            float tx = (2.5F + gridX * 3.75F) / SCALE * PX;
            float ty = -2F * PX / SCALE + 0.01F * entry.iterIndex;
            float tz = (3.5F + gridZ * 3.25F) / SCALE * PX;
            poseStack.translate(tx, ty, tz);

            poseStack.mulPose(Axis.XP.rotationDegrees(45F));

            entry.renderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static class State extends BlockEntityRenderState {
        public final List<Entry> entries = new ArrayList<>();
        public float facingAngle;
    }

    public record Entry(ItemStackRenderState renderState, PositionInfo position, int iterIndex) {}

}
