package shadows.apotheosis.adventure.affix.reforging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;

public class ReforgingScreen extends AbstractContainerScreen<ReforgingMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/reforge.png");

    protected ItemStack[] choices = new ItemStack[3];
    protected ItemStack lastInput = ItemStack.EMPTY;
    protected LootRarity lastRarity = null;
    protected Component title;

    public ReforgingScreen(ReforgingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.titleLabelY = 5;
        Arrays.fill(this.choices, ItemStack.EMPTY);
        this.title = Component.translatable("container.apotheosis.reforge");
    }

    public boolean shouldRecompute() {
        ItemStack input = this.menu.getSlot(0).getItem();
        LootRarity rarity = this.getMenu().getRarity();
        return !ItemStack.isSameItemSameTags(input, this.lastInput) || this.lastRarity != rarity;
    }

    public void recomputeChoices() {
        ItemStack input = this.menu.getSlot(0).getItem();
        LootRarity rarity = this.getMenu().getRarity();
        if (input.isEmpty() || rarity == null) {
            Arrays.fill(this.choices, ItemStack.EMPTY);
        }
        else {
            RandomSource rand = this.menu.random;
            for (int i = 0; i < 3; i++) {
                rand.setSeed(this.menu.getSeed() ^ ForgeRegistries.ITEMS.getKey(input.getItem()).hashCode() + i);
                this.choices[i] = LootController.createLootItem(input.copy(), rarity, rand);
            }
        }
        this.lastInput = input.copy();
        this.lastRarity = rarity;
    }

    @Override
    public void render(PoseStack pPoseStack, int x, int y, float pPartialTick) {
        if (this.shouldRecompute()) this.recomputeChoices();
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, x, y, pPartialTick);
        RenderSystem.disableBlend();
        this.renderTooltip(pPoseStack, x, y);

        int xCenter = (this.width - this.imageWidth) / 2;
        int yCenter = (this.height - this.imageHeight) / 2;

        int dust = this.menu.getDustCount();
        int mats = this.menu.getMatCount();
        int levels = this.menu.player.experienceLevel;
        LootRarity rarity = this.menu.getRarity();

        for (int slot = 0; slot < 3; ++slot) {
            ItemStack choice = this.choices[slot];
            if (choice.isEmpty() || this.menu.needsReset()) continue;
            List<Component> tooltips = new ArrayList<>();

            int dustCost = this.menu.getDustCost(slot, rarity);
            int matCost = this.menu.getMatCost(slot, rarity);
            int levelCost = this.menu.getLevelCost(slot, rarity);

            tooltips.add(Component.translatable("text.apotheosis.reforge_cost").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
            tooltips.add(CommonComponents.EMPTY);
            if (dustCost > 0) {
                tooltips.add(Component.translatable("%s %s", dustCost, Apoth.Items.GEM_DUST.get().getName(ItemStack.EMPTY)).withStyle(dust < dustCost ? ChatFormatting.RED : ChatFormatting.GRAY));
            }
            if (matCost > 0) {
                tooltips.add(Component.translatable("%s %s", matCost, this.menu.getSlot(1).getItem().getHoverName()).withStyle(mats < matCost ? ChatFormatting.RED : ChatFormatting.GRAY));
            }
            String key = levels >= levelCost ? levelCost == 1 ? "container.enchant.level.one" : "container.enchant.level.many" : "container.enchant.level.requirement";

            tooltips.add(Component.translatable(key, levelCost).withStyle(levels < levelCost ? ChatFormatting.RED : ChatFormatting.GRAY));

            int k2 = x - (xCenter + 60);
            int l2 = y - (yCenter + 14 + 19 * slot);
            if (k2 >= 0 && l2 >= 0 && k2 < 108 && l2 < 19) {
                this.renderTooltip(pPoseStack, choice.getTooltipLines(this.menu.player, TooltipFlag.Default.NORMAL), Optional.empty(), x, y, choice);
                pPoseStack.translate(0, 0, -100);
                this.drawOnLeft(pPoseStack, tooltips, this.getGuiTop() + 29);
                pPoseStack.translate(0, 0, 100);
            }
        }
    }

    @Override
    protected void renderBg(PoseStack stack, float partials, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int xCenter = (this.width - this.imageWidth) / 2;
        int slotsX = xCenter + 60;
        int yCenter = (this.height - this.imageHeight) / 2;
        this.blit(stack, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight);

        LootRarity rarity = this.menu.getRarity();

        if (this.menu.getSlot(0).getItem().isEmpty() || rarity == null || this.menu.needsReset()) {
            for (int slot = 0; slot < 3; ++slot) {
                this.blit(stack, slotsX, yCenter + 14 + 19 * slot, 0, 166 + 19, 108, 19);
            }
            return;
        }

        int dust = this.menu.getDustCount();
        int mats = this.menu.getMatCount();
        int levels = this.menu.player.experienceLevel;

        EnchantmentNames.getInstance().initSeed(this.menu.getSeed());
        for (int slot = 0; slot < 3; ++slot) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, TEXTURE);

            int dustCost = this.menu.getDustCost(slot, rarity);
            int matCost = this.menu.getMatCost(slot, rarity);
            int levelCost = this.menu.getLevelCost(slot, rarity);
            int maxCost = this.menu.getMatCost(2, rarity);

            String levelStr = "" + levelCost;
            String costStr = "" + matCost;
            int width = 86 - this.font.width(levelStr + maxCost);
            int randTextX = slotsX + 15 + this.font.width("" + maxCost);
            FormattedText randText = EnchantmentNames.getInstance().getRandomName(this.font, width);
            int color = 0x515151;
            if ((dust < dustCost || levels < levelCost || mats < matCost) && !this.minecraft.player.getAbilities().instabuild) {
                this.blit(stack, slotsX, yCenter + 14 + 19 * slot, 0, 166 + 19, 108, 19);
                this.blit(stack, slotsX + 1, yCenter + 15 + 19 * slot, 16 * slot, 239, 16, 16);
                this.font.drawWordWrap(randText, randTextX, yCenter + 16 + 19 * slot, width, color);
                color = this.darken(rarity.color().getValue(), 2);
            }
            else {
                int k2 = x - (xCenter + 60);
                int l2 = y - (yCenter + 14 + 19 * slot);
                if (k2 >= 0 && l2 >= 0 && k2 < 108 && l2 < 19) {
                    this.blit(stack, slotsX, yCenter + 14 + 19 * slot, 0, 166 + 38, 108, 19);
                    color = 0xFFFF80;
                }
                else {
                    this.blit(stack, slotsX, yCenter + 14 + 19 * slot, 0, 166, 108, 19);
                    color = 0xCDCDCD;
                }
                this.blit(stack, slotsX + 1, yCenter + 15 + 19 * slot, 16 * slot, 223, 16, 16);

                this.font.drawWordWrap(randText, randTextX, yCenter + 16 + 19 * slot, width, color);
                color = rarity.color().getValue();
            }
            this.drawBorderedString(stack, costStr, slotsX + 10, yCenter + 21 + 19 * slot, color, this.darken(color, 4));
            this.drawBorderedString(stack, levelStr, slotsX + 106 - this.font.width(levelStr), yCenter + 16 + 19 * slot + 7, color, this.darken(color, 4));
        }
    }

    protected int darken(int rColor, int factor) {
        int r = rColor >> 16 & 0xFF, g = rColor >> 8 & 0xFF, b = rColor & 0xFF;
        r /= factor;
        g /= factor;
        b /= factor;
        return r << 16 | g << 8 | b;
    }

    protected void drawBorderedString(PoseStack stack, String str, int x, int y, int color, int shadowColor) {
        Component comp = Component.literal(str);
        this.font.draw(stack, comp, x, y - 1, shadowColor);
        this.font.draw(stack, comp, x - 1, y, shadowColor);
        this.font.draw(stack, comp, x, y + 1, shadowColor);
        this.font.draw(stack, comp, x + 1, y, shadowColor);
        this.font.draw(stack, comp, x, y, color);
    }

    @Override
    protected void renderLabels(PoseStack stack, int x, int y) {
        this.font.draw(stack, this.title, this.titleLabelX, this.titleLabelY, 4210752);
        this.font.draw(stack, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        for (int k = 0; k < 3; ++k) {
            double d0 = pMouseX - (i + 60);
            double d1 = pMouseY - (j + 14 + 19 * k);
            if (d0 >= 0.0D && d1 >= 0.0D && d0 < 108.0D && d1 < 19.0D && this.menu.clickMenuButton(this.minecraft.player, k)) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, k);
                return true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public void drawOnLeft(PoseStack stack, List<Component> list, int y) {
        if (list.isEmpty()) return;
        int xPos = this.getGuiLeft() - 16 - list.stream().map(this.font::width).max(Integer::compare).get();
        int maxWidth = 9999;
        if (xPos < 0) {
            maxWidth = this.getGuiLeft() - 6;
            xPos = -8;
        }

        List<FormattedText> split = new ArrayList<>();
        int lambdastupid = maxWidth;
        list.forEach(comp -> split.addAll(this.font.getSplitter().splitLines(comp, lambdastupid, comp.getStyle())));

        this.renderComponentTooltip(stack, split, xPos, y, this.font);

        // GuiUtils.drawHoveringText(stack, list, xPos, y, width, height, maxWidth, this.font);
    }
}
