package dev.shadowsoffire.apotheosis.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.placebo.patreon.PatreonUtils.WingType;
import dev.shadowsoffire.placebo.patreon.wings.IWingModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Renders one of Placebo's patreon wings on any living entity that holds the {@link Attachments#WINGS} attachment.
 * <p>
 * The wings themselves are drawn by Placebo's {@link IWingModel}. This layer only picks the wing type and positions the pose stack.
 */
public class BossWingLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final WingType[] TYPES = WingType.values();

    public BossWingLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource buf, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        Integer selector = entity.getExistingDataOrNull(Attachments.WINGS);
        if (selector == null) {
            return;
        }

        WingType type = TYPES[Math.floorMod(selector, TYPES.length)];

        stack.pushPose();
        this.moveToShoulders(stack, entity);
        type.model.get().render(stack, buf, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0), entity, partialTicks, type);
        stack.popPose();
    }

    /**
     * Moves the pose stack to the point the wings hang from, which {@link IWingModel#render} expects to be the top-center of the torso.
     * <p>
     * Humanoid models expose that point directly. For anything else it is estimated from the hitbox, which puts the wings in the
     * right region for upright mobs but has not been tuned for any particular model.
     */
    private void moveToShoulders(PoseStack stack, T entity) {
        if (this.getParentModel() instanceof HumanoidModel<?> humanoid) {
            if (humanoid.young) {
                // Matches the transform that AgeableListModel#renderToBuffer applies to the body parts of baby humanoids.
                stack.scale(0.5F, 0.5F, 0.5F);
                stack.translate(0, 1.5F, 0);
            }
            humanoid.body.translateAndRotate(stack);
        }
        else {
            // The hitbox is in world units, but the pose has already been scaled by the renderer (scale attribute, slime size, etc).
            Matrix4f pose = stack.last().pose();
            float poseScale = Mth.sqrt(pose.m10() * pose.m10() + pose.m11() * pose.m11() + pose.m12() * pose.m12());
            float height = entity.getBbHeight() / poseScale;

            // The model origin sits 1.501 blocks above the feet, and a player's shoulders are at 5/6ths of their 1.8 block height.
            float size = height / 1.8F;
            stack.translate(0, 1.501F - height * 5 / 6F, 0);
            stack.scale(size, size, size);
        }
    }

}
