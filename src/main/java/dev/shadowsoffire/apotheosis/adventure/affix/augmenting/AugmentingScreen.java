package dev.shadowsoffire.apotheosis.adventure.affix.augmenting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apoth.Affixes;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.client.AdventureContainerScreen;
import dev.shadowsoffire.apotheosis.adventure.client.DropDownList;
import dev.shadowsoffire.apotheosis.adventure.client.SimpleTexButton;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AugmentingScreen extends AdventureContainerScreen<AugmentingMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/augmenting.png");

    protected ItemStack lastMainItem = ItemStack.EMPTY;
    protected List<AffixInstance> currentItemAffixes = new ArrayList<>();
    protected AffixDropList list;
    protected SimpleTexButton upgradeBtn, rerollBtn;

    public AugmentingScreen(AugmentingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.getGuiLeft();
        int top = this.getGuiTop();

        this.list = this.addRenderableWidget(new AffixDropList(left + 31, top + 10, 122, 13, Component.empty(), this.currentItemAffixes, 7));

        this.upgradeBtn = this.addRenderableWidget(
            new SimpleTexButton(left + 35, top + 88, 20, 20, 216, 196, TEXTURE, 256, 256,
                btn -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0),
                Component.translatable("button.apotheosis.augmenting.upgrade")));

        this.rerollBtn = this.addRenderableWidget(
            new SimpleTexButton(left + 145, top + 88, 20, 20, 236, 196, TEXTURE, 256, 256,
                btn -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1),
                Component.translatable("button.apotheosis.augmenting.reroll")));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int left = this.getGuiLeft();
        int top = this.getGuiTop();
        int xCenter = (this.width - this.imageWidth) / 2;
        int yCenter = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURE, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int selected = this.list.getSelected();

        if (selected != -1 && !this.list.isOpen()) {
            AffixInstance inst = this.currentItemAffixes.get(selected);
            Component comp = inst.getAugmentingText();
            List<FormattedCharSequence> split = font.split(comp, 124);
            for (int i = 0; i < split.size(); i++) {
                gfx.drawString(font, split.get(i), left + 38, top + 40 + i * 11, 0xFFFFFF, true);
            }

            final int BACKGROUND_COLOR = -267386864;
            final int BORDER_COLOR_TOP = 1347420415;
            final int BORDER_COLOR_BOTTOM = 1344798847;
            TooltipRenderUtil.renderTooltipBackground(gfx, left + 38, top + 40, 124, split.size() * (1 + font.lineHeight), 0, BACKGROUND_COLOR, BACKGROUND_COLOR, BORDER_COLOR_TOP, BORDER_COLOR_BOTTOM);
        }

        this.rerollBtn.visible = !this.list.isOpen();
        this.upgradeBtn.visible = !this.list.isOpen();

        if (selected != -1 && this.rerollBtn.isHovered()) {
            AffixInstance inst = this.currentItemAffixes.get(selected);
            List<DynamicHolder<? extends Affix>> alternatives = LootController.getAvailableAffixes(this.lastMainItem, inst.rarity().get(), this.currentItemAffixes.stream().map(AffixInstance::affix).collect(Collectors.toSet()),
                inst.affix().get().getType());

            List<Component> altText = new ArrayList<>();

            if (!alternatives.isEmpty()) {
                altText.add(Component.literal("Potential Rerolls").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                for (var afx : alternatives) {
                    altText.add(afx.get().getAugmentingText(inst.stack(), inst.rarity().get(), inst.level()));
                    altText.add(CommonComponents.SPACE);
                }
                altText.remove(altText.size() - 1);
            }
            else {
                altText.add(Component.literal("No Alternatives Available").withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE));
            }

            this.drawOnLeft(gfx, altText, top + 40, 150);
        }

        if (selected != -1 && this.upgradeBtn.isHovered()) {
            AffixInstance inst = this.currentItemAffixes.get(selected);
            AffixInstance upgraded = new AffixInstance(inst.affix(), inst.stack(), inst.rarity(), Math.min(1F, inst.level() + 0.25F));

            List<Component> altText = new ArrayList<>();

            altText.add(Component.literal("Upgraded Form").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));

            altText.add(upgraded.getAugmentingText());

            this.drawOnLeft(gfx, altText, top + 40, 150);
        }
    }

    @Override
    protected void containerTick() {
        ItemStack mainItem = this.menu.getSlot(0).getItem();
        if (!ItemStack.isSameItemSameTags(mainItem, this.lastMainItem)) {
            this.currentItemAffixes = computeItemAffixes(mainItem);
            this.lastMainItem = mainItem.copy();
            this.list.setEntries(this.currentItemAffixes);
        }

        // TODO: Update button active states based on selection and available sigils
    }

    protected List<AffixInstance> computeItemAffixes(ItemStack stack) {
        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        if (affixes.isEmpty()) {
            return Collections.emptyList();
        }

        return affixes.values().stream().sorted(Comparator.comparing(inst -> inst.affix().getId())).filter(a -> !a.affix().equals(Affixes.DURABLE)).toList();
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
                gfx.blit(TEXTURE, this.getX(), this.getY(), 0, 243, this.width + 16, this.baseHeight, 256, 256);
            }

            super.renderWidget(gfx, mouseX, mouseY, partialTick);

            int hovered = this.getHoveredSlot(mouseX, mouseY);
            if (this.isOpen && hovered != -1) {
                AffixInstance inst = this.entries.get(hovered);
                List<Component> list = new ArrayList<>();
                list.add(inst.getName(true).copy().withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                list.add(inst.getAugmentingText());

                AugmentingScreen.this.drawOnLeft(gfx, list, AugmentingScreen.this.getGuiTop() + 30, 150);
            }

            gfx.blit(TEXTURE, this.getX() + this.width, this.getY(), 138 + (this.isOpen ? 15 : 0), 217, 15, 11, 256, 256);
            gfx.pose().popPose();
        }

        @Override
        protected void renderEntry(GuiGraphics gfx, int x, int y, int mouseX, int mouseY, AffixInstance entry) {
            int hovered = this.getHoveredSlot(mouseX, mouseY);
            int idx = this.entries.indexOf(entry);
            // blit(ResourceLocation pAtlasLocation, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight)
            gfx.blit(TEXTURE, x, y, 0, 217 + (hovered == idx ? this.baseHeight : 0), this.width, this.baseHeight, 256, 256);
            gfx.drawString(AugmentingScreen.this.font, entry.getName(true), x + 2, y + 3, 0xFFFFFF);
        }

    }

}
