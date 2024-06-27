package dev.shadowsoffire.apotheosis.adventure.affix.salvaging;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingRecipe.OutputData;
import dev.shadowsoffire.apotheosis.adventure.client.AdventureContainerScreen;
import dev.shadowsoffire.apotheosis.adventure.client.GrayBufferSource;
import dev.shadowsoffire.apotheosis.adventure.client.SimpleTexButton;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SalvagingScreen extends AdventureContainerScreen<SalvagingMenu> {

    public static final Component TITLE = Component.translatable("container.apotheosis.salvage");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/salvage.png");

    protected List<OutputData> results = new ArrayList<>();
    protected SimpleTexButton salvageBtn;

    public SalvagingScreen(SalvagingMenu menu, Inventory inv, Component title) {
        super(menu, inv, TITLE);
        this.menu.addSlotListener((id, stack) -> this.computeResults());
        this.imageHeight = 174;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.getGuiLeft();
        int top = this.getGuiTop();

        this.salvageBtn = this.addRenderableWidget(
            new SimpleTexButton(left + 98, top + 34, 18, 18, 238, 0, TEXTURE, 256, 256,
                btn -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0),
                Component.translatable("button.apotheosis.salvage"))
                .setInactiveMessage(Component.translatable("button.apotheosis.no_salvage").withStyle(ChatFormatting.RED)));

        this.computeResults();
    }

    public void computeResults() {
        if (this.salvageBtn == null) return;

        var matches = new ArrayList<OutputData>();

        for (int i = 0; i < 15; i++) {
            Slot s = this.menu.getSlot(i);
            ItemStack stack = s.getItem();
            var recipe = SalvagingMenu.findMatch(Minecraft.getInstance().level, stack);
            if (recipe != null) {
                for (OutputData d : recipe.getOutputs()) {
                    int[] counts = SalvagingMenu.getSalvageCounts(d, stack);
                    matches.add(new OutputData(d.stack, counts[0], counts[1]));
                }
            }
        }

        List<OutputData> compressed = new ArrayList<>();

        for (OutputData data : matches) {
            if (data == null) continue;
            boolean success = false;
            for (OutputData existing : compressed) {
                if (ItemStack.isSameItemSameTags(data.stack, existing.stack)) {
                    existing.min += data.min;
                    existing.max += data.max;
                    success = true;
                    break;
                }
            }
            if (!success) compressed.add(data);
        }

        this.results = compressed;
        this.salvageBtn.active = !this.results.isEmpty();
    }

    @Override
    public void render(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(gfx);
        super.render(gfx, pMouseX, pMouseY, pPartialTick);

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();

        int maxDisplay = Math.min(6, this.results.size());

        IntSet skipSlots = new IntOpenHashSet();
        for (int i = 0; i < maxDisplay; i++) {
            ItemStack display = this.results.get(i).stack;
            // Search for an empty slot to draw the ghost item on.
            // Skip drawing the item if it already exists in the output inventory.
            int displaySlot = -1;
            for (int slot = 12; slot < 18; slot++) {
                if (skipSlots.contains(slot)) continue;
                ItemStack outStack = this.menu.slots.get(slot).getItem();
                if (outStack.isEmpty()) {
                    displaySlot = slot;
                    skipSlots.add(slot);
                    break;
                }
                else if (outStack.is(display.getItem())) {
                    break;
                }
            }
            if (displaySlot == -1) continue;
            Slot slot = this.menu.getSlot(displaySlot);
            renderGuiItem(gfx, display, this.getGuiLeft() + slot.x, this.getGuiTop() + slot.y, GrayBufferSource::new);
        }

        this.renderTooltip(gfx, pMouseX, pMouseY);
    }

    public static void renderGuiItem(GuiGraphics gfx, ItemStack pStack, int pX, int pY, Function<MultiBufferSource, MultiBufferSource> wrapper) {
        Minecraft.getInstance().textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack posestack = gfx.pose();
        posestack.pushPose();
        posestack.translate(pX, pY, 100.0F);
        posestack.translate(8.0D, 8.0D, 0.0D);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(16.0F, 16.0F, 16.0F);
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getItemRenderer().getModel(pStack, mc.level, mc.player, pX ^ pY);
        boolean flag = !model.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Minecraft.getInstance().getItemRenderer().render(pStack, ItemDisplayContext.GUI, false, posestack, wrapper.apply(buffer), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        posestack.popPose();
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float pPartialTick, int pX, int pY) {
        gfx.blit(TEXTURE, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderTooltip(GuiGraphics gfx, int x, int y) {
        PoseStack stack = gfx.pose();
        stack.pushPose();
        stack.translate(0, 0, -100);
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("text.apotheosis.salvage_results").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));

        for (OutputData data : this.results) {
            tooltip.add(Component.translatable("%s-%s %s", data.min, data.max, data.stack.getHoverName()));
        }

        if (tooltip.size() > 1) this.drawOnLeft(gfx, tooltip, this.getGuiTop() + 29);
        stack.popPose();

        super.renderTooltip(gfx, x, y);
    }

}
