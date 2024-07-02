package dev.shadowsoffire.apotheosis.adventure.affix.augmenting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.client.AdventureContainerScreen;
import dev.shadowsoffire.apotheosis.adventure.client.DropDownList;
import dev.shadowsoffire.apotheosis.adventure.client.SimpleTexButton;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AugmentingScreen extends AdventureContainerScreen<AugmentingMenu> {

    /**
     * Texture file is 256x307
     */
    public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/augmenting.png");

    public static final int ALTERNATIVE_TEXT_WIDTH = 150;
    public static final int ALTERNATIVE_MAX_LINES = 15;

    protected ItemStack lastMainItem = ItemStack.EMPTY;
    protected int lastSelection = DropDownList.NO_SELECTION;

    protected List<AffixInstance> currentItemAffixes = Collections.emptyList();

    protected int alternativePage = DropDownList.NO_SELECTION;
    protected List<List<FormattedText>> alternativePages = Collections.emptyList();
    protected int alternativeXPos = 0;
    protected int alternativeWidth = 0;

    protected AffixDropList list;
    protected SimpleTexButton upgradeBtn, rerollBtn;

    public AugmentingScreen(AugmentingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.getGuiLeft();
        int top = this.getGuiTop();

        int selected = this.getSelectedAffix();
        this.list = this.addRenderableWidget(new AffixDropList(left + 39, top + 17, 123, 14, Component.empty(), this.currentItemAffixes, 6));
        this.list.setSelected(selected);

        Component sigilName = Component.translatable("item.apotheosis.sigil_of_enhancement").withStyle(ChatFormatting.YELLOW);

        this.upgradeBtn = this.addRenderableWidget(
            new FatTexButton(left + 60, top + 111, 29, 13, 186, 135,
                btn -> {
                    if (this.getSelectedAffix() != DropDownList.NO_SELECTION) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, AugmentingMenu.UPGRADE | this.getSelectedAffix() << 1);
                    }
                },
                Component.translatable("button.apotheosis.augmenting.upgrade"),
                Component.translatable("button.apotheosis.augmenting.upgrade.cost", 2, sigilName)));

        this.rerollBtn = this.addRenderableWidget(
            new FatTexButton(left + 112, top + 111, 29, 13, 186 + 37, 135,
                btn -> {
                    if (this.getSelectedAffix() != DropDownList.NO_SELECTION) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, AugmentingMenu.REROLL | this.getSelectedAffix() << 1);
                    }
                },
                Component.translatable("button.apotheosis.augmenting.reroll"),
                Component.translatable("button.apotheosis.augmenting.upgrade.cost", 1, sigilName)));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        this.updateCachedState();

        int left = this.getGuiLeft();
        int top = this.getGuiTop();
        int xCenter = (this.width - this.imageWidth) / 2;
        int yCenter = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURE, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight, 256, 307);

        int selected = this.getSelectedAffix();

        if (selected != DropDownList.NO_SELECTION && !this.list.isOpen()) {
            AffixInstance inst = this.currentItemAffixes.get(selected);
            Component comp = inst.getAugmentingText();
            List<FormattedCharSequence> split = this.font.split(comp, 117);
            for (int i = 0; i < split.size(); i++) {
                gfx.drawString(this.font, split.get(i), left + 43, top + 40 + i * 11, ChatFormatting.YELLOW.getColor(), true);
            }

            int bgColor = 0xF0100010;
            int borderColor = 0xFF36454F;
            TooltipRenderUtil.renderTooltipBackground(gfx, left + 42, top + 39, 117, 6 * 11 - 1, 0, bgColor, bgColor, borderColor, borderColor);
        }
        else {
            int bgColor = 0xAA101010;
            int borderColor = 0xAA36454F;
            TooltipRenderUtil.renderTooltipBackground(gfx, left + 42, top + 39, 117, 6 * 11 - 1, 0, bgColor, bgColor, borderColor, borderColor);
        }

        if (selected != DropDownList.NO_SELECTION && this.rerollBtn.isHovered() && this.rerollBtn.isActive()) {
            if (this.alternativePage != DropDownList.NO_SELECTION) {
                List<FormattedText> page = this.alternativePages.get(this.alternativePage);

                List<ClientTooltipComponent> list = page.stream()
                    .map(Language.getInstance()::getVisualOrder)
                    .map(ClientTooltipComponent::create)
                    .collect(Collectors.toCollection(Lists::newArrayList));

                // We want the rendered tooltip box to have a consistent width when we have pages, regardless of the width of the individual pages.
                // So we replace the empty space on the second-to-last line with empty space of the correct width.
                if (this.alternativePages.size() > 1) {
                    list.set(list.size() - 2, new FakeWidthComponent(this.alternativeWidth));
                }

                gfx.renderTooltipInternal(this.font, list, this.alternativeXPos, this.getGuiTop() + 33, DefaultTooltipPositioner.INSTANCE);
            }
        }

        if (selected != DropDownList.NO_SELECTION && this.upgradeBtn.isActive() && this.upgradeBtn.isHovered()) {
            AffixInstance inst = this.currentItemAffixes.get(selected);
            AffixInstance upgraded = new AffixInstance(inst.affix(), inst.stack(), inst.rarity(), Math.min(1F, inst.level() + 0.25F));

            List<Component> altText = new ArrayList<>();
            altText.add(Component.translatable("text.apotheosis.upgraded_form").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE));
            altText.add(Component.translatable("%s", upgraded.getAugmentingText()).withStyle(ChatFormatting.YELLOW));

            this.drawOnLeft(gfx, altText, top + 33, 150);
        }
    }

    protected void updateCachedState() {
        ItemStack mainItem = this.menu.getMainItem();
        if (!ItemStack.isSameItemSameTags(mainItem, this.lastMainItem)) {
            List<AffixInstance> newAffixes = AugmentingMenu.computeItemAffixes(mainItem);

            if (ItemStack.isSameItem(mainItem, this.lastMainItem) && this.currentItemAffixes.size() == newAffixes.size()) {
                this.list.setEntries(newAffixes);
                this.list.setSelected(this.lastSelection);
            }
            else {
                this.list.setEntries(newAffixes);
            }

            this.currentItemAffixes = newAffixes;
            this.lastMainItem = mainItem.copy();
            this.computeAlternatives(this.list.getSelected());
        }

        if (this.lastSelection != this.list.getSelected()) {
            this.lastSelection = this.list.getSelected();
            this.computeAlternatives(this.lastSelection);
        }

        int selected = this.getSelectedAffix();
        if (selected == DropDownList.NO_SELECTION) {
            Component comp = Component.translatable("button.apotheosis.augmenting.no_selection").withStyle(ChatFormatting.RED);
            this.upgradeBtn.active = false;
            this.upgradeBtn.setInactiveMessage(comp);
            this.rerollBtn.active = false;
            this.rerollBtn.setInactiveMessage(comp);
        }
        else {
            this.upgradeBtn.active = true;
            this.rerollBtn.active = true;

            AffixInstance current = this.currentItemAffixes.get(selected);

            if (current.level() >= 1F) {
                this.upgradeBtn.active = false;
                this.upgradeBtn.setInactiveMessage(Component.translatable("button.apotheosis.augmenting.max_level").withStyle(ChatFormatting.RED));
            }

            if (this.alternativePages.isEmpty()) {
                this.rerollBtn.active = false;
                this.rerollBtn.setInactiveMessage(Component.translatable("button.apotheosis.augmenting.no_alternatives").withStyle(ChatFormatting.RED));
            }

            if (this.upgradeBtn.isActive() && this.menu.getSigils().getCount() < 2 && !this.menu.player.isCreative()) {
                this.upgradeBtn.active = false;
                this.upgradeBtn.setInactiveMessage(Component.translatable("Not Enough Sigils").withStyle(ChatFormatting.RED));
            }

            if (this.rerollBtn.isActive() && this.menu.getSigils().getCount() < 1 && !this.menu.player.isCreative()) {
                this.rerollBtn.active = false;
                this.rerollBtn.setInactiveMessage(Component.translatable("Not Enough Sigils").withStyle(ChatFormatting.RED));
            }
        }
    }

    protected void computeAlternatives(int selected) {
        if (selected == DropDownList.NO_SELECTION) {
            this.alternativePages = Collections.emptyList();
            this.alternativePage = DropDownList.NO_SELECTION;
            return;
        }

        AffixInstance current = this.currentItemAffixes.get(selected);
        List<DynamicHolder<? extends Affix>> alternatives = LootController.getAvailableAffixes(this.lastMainItem, current.rarity().get(),
            this.currentItemAffixes.stream().map(AffixInstance::affix).collect(Collectors.toSet()), current.affix().get().getType());

        if (alternatives.isEmpty()) {
            this.alternativePages = Collections.emptyList();
            this.alternativePage = DropDownList.NO_SELECTION;
        }
        else {
            StringSplitter splitter = this.font.getSplitter();

            {
                int maxWidth = 0;
                Component heading = Component.translatable("text.apotheosis.potential_rerolls").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE);
                List<List<FormattedText>> pages = new ArrayList<>();
                List<FormattedText> page = new ArrayList<>();
                page.add(heading);
                boolean first = true; // For the first line in the first page, we have to skip the initial newline.
                for (DynamicHolder<? extends Affix> afx : alternatives) {
                    Component augTxt = afx.get().getAugmentingText(current.stack(), current.rarity().get(), current.level());
                    List<FormattedText> split = splitter.splitLines(Component.translatable("%s", augTxt).withStyle(ChatFormatting.YELLOW), ALTERNATIVE_TEXT_WIDTH, augTxt.getStyle());
                    maxWidth = Math.max(maxWidth, split.stream().map(this.ths().font::width).max(Integer::compare).get());

                    if (page.size() + split.size() + 1 > ALTERNATIVE_MAX_LINES) {
                        pages.add(page);
                        page = new ArrayList<>();
                        page.add(heading);
                        page.addAll(split);
                    }
                    else {
                        if (!first) {
                            page.add(CommonComponents.SPACE);
                        }
                        page.addAll(split);
                        first = false;
                    }

                    if (afx == alternatives.get(alternatives.size() - 1)) {
                        pages.add(page);
                    }
                }

                this.alternativePage = 0;
                this.alternativePages = pages;
                this.alternativeXPos = this.ths().getGuiLeft() - 16 - maxWidth;
                this.alternativeWidth = maxWidth;
            }

            int pages = this.alternativePages.size();
            if (pages > 1) {

                for (int i = 0; i < pages; i++) {
                    List<FormattedText> page = this.alternativePages.get(i);
                    page.add(CommonComponents.SPACE);
                    page.add(Component.translatable("text.apotheosis.alternative_page", i + 1, pages).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    protected int getSelectedAffix() {
        return this.list == null ? DropDownList.NO_SELECTION : this.list.getSelected();
    }

    public static void handleRerollResult(DynamicHolder<? extends Affix> newAffix) {
        if (Minecraft.getInstance().screen instanceof AugmentingScreen scn) {
            scn.updateCachedState();
            for (int i = 0; i < scn.currentItemAffixes.size(); i++) {
                AffixInstance inst = scn.currentItemAffixes.get(i);
                if (inst.affix().equals(newAffix)) {
                    scn.list.setSelected(i);
                    return;
                }
            }
        }
    }

    public class AffixDropList extends DropDownList<AffixInstance> {

        public AffixDropList(int x, int y, int width, int height, Component narrationMsg, List<AffixInstance> entries, int maxDisplayedEntries) {
            super(x, y, width, height, narrationMsg, entries, maxDisplayedEntries);
        }

        @Override
        protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            gfx.pose().pushPose();
            gfx.pose().translate(0, 0, 100);

            if (this.entries.isEmpty()) {
                gfx.blit(TEXTURE, this.getX(), this.getY(), 0, 267, this.width, this.baseHeight, 256, 307);
            }

            super.renderWidget(gfx, mouseX, mouseY, partialTick);

            int hovered = this.getHoveredSlot(mouseX, mouseY);
            if (this.isOpen && hovered != -1) {
                AffixInstance inst = this.entries.get(hovered);
                List<Component> list = new ArrayList<>();
                list.add(inst.getName(true).copy().withStyle(Style.EMPTY.withColor(0xFFFF80).withUnderlined(true)));
                list.add(Component.translatable("%s", inst.getAugmentingText()).withStyle(ChatFormatting.YELLOW));

                AugmentingScreen.this.drawOnLeft(gfx, list, AugmentingScreen.this.getGuiTop() + 33, 150);
            }

            gfx.blit(TEXTURE, this.getX() + this.width - 15, this.getY(), 123 + (this.isOpen ? 15 : 0), 239, 15, 14, 256, 307);
            gfx.pose().popPose();
        }

        @Override
        protected void renderEntry(GuiGraphics gfx, int x, int y, int mouseX, int mouseY, AffixInstance entry) {
            int hovered = this.getHoveredSlot(mouseX, mouseY);
            int idx = this.entries.indexOf(entry);
            // blit(ResourceLocation pAtlasLocation, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight)
            gfx.blit(TEXTURE, x, y, 0, 239 + (hovered == idx ? this.baseHeight : 0), this.width, this.baseHeight, 256, 307);
            gfx.drawString(AugmentingScreen.this.font, entry.getName(true), x + 2, y + 3, 0xFFFF80);
        }

    }

    /**
     * Variant of {@link SimpleTexButton} which draws an additional 2px border outside the button
     */
    public class FatTexButton extends SimpleTexButton {

        protected final Component costMsg;

        public FatTexButton(int x, int y, int width, int height, int u, int v, OnPress press, Component message, Component costMsg) {
            super(x, y, width, height, u, v, TEXTURE, 256, 307, press, message);
            this.costMsg = costMsg;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
            int yTex = this.yTexStart - 2;
            if (!this.isActive()) {
                yTex += this.height + 4;
            }
            else if (this.isHovered()) {
                yTex += (this.height + 4) * 2;
            }

            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            gfx.blit(this.texture, this.getX() - 2, this.getY() - 2, this.xTexStart - 2, yTex, this.width + 4, this.height + 4, this.textureWidth, this.textureHeight);
            if (this.isHovered()) {
                this.renderToolTip(gfx, pMouseX, pMouseY);
            }
        }

        @Override
        public void renderToolTip(GuiGraphics gfx, int pMouseX, int pMouseY) {
            if (this.getMessage() != CommonComponents.EMPTY && this.isHovered()) {
                Component primary = this.getMessage();
                if (!this.active) {
                    primary = primary.copy().withStyle(ChatFormatting.GRAY);
                }

                List<Component> tooltips = new ArrayList<>();
                tooltips.add(primary);

                if (this.isActive()) {
                    tooltips.add(this.costMsg);
                }
                else if (this.inactiveMessage != CommonComponents.EMPTY) {
                    tooltips.add(this.inactiveMessage);
                }

                gfx.renderComponentTooltip(Minecraft.getInstance().font, tooltips, pMouseX, pMouseY);
            }
        }

        @Override
        public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
            if (this == AugmentingScreen.this.rerollBtn && this.isActive() && this.isHovered()) {
                int change = pDelta < 0 ? 1 : -1;
                int page = AugmentingScreen.this.alternativePage;

                page = Math.floorMod(page + change, AugmentingScreen.this.alternativePages.size());

                AugmentingScreen.this.alternativePage = page;
                return true;
            }
            return super.mouseScrolled(pMouseX, pMouseY, pDelta);
        }

    }

    public static record FakeWidthComponent(int width) implements ClientTooltipComponent {

        @Override
        public int getHeight() {
            return 9; // Font#lineHeight
        }

        @Override
        public int getWidth(Font pFont) {
            return this.width;
        }

    }

}
