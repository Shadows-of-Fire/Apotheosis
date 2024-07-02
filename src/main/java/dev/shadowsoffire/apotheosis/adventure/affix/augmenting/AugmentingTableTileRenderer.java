package dev.shadowsoffire.apotheosis.adventure.affix.augmenting;

import org.joml.Quaternionf;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingTableTile.AnimationStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class AugmentingTableTileRenderer implements BlockEntityRenderer<AugmentingTableTile> {

    public static final ResourceLocation STAR_CUBE = new ResourceLocation(Apotheosis.MODID, "item/star_cube");

    @Override
    @SuppressWarnings("deprecation")
    public void render(AugmentingTableTile tile, float partials, PoseStack matrix, MultiBufferSource pBufferSource, int light, int overlay) {
        if (tile.stage == AnimationStage.HIDING) {
            return; // no-op if the cube is hidden
        }

        Minecraft.getInstance().textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        ItemRenderer irenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel base = irenderer.getItemModelShaper().getModelManager().getModel(STAR_CUBE);

        double px = 1 / 16D;
        long time = tile.time;

        matrix.pushPose();

        matrix.translate(5 * px, 5 * px, 5 * px);

        switch (tile.stage) {
            case HIDING -> {

            }
            case RISING -> {
                float progress = (time + partials) / AugmentingTableTile.RISE_TIME;
                double rise = Mth.lerp(progress, 0.1 * px, 11 * px);
                matrix.translate(0, rise, 0);
            }
            case FALLING -> {
                float progress = (AugmentingTableTile.RISE_TIME - time + partials) / AugmentingTableTile.RISE_TIME;
                double rise = Mth.lerp(progress, 11 * px, 0.1 * px);
                matrix.translate(0, rise, 0);
            }
            case SPINNING -> {
                float rotation = (time % 360 + partials) * Mth.PI / 180F;

                matrix.translate(0, 11 * px, 0);

                matrix.translate(3 * px, 3 * px, 3 * px);
                matrix.mulPose(new Quaternionf().rotationXYZ(rotation, 0, rotation));
                matrix.translate(-3 * px, -3 * px, -3 * px);
            }
        }

        irenderer.renderModelLists(base, ItemStack.EMPTY, light, overlay, matrix, ItemRenderer.getFoilBufferDirect(pBufferSource, Sheets.translucentItemSheet(), true, false));

        matrix.popPose();
    }

}
