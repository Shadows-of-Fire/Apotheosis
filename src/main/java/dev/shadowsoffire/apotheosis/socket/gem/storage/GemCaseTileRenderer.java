package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GemCaseTileRenderer implements BlockEntityRenderer<GemCaseTile> {

    private Map<DynamicHolder<Gem>, ItemStack> gemCache = new HashMap<>();

    @Override
    public void render(GemCaseTile tile, float partials, PoseStack pose, MultiBufferSource bufferSrc, int light, int overlay) {
        ItemRenderer irenderer = Minecraft.getInstance().getItemRenderer();

        double px = 1 / 16D;

        Direction facing = tile.getBlockState().getValue(GemCaseBlock.FACING);

        float angle = switch (facing) {
            case NORTH -> 0;
            case EAST -> 270;
            case SOUTH -> 180;
            case WEST -> 90;
            default -> 0;
        };

        GemCaseAnimationState animState = tile.getAnimationState();

        int i = 0;

        for (DynamicHolder<Gem> gem : tile.gems.keySet()) {
            int count = 0;
            for (Purity p : Purity.ALL_PURITIES) {
                count += tile.getCount(gem, p);
            }

            if (count == 0) continue;

            ItemStack stack = this.gemCache.computeIfAbsent(gem, g -> {
                return gem.get().toStack(Purity.FLAWLESS);
            });

            pose.pushPose();

            pose.translate(8 * px, 0 * px, 8 * px);
            pose.mulPose(Axis.YP.rotationDegrees(angle));
            pose.translate(-8 * px, 0 * px, -8 * px);

            pose.translate(0, 16 * px, 0);

            float scale = 1 / 6F;

            pose.scale(scale, scale, scale);

            // Get the animated position for this gem
            GemCaseAnimationState.PositionInfo posInfo = animState.getPosition(i, partials);

            // Calculate the actual slot position including animation offset
            float gridX = (posInfo.baseSlot() % 4) + posInfo.offsetX();
            float gridZ = (posInfo.baseSlot() / 4) + posInfo.offsetZ();

            // Position the gems in a 4x4 grid within the case, which is itself a 1x1 block using 14px of internal space.
            float offsetX = (2.5F + gridX * 3.75F) / scale;
            float offsetZ = (3.5F + gridZ * 3.25F) / scale;
            pose.translate(offsetX * px, -2 * px / scale + 0.01 * i, offsetZ * px);

            pose.mulPose(Axis.XP.rotationDegrees(45));

            irenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, bufferSrc, Minecraft.getInstance().level, 0);

            pose.popPose();

            if (++i >= 16) break;
        }

    }

}
