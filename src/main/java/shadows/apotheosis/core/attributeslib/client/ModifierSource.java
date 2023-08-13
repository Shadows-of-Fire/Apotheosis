package shadows.apotheosis.core.attributeslib.client;

import java.util.Comparator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.util.Comparators;

/**
 * A Modifier Source is a container object around any potential Attribute Modifier Source.<br>
 * It has the code necessary to render and compare the object for display in the Attributes screen.
 */
public abstract class ModifierSource<T> implements Comparable<ModifierSource<T>> {

    protected final ModifierSourceType<T> type;
    protected final Comparator<T> comparator;
    protected final T data;

    public ModifierSource(ModifierSourceType<T> type, Comparator<T> comparator, T data) {
        this.type = type;
        this.comparator = comparator;
        this.data = data;
    }

    /**
     * Render this ModifierSource as whatever visual representation it may take.
     *
     * @param font
     * @param x
     * @param y
     * @param stack
     * @param itemRenderer
     * @param pBlitOffset
     */
    public abstract void render(Font font, int x, int y, PoseStack stack, ItemRenderer itemRenderer, int pBlitOffset);

    public ModifierSourceType<T> getType() {
        return this.type;
    }

    public final T getData() {
        return this.data;
    }

    @Override
    public int compareTo(ModifierSource<T> o) {
        return this.comparator.compare(this.getData(), o.getData());
    }

    public static class ItemModifierSource extends ModifierSource<ItemStack> {

        @SuppressWarnings("deprecation")
        public ItemModifierSource(ItemStack data) {
            super(ModifierSourceType.EQUIPMENT, Comparator.comparing(LivingEntity::getEquipmentSlotForItem).reversed().thenComparing(Comparator.comparing(ItemStack::getItem, Comparators.idComparator(Registry.ITEM))), data);
        }

        @Override
        public void render(Font font, int x, int y, PoseStack stack, ItemRenderer itemRenderer, int pBlitOffset) {
            PoseStack mvStack = RenderSystem.getModelViewStack();
            mvStack.pushPose();
            float scale = 0.5F;
            mvStack.scale(scale, scale, 1);
            mvStack.translate(1 + x / scale, 1 + y / scale, 0);
            itemRenderer.renderAndDecorateFakeItem(this.data, 0, 0);
            mvStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

    }

    public static class EffectModifierSource extends ModifierSource<MobEffectInstance> {

        @SuppressWarnings("deprecation")
        public EffectModifierSource(MobEffectInstance data) {
            super(ModifierSourceType.MOB_EFFECT, Comparator.comparing(MobEffectInstance::getEffect, Comparators.idComparator(Registry.MOB_EFFECT)), data);
        }

        @Override
        public void render(Font font, int x, int y, PoseStack stack, ItemRenderer itemRenderer, int pBlitOffset) {
            MobEffectTextureManager texMgr = Minecraft.getInstance().getMobEffectTextures();
            // We don't have an EffectRenderingInventoryScreen, so we'll just hope the texture is good enough.
            // var renderer = net.minecraftforge.client.extensions.common.IClientMobEffectExtensions.of(inst);
            // if (renderer.renderInventoryIcon(inst, this, pPoseStack, pRenderX + (p_194013_ ? 6 : 7), i, this.getBlitOffset())) {
            // i += pYOffset;
            // continue;
            // }
            MobEffect effect = this.data.getEffect();
            TextureAtlasSprite sprite = texMgr.get(effect);
            RenderSystem.setShaderTexture(0, sprite.atlas().location());
            float scale = 0.5F;
            stack.pushPose();
            stack.scale(scale, scale, 1);
            stack.translate(x / scale, y / scale, pBlitOffset);
            GuiComponent.blit(stack, 0, 0, 0, 18, 18, sprite);
            stack.popPose();
        }

    }
}
