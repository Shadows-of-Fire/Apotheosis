package dev.shadowsoffire.apotheosis.client;

import java.util.Arrays;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class WorldTierDetailScreen extends Screen {

    public static final ResourceLocation TEXTURE = Apotheosis.loc("textures/gui/detail_column.png");

    public static final int BOX_WIDTH = 138;
    public static final int BOX_HEIGHT = 225;

    protected final WorldTier tier;

    protected WorldTierDetailScreen(WorldTier tier) {
        super(Apotheosis.lang("title", "world_tier_details"));
        this.tier = tier;
    }

    @Override
    protected void init() {
        int leftPos = (this.width - WorldTierSelectScreen.GUI_WIDTH) / 2;
        int topPos = (this.height - WorldTierSelectScreen.GUI_HEIGHT) / 2;

        this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(20, 20)
                .pos(leftPos + WorldTierSelectScreen.GUI_WIDTH - 15, topPos - 5)
                .texture(SimpleTexButton.APOTH_SPRITES)
                .action(btn -> Minecraft.getInstance().popGuiLayer())
                .buttonText(Apotheosis.lang("button", "return"))
                .message(Apotheosis.lang("button", "return.desc"))
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gfx, mouseX, mouseY, partialTick);

        int leftPos = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2 + 26;
        int topPos = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2 + 30;

        LocalPlayer player = Minecraft.getInstance().player;
        AttributeTooltipContext ctx = AttributeTooltipContext.of(player, TooltipContext.of(player.level()), ApothicAttributes.getTooltipFlag());

        for (int i = 0; i < 3; i++) {
            gfx.blit(TEXTURE, leftPos + i * (BOX_WIDTH + 16), topPos, 0, 0, BOX_WIDTH, BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);

            if (i == 2) {
                int x = leftPos + i * (BOX_WIDTH + 16);
                int y = topPos;

                Component header = Apotheosis.lang("text", "monster_augments").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
                gfx.drawCenteredString(font, header, x + BOX_WIDTH / 2, y + 12, 0);

                y += 20;

                for (TierAugment aug : TierAugmentRegistry.getAugments(this.tier, Target.MONSTERS)) {
                    y += 12;
                    Component comp = aug.getDescription(ctx).plainCopy().withStyle(ChatFormatting.RED);
                    drawScrollingStringWithoutMoving(gfx, font, comp, x + 12, x + BOX_WIDTH - 12, y, 0);
                }

            }
            else if (i == 1) {
                int x = leftPos + i * (BOX_WIDTH + 16);
                int y = topPos;

                Component header = Apotheosis.lang("text", "player_augments").withColor(0x00AAFF).withStyle(ChatFormatting.BOLD);
                gfx.drawCenteredString(font, header, x + BOX_WIDTH / 2, y + 12, 0);

                y += 20;

                for (TierAugment aug : TierAugmentRegistry.getAugments(this.tier, Target.PLAYERS)) {
                    y += 12;
                    Component comp = aug.getDescription(ctx).plainCopy().withColor(0x00AAFF);
                    drawScrollingStringWithoutMoving(gfx, font, comp, x + 12, x + BOX_WIDTH - 12, y, 0);
                }

            }
            else if (i == 0) {
                int x = leftPos + i * (BOX_WIDTH + 16);
                int y = topPos;

                Component header = Apotheosis.lang("text", "drop_chances").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
                gfx.drawCenteredString(font, header, x + BOX_WIDTH / 2, y + 12, 0);

                Component rarityHeader = Apotheosis.lang("text", "rarities").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE);
                gfx.drawCenteredString(font, rarityHeader, x + BOX_WIDTH / 2, y + 33, 0);

                y += 35;

                int totalWeight = RarityRegistry.INSTANCE.getValues().stream().mapToInt(r -> r.weights().getWeight(tier, 0)).sum();
                for (LootRarity rarity : RarityRegistry.getSortedRarities()) {
                    y += 12;
                    float percent = rarity.weights().getWeight(tier, 0) / (float) totalWeight;
                    MutableComponent comp = rarity.toComponent();
                    comp.append(Component.translatable(": %s", Affix.fmt(100 * percent) + "%").withStyle(s -> s.withColor(rarity.color())));
                    drawScrollingStringWithoutMoving(gfx, font, comp, x + 12, x + BOX_WIDTH - 12, y, 0xFFFFFF);
                }

                Component purityHeader = Apotheosis.lang("text", "purities").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE);
                gfx.drawCenteredString(font, purityHeader, x + BOX_WIDTH / 2, y + 13, 0);

                y += 15;

                Purity[] values = Purity.values();
                totalWeight = Arrays.stream(values).mapToInt(r -> r.weights().getWeight(tier, 0)).sum();
                for (Purity purity : values) {
                    y += 12;
                    float percent = purity.weights().getWeight(tier, 0) / (float) totalWeight;
                    MutableComponent comp = purity.toComponent();
                    comp.append(Component.translatable(": %s", Affix.fmt(100 * percent) + "%").withStyle(s -> s.withColor(purity.getColor())));
                    drawScrollingStringWithoutMoving(gfx, font, comp, x + 12, x + BOX_WIDTH - 12, y, 0xFFFFFF);
                }
            }

        }
    }

    /**
     * For some reason, {@link GuiGraphics#drawScrollingString} moves the string when it transitions to scrolling.
     * <p>
     * This version fixes that.
     */
    int drawScrollingStringWithoutMoving(GuiGraphics gfx, Font font, Component text, int minX, int maxX, int y, int color) {
        int maxWidth = maxX - minX;
        int textWidth = font.width(text.getVisualOrderText());
        if (textWidth <= maxWidth) {
            return gfx.drawString(font, text, minX, y, color);
        }
        else {
            AbstractWidget.renderScrollingString(gfx, font, text, minX, y - 1, maxX, y + font.lineHeight, color);
            return maxWidth;
        }
    }

}
