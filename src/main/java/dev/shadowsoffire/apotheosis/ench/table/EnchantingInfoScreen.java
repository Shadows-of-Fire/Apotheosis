package dev.shadowsoffire.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.EnchModule;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu.Arcana;
import dev.shadowsoffire.apotheosis.ench.table.RealEnchantmentHelper.ArcanaEnchantmentData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry.IntrusiveBase;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantingInfoScreen extends Screen {

    public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/enchanting_info.png");

    protected static ChatFormatting[] colors = { ChatFormatting.WHITE, ChatFormatting.YELLOW, ChatFormatting.BLUE, ChatFormatting.GOLD };

    protected final ApothEnchantScreen parent;
    protected final int imageWidth, imageHeight;
    protected final ItemStack toEnchant;
    protected final int[] costs;
    protected final int[] clues;
    protected final int[][] powers = new int[3][];
    protected final boolean treasure;

    protected int selectedSlot = -1;
    protected int leftPos, topPos;
    protected PowerSlider slider;
    protected int currentPower;
    protected float scrollOffs;
    protected boolean scrolling;
    protected int startIndex;
    List<EnchantmentDataWrapper> enchantments = Collections.emptyList();
    Map<Enchantment, List<Enchantment>> exclusions = new HashMap<>();

    public EnchantingInfoScreen(ApothEnchantScreen parent) {
        super(Component.translatable("info.apotheosis.enchinfo_title"));
        this.parent = parent;
        this.imageWidth = 240;
        this.imageHeight = 170;
        this.toEnchant = parent.getMenu().getSlot(0).getItem();
        this.costs = parent.getMenu().costs;
        this.clues = parent.getMenu().enchantClue;
        this.treasure = parent.getMenu().stats.treasure();
        for (int i = 0; i < 3; i++) {
            Enchantment clue = Enchantment.byId(this.clues[i]);
            if (clue != null) {
                int level = this.costs[i];
                float quanta = parent.getMenu().stats.quanta() / 100F;
                float rectification = parent.getMenu().stats.rectification() / 100F;
                int minPow = Math.round(Mth.clamp(level - level * (quanta - quanta * rectification), 1, EnchantingStatRegistry.getAbsoluteMaxEterna() * 4));
                int maxPow = Math.round(Mth.clamp(level + level * quanta, 1, EnchantingStatRegistry.getAbsoluteMaxEterna() * 4));
                this.powers[i] = new int[] { minPow, maxPow };
                this.selectedSlot = i;
            }
        }
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.slider = this.addRenderableWidget(new PowerSlider(this.leftPos + 5, this.topPos + 80, 80, 20));
    }

    @Override
    public void render(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(gfx);

        PoseStack pose = gfx.pose();
        pose.pushPose();
        pose.translate(this.leftPos, this.topPos, 0);
        gfx.blit(TEXTURES, 0, 0, 0, 0, this.imageWidth, this.imageHeight);
        for (int i = 0; i < 3; i++) {
            Enchantment clue = Enchantment.byId(this.clues[i]);
            int u = 199, v = 225;
            if (clue == null) {
                u += 19;
                v += 16;
            }
            else if (this.selectedSlot == i || this.isHovering(8, 18 + 19 * i, 18, 18, pMouseX, pMouseY)) {
                u += 38;
            }
            gfx.blit(TEXTURES, 8, 18 + 19 * i, 224, u, 18, 19);
            gfx.blit(TEXTURES, 9, 22 + 18 * i + i, 16 * i, v, 16, 16);
        }

        int scrollbarPos = (int) (128F * this.scrollOffs);
        gfx.blit(TEXTURES, 220, 18 + scrollbarPos, 244, 173 + (this.isScrollBarActive() ? 0 : 15), 12, 15);

        EnchantmentDataWrapper hover = this.getHovered(pMouseX, pMouseY);
        for (int i = 0; i < 11; i++) {
            if (this.enchantments.size() - 1 < i) break;
            int v = 173;
            EnchantmentDataWrapper data = this.enchantments.get(this.startIndex + i);
            if (data.isBlacklisted) v += 26;
            else if (hover == this.enchantments.get(this.startIndex + i)) v += 13;
            gfx.blit(TEXTURES, 89, 18 + 13 * i, 96, v, 128, 13);
        }

        for (int i = 0; i < 11; i++) {
            if (this.enchantments.size() - 1 < i) break;
            EnchantmentDataWrapper data = this.enchantments.get(this.startIndex + i);
            if (data.isBlacklisted) {
                gfx.drawString(this.font, Component.translatable(data.getEnch().getDescriptionId()).withStyle(s -> s.withColor(0x58B0CC).withStrikethrough(true)), 91, 21 + 13 * i, 0xFFFF80, false);
            }
            else {
                gfx.drawString(this.font, I18n.get(data.getEnch().getDescriptionId()), 91, 21 + 13 * i, 0xFFFF80, false);
            }
        }

        List<Component> list = new ArrayList<>();
        Arcana a = Arcana.getForThreshold(this.parent.getMenu().stats.arcana());
        list.add(Component.translatable("info.apotheosis.weights").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.YELLOW));
        list.add(Component.translatable("info.apotheosis.weight", I18n.get("rarity.enchantment.common"), a.rarities[0]).withStyle(ChatFormatting.GRAY));
        list.add(Component.translatable("info.apotheosis.weight", I18n.get("rarity.enchantment.uncommon"), a.rarities[1]).withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("info.apotheosis.weight", I18n.get("rarity.enchantment.rare"), a.rarities[2]).withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("info.apotheosis.weight", I18n.get("rarity.enchantment.very_rare"), a.rarities[3]).withStyle(ChatFormatting.GOLD));
        gfx.renderComponentTooltip(this.font, list, a == Arcana.MAX ? -2 : 1, 120);

        gfx.drawString(this.font, this.title, 7, 4, 4210752, false);
        pose.popPose();
        pose.translate(0, 0, 10);

        for (int i = 0; i < 3; i++) {
            if (this.isHovering(8, 18 + 19 * i, 18, 18, pMouseX, pMouseY)) {
                list.clear();
                list.add(Component.translatable("info.apotheosis.enchinfo_slot", i + 1).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE));
                list.add(Component.translatable("info.apotheosis.enchinfo_level", this.costs[i]).withStyle(ChatFormatting.GREEN));
                list.add(Component.translatable("info.apotheosis.enchinfo_minpow", this.powers[i][0]).withStyle(ChatFormatting.DARK_RED));
                list.add(Component.translatable("info.apotheosis.enchinfo_maxpow", this.powers[i][1]).withStyle(ChatFormatting.BLUE));
                gfx.renderComponentTooltip(this.font, list, pMouseX, pMouseY);
            }
        }

        if (hover != null) {
            list.clear();
            list.add(Component.translatable(hover.getEnch().getDescriptionId()).withStyle(getColor(hover.getEnch()), ChatFormatting.UNDERLINE));
            list.add(Component.translatable("info.apotheosis.enchinfo_level", Component.translatable("enchantment.level." + hover.getLevel())).withStyle(ChatFormatting.DARK_AQUA));
            Component rarity = Component.translatable("rarity.enchantment." + hover.getEnch().getRarity().name().toLowerCase(Locale.ROOT)).withStyle(colors[hover.getEnch().getRarity().ordinal()]);
            list.add(Component.translatable("info.apotheosis.enchinfo_rarity", rarity).withStyle(ChatFormatting.DARK_AQUA));
            list.add(Component.translatable("info.apotheosis.enchinfo_chance", String.format("%.2f", 100F * hover.getWeight().asInt() / WeightedRandom.getTotalWeight(this.enchantments)) + "%").withStyle(ChatFormatting.DARK_AQUA));
            if (I18n.exists(hover.getEnch().getDescriptionId() + ".desc")) {
                list.add(Component.translatable(hover.getEnch().getDescriptionId() + ".desc").withStyle(ChatFormatting.DARK_AQUA));
            }
            List<Enchantment> excls = this.exclusions.get(hover.getEnch());
            if (!excls.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < excls.size(); i++) {
                    sb.append(I18n.get(excls.get(i).getDescriptionId()));
                    if (i != excls.size() - 1) sb.append(", ");
                }
                list.add(Component.translatable("Exclusive With: %s", sb.toString()).withStyle(ChatFormatting.RED));
            }
            gfx.renderComponentTooltip(this.font, list, pMouseX, pMouseY);
        }

        gfx.renderFakeItem(this.toEnchant, this.leftPos + 49, this.topPos + 39);
        pose.translate(0, 0, -10);
        super.render(gfx, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.scrolling = false;

        int left = this.leftPos + 220;
        int top = this.topPos + 18;
        if (pMouseX >= left && pMouseX < left + 12 && pMouseY >= top && pMouseY < top + 143) {
            this.scrolling = true;
            this.mouseDragged(pMouseX, pMouseY, 0, pMouseX, pMouseY);
        }

        for (int i = 0; i < 3; i++) {
            Enchantment clue = Enchantment.byId(this.clues[i]);
            if (this.selectedSlot != i && clue != null && this.isHovering(8, 18 + 19 * i, 18, 18, pMouseX, pMouseY)) {
                this.selectedSlot = i;
                this.slider.setValue((this.slider.min() + this.slider.max()) / 2);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + 18;
            int j = i + 143;
            this.scrollOffs = ((float) pMouseY - i - 7.5F) / (j - i - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5D);
            return true;
        }
        else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.isScrollBarActive()) {
            int i = this.getOffscreenRows();
            this.scrollOffs = (float) (this.scrollOffs - pDelta / i);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * i + 0.5D);
        }
        return true;
    }

    private boolean isScrollBarActive() {
        return this.enchantments.size() > 11;
    }

    protected int getOffscreenRows() {
        return this.enchantments.size() - 11;
    }

    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= pX - 1 && pMouseX < pX + pWidth + 1 && pMouseY >= pY - 1 && pMouseY < pY + pHeight + 1;
    }

    protected void recomputeEnchantments() {
        Arcana arc = Arcana.getForThreshold(this.parent.getMenu().stats.arcana());
        Set<Enchantment> blacklist = this.parent.getMenu().stats.blacklist();
        this.enchantments = RealEnchantmentHelper.getAvailableEnchantmentResults(this.currentPower, this.toEnchant, this.treasure, Collections.emptySet())
            .stream()
            .map(e -> new ArcanaEnchantmentData(arc, e))
            .map(a -> new EnchantmentDataWrapper(a, blacklist.contains(a.data.enchantment)))
            .collect(Collectors.toList());
        if (this.startIndex + 11 >= this.enchantments.size()) {
            this.startIndex = 0;
            this.scrollOffs = 0;
        }
        this.exclusions.clear();
        for (EnchantmentDataWrapper d : this.enchantments) {
            if (blacklist.contains(d.getEnch())) continue;
            List<Enchantment> excls = new ArrayList<>();
            for (EnchantmentDataWrapper d2 : this.enchantments) {
                if (d != d2 && !d.getEnch().isCompatibleWith(d2.getEnch())) excls.add(d2.getEnch());
            }
            this.exclusions.put(d.getEnch(), excls);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected EnchantmentDataWrapper getHovered(double mouseX, double mouseY) {
        for (int i = 0; i < 11; i++) {
            if (this.enchantments.size() - 1 < i) break;
            if (this.isHovering(89, 18 + i * 13, 128, 13, mouseX, mouseY)) {
                EnchantmentDataWrapper data = this.enchantments.get(this.startIndex + i);
                return data.isBlacklisted ? null : data;
            }
        }
        return null;
    }

    public static ChatFormatting getColor(Enchantment ench) {
        return EnchModule.getEnchInfo(ench).isTreasure() ? ChatFormatting.GOLD : ChatFormatting.GREEN;
    }

    public class PowerSlider extends AbstractSliderButton {

        public PowerSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), 0);
            if (EnchantingInfoScreen.this.selectedSlot != -1 && this.value == 0) {
                this.value = this.normalizeValue(EnchantingInfoScreen.this.currentPower == 0 ? (this.max() + this.min()) / 2 : EnchantingInfoScreen.this.currentPower);
                this.applyValue();
            }
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("info.apotheosis.slider_power", EnchantingInfoScreen.this.currentPower));
        }

        @Override
        protected void applyValue() {
            EnchantingInfoScreen.this.currentPower = this.denormalizeValue(this.value);
            EnchantingInfoScreen.this.recomputeEnchantments();
        }

        public void setValue(int value) {
            if (!EnchantingInfoScreen.this.isDragging()) {
                this.value = this.normalizeValue(value);
                this.applyValue();
                this.updateMessage();
            }
        }

        /**
         * Converts an int value within the range into a slider percentage.
         */
        public double normalizeValue(double value) {
            return Mth.clamp((this.snapToStepClamp(value) - this.min()) / (this.max() - this.min()), 0.0D, 1.0D);
        }

        /**
         * Converts a slider percentage to its bounded int value.
         */
        public int denormalizeValue(double value) {
            return (int) this.snapToStepClamp(Mth.lerp(Mth.clamp(value, 0.0D, 1.0D), this.min(), this.max()));
        }

        private double snapToStepClamp(double valueIn) {
            if (this.step() > 0.0F) {
                valueIn = this.step() * Math.round(valueIn / this.step());
            }

            return Mth.clamp(valueIn, this.min(), this.max());
        }

        private int min() {
            return EnchantingInfoScreen.this.powers[EnchantingInfoScreen.this.selectedSlot][0];
        }

        private int max() {
            return EnchantingInfoScreen.this.powers[EnchantingInfoScreen.this.selectedSlot][1];
        }

        private float step() {
            return 1F / Math.max(this.max() - this.min(), 1);
        }
    }

    protected static class EnchantmentDataWrapper extends IntrusiveBase {

        protected final ArcanaEnchantmentData data;
        protected final boolean isBlacklisted;

        public EnchantmentDataWrapper(ArcanaEnchantmentData data, boolean isBlacklisted) {
            super(isBlacklisted ? 0 : data.getWeight().asInt());
            this.data = data;
            this.isBlacklisted = isBlacklisted;
        }

        public Enchantment getEnch() {
            return this.data.data.enchantment;
        }

        public int getLevel() {
            return this.data.data.level;
        }

    }

}
