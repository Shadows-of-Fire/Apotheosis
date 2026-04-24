package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.client.AdventureContainerScreen;
import dev.shadowsoffire.apotheosis.client.PipelinedRenderer;
import dev.shadowsoffire.apotheosis.client.SimpleTexButton;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.payloads.ButtonClickPayload;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.DrawsOnLeft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class GemCaseScreen extends AdventureContainerScreen<GemCaseMenu> implements DrawsOnLeft {

    public static final Identifier TEXTURES = Apotheosis.loc("textures/gui/gem_case.png");
    public static final int MAX_ROWS = 3;
    public static final int SLOTS_PER_ROW = 6;

    protected float scrollOffs;
    protected boolean scrolling;
    protected int startIndex;

    /**
     * List of gems matching the current name and item filter(s) that are to be displayed.
     */
    protected List<SafeSlot> data = new ArrayList<>();
    protected List<SimpleTexButton> upgradeButtons = new ArrayList<>();

    @Nullable
    protected EditBox filter = null;

    public GemCaseScreen(GemCaseMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        this.imageHeight = 230;
        this.containerChanged();
        container.setNotifier(this::containerChanged);
    }

    @Override
    protected void init() {
        super.init();
        this.filter = this.addRenderableWidget(new EditBox(this.font, this.getLeftPos() + 16, this.getTopPos() + 16, 110, 11, this.filter, Component.literal("")));
        this.filter.setBordered(false);
        this.filter.setTextColor(0xFF97714F);
        this.filter.setResponder(t -> this.containerChanged());
        this.setFocused(this.filter);
        for (int i = 0; i < MAX_ROWS * SLOTS_PER_ROW; i++) {
            var btn = new GemCaseSelectButton(this, i, this.getLeftPos() + 21 + i % SLOTS_PER_ROW * 18, this.getTopPos() + 31 + i / SLOTS_PER_ROW * 19);
            this.addRenderableWidget(btn);
        }

        this.upgradeButtons.clear();
        Purity[] purities = Purity.values();
        for (int i = 1; i < purities.length; i++) {
            Purity prev = purities[i - 1];
            Purity purity = purities[i];
            var btn = SimpleTexButton.builder()
                .size(16, 16)
                .texture(TEXTURES)
                .texSize(307, 256)
                .texPos(291, 29)
                .pos(this.getLeftPos() + 30 + (i - 1) * 18, this.getTopPos() + 109)
                .message(Apotheosis.lang("button", "gem_case.upgrade", prev.toComponent(), purity.toComponent()))
                .inactiveMessage(Apotheosis.lang("button", "gem_case.upgrade_no_materials"))
                .action(this.tryUpgrade(purity))
                .build();
            this.upgradeButtons.add(btn);
            this.addRenderableWidget(btn);
        }

        this.containerChanged();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key mouseKey = InputConstants.getKey(event);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.getFocused() == this.filter) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void extractTooltip(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        super.extractTooltip(gfx, mouseX, mouseY);
        if (this.getSelectedGem() != null) {
            for (Purity p : Purity.ALL_PURITIES) {
                if (!p.isAtLeast(this.getSelectedGem().getMinPurity())) {
                    continue;
                }
                int count = this.menu.getGemCount(this.getSelectedGem(), p);
                if (count == 0) {
                    int slotIndex = p.ordinal();
                    int x = this.getLeftPos() + 21 + slotIndex * 18;
                    int y = this.getTopPos() + 91;
                    if (this.isHovering(x - this.getLeftPos(), y - this.getTopPos(), 16, 16, mouseX, mouseY)
                        && this.menu.getCarried().isEmpty()) {
                        ItemStack stack = this.getSelectedGem().toStack(p);
                        List<Component> tooltip = new ArrayList<>();
                        tooltip.add(stack.getHoverName());
                        tooltip.add(Apotheosis.lang("tooltip", "gem_case.none_owned").withStyle(ChatFormatting.RED));
                        tooltip.add(CommonComponents.SPACE);
                        stack.getItem().appendHoverText(stack, TooltipContext.of(Minecraft.getInstance().level),
                            stack.getOrDefault(net.minecraft.core.component.DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT), tooltip::add, TooltipFlag.NORMAL);
                        gfx.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partial) {
        super.extractBackground(gfx, mouseX, mouseY, partial);
        int left = this.leftPos;
        int top = this.topPos;
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight, 307, 256);
        int scrollbarPos = (int) (90F * this.scrollOffs);
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, left + 13, top + 29 + scrollbarPos, 303, this.isScrollBarActive() ? 0 : 12, 4, 12, 307, 256);
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, left - 65, top + 16, 198, 0, 65, 193, 307, 256);

        if (this.getSelectedGem() != null) {
            for (Purity p : Purity.ALL_PURITIES) {
                if (!p.isAtLeast(this.getSelectedGem().getMinPurity())) {
                    continue;
                }
                int count = this.menu.getGemCount(this.getSelectedGem(), p);
                if (count == 0) {
                    ItemStack stack = this.getSelectedGem().toStack(p);
                    int slotIndex = p.ordinal();
                    int x = this.getLeftPos() + 21 + slotIndex * 18;
                    int y = this.getTopPos() + 91;
                    PipelinedRenderer.ghostFakeItem(gfx, stack, x, y);
                }
            }
        }
    }

    @Override
    public void renderSlotContents(GuiGraphicsExtractor gfx, ItemStack stack, Slot slot, @Nullable String stackCount) {
        if (slot instanceof GemCaseSlot gss) {
            gfx.fakeItem(stack, slot.x, slot.y);

            int count = this.menu.getGemCount(this.menu.selectedGem, gss.purity);
            if (count > 1) {
                String countStr = GemCaseBlock.format(count);
                float scale = 1.0f;
                if (countStr.length() > 2) {
                    scale = 2.0f / countStr.length();
                }
                gfx.pose().pushMatrix();
                gfx.pose().scale(scale, scale);
                float textX = (slot.x + 16 - (this.font.width(countStr) - 1) * scale) / scale;
                float textY = (slot.y + 16 - (this.font.lineHeight - 2) * scale) / scale;
                gfx.text(this.font, countStr, (int) textX, (int) textY, 0xFFFFFFFF, true);
                gfx.pose().popMatrix();
            }
        }
        else {
            super.renderSlotContents(gfx, stack, slot, stackCount);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.scrolling = false;
        if (this.isHovering(14, 29, 4, 103, event.x(), event.y())) {
            this.scrolling = true;
            this.mouseDragged(event, 0, 0);
            return true;
        }

        if (this.filter.isHovered() && event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.filter.setValue("");
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int barTop = this.topPos + 14;
            int barBot = barTop + 103;
            this.scrollOffs = ((float) event.y() - barTop - 6F) / (barBot - barTop - 12F) - 0.12F;
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5D);
            return true;
        }
        else {
            return super.mouseDragged(event, pDragX, pDragY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (this.isScrollBarActive()) {
            int i = this.getOffscreenRows();
            this.scrollOffs = (float) (this.scrollOffs - pScrollY / i);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * i + 0.5D);
        }
        return true;
    }

    @Nullable
    public Gem getSelectedGem() {
        return this.menu.selectedGem;
    }

    private boolean isScrollBarActive() {
        return this.data.size() > MAX_ROWS * SLOTS_PER_ROW;
    }

    protected int getOffscreenRows() {
        return Math.ceilDiv(this.data.size() - MAX_ROWS * SLOTS_PER_ROW, SLOTS_PER_ROW);
    }

    private void containerChanged() {
        this.data.clear();
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            var slot = new SafeSlot(gem, this.menu.getGemCount(gem), new ItemStack(Apoth.Items.GEM));
            GemItem.setGem(slot.displayStack, gem);
            GemItem.setPurity(slot.displayStack, Purity.NORMAL);
            this.data.add(slot);
        }

        this.data = this.filter(this.data);

        if (!this.isScrollBarActive()) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }
        Collections.sort(this.data, Comparator.<SafeSlot, Boolean>comparing(slot -> slot.count <= 0).thenComparing(Comparator.comparing(slot -> slot.gem.getId().toString())));

        for (int i = 0; i < this.upgradeButtons.size(); i++) {
            Purity prev = Purity.values()[i];
            Purity purity = prev.next();
            GemUpgradeMatch match = this.menu.getUpgradeMatch(purity);
            SimpleTexButton button = this.upgradeButtons.get(i);
            if (match != null) {
                button.active = true;
                ItemStack leftMat = this.menu.upgradeMatInv.getItem(match.leftSlot());
                ItemStack rightMat = this.menu.upgradeMatInv.getItem(match.rightSlot());
                int leftCount = match.leftIng().count(), rightCount = match.rightIng().count();

                button.setTooltipProvider((btn, tooltip) -> {
                    tooltip.accept(Apotheosis.lang("button", "gem_case.upgrade_cost", leftCount, leftMat.getHoverName(), rightCount, rightMat.getHoverName()));
                    if (Minecraft.getInstance().hasShiftDown()) {
                        tooltip.accept(Apotheosis.lang("button", "gem_case.upgrade_all").withStyle(ChatFormatting.YELLOW));
                    }
                });
            }
            else {
                button.active = false;
                if (this.menu.getGemCount(this.getSelectedGem(), prev) < 2) {
                    button.setInactiveMessage(Apotheosis.lang("button", "gem_case.upgrade_no_gems").withStyle(ChatFormatting.RED));
                }
                else {
                    button.setInactiveMessage(Apotheosis.lang("button", "gem_case.upgrade_no_materials").withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    private List<SafeSlot> filter(List<SafeSlot> list) {
        Iterator<SafeSlot> iter = list.iterator();
        while (iter.hasNext()) {
            SafeSlot slot = iter.next();
            if (!this.isAllowedByItem(slot) || !this.isAllowedBySearch(slot)) {
                iter.remove();
            }
        }
        return list;
    }

    private boolean isAllowedByItem(SafeSlot slot) {
        ItemStack stack = this.menu.ioInv.getItem(1);
        return stack.isEmpty() || slot.gem.getBonus(LootCategory.forItem(stack)) != null;
    }

    // TODO: This is currently only searching by name. It needs to also search by item tooltip lines (or at least category).
    private boolean isAllowedBySearch(SafeSlot slot) {
        String name = slot.displayStack.getDisplayName().getString().toLowerCase(Locale.ROOT);
        String search = this.filter == null ? "" : this.filter.getValue().trim().toLowerCase(Locale.ROOT);
        return Strings.isNullOrEmpty(search) || ChatFormatting.stripFormatting(name).contains(search);
    }

    private OnPress tryUpgrade(Purity purity) {
        return btn -> {
            boolean shift = Minecraft.getInstance().hasShiftDown();
            int value = purity.ordinal() | (shift ? 0x1000 : 0x0000);
            ClientPacketDistributor.sendToServer(new ButtonClickPayload(value));
        };
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gfx, int pMouseX, int pMouseY) {}

    public static void handleSelectedGem(DynamicHolder<Gem> gem) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof GemCaseScreen screen) {
            screen.menu.setSelectedGem(gem);
        }
    }

    static record SafeSlot(Gem gem, int count, ItemStack displayStack) {}
}
