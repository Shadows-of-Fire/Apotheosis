package dev.shadowsoffire.apotheosis.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

/**
 * This screen is intended to be used as a GUI Layer over {@link WorldTierSelectScreen} to show the world tier tutorial.
 */
public class WorldTierTutorialScreen extends Screen {

    private final WorldTierSelectScreen parent;
    private TutorialStage stage = TutorialStage.INTRODUCTION;
    private SimpleTexButton skipButton, prevButton, nextButton;

    public WorldTierTutorialScreen(WorldTierSelectScreen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int imgLeft = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2;
        int imgTop = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2;

        skipButton = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(80, 20)
                .pos(imgLeft + 15, imgTop + 250)
                .texture(SimpleTexButton.APOTH_SPRITES)
                .action(btn -> {
                    closeTutorial();
                })
                .buttonText(Apotheosis.lang("button", "skip_tutorial"))
                .build());

        prevButton = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(60, 20)
                .pos(imgLeft + 340, imgTop + 250)
                .texture(SimpleTexButton.APOTH_SPRITES)
                .action(btn -> {
                    this.stage = this.stage.prev();
                    this.updateButtons();
                })
                .buttonText(Apotheosis.lang("button", "prev_tutorial"))
                .build());

        nextButton = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(60, 20)
                .pos(imgLeft + 420, imgTop + 250)
                .texture(SimpleTexButton.APOTH_SPRITES)
                .action(btn -> {
                    this.stage = this.stage.next();
                    this.updateButtons();
                    if (this.stage == null) {
                        closeTutorial();
                    }
                })
                .buttonText(Apotheosis.lang("button", "next_tutorial"))
                .build());

        this.updateButtons();
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int imgLeft = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2;
        int imgTop = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2;

        RenderSystem.enableBlend();
        gfx.blit(stage.overlay, imgLeft, imgTop, 0, 0, WorldTierSelectScreen.IMAGE_WIDTH, WorldTierSelectScreen.IMAGE_HEIGHT, WorldTierSelectScreen.IMAGE_WIDTH, WorldTierSelectScreen.IMAGE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        int imgLeft = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2;
        int imgTop = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2;
        PoseStack pose = gfx.pose();

        float scale = 2;
        pose.pushPose();
        pose.scale(scale, scale, 1);
        Component title = this.stage.title;
        gfx.drawString(font, title.getVisualOrderText(), (imgLeft + 380 - font.width(title) * scale / 2) / scale, (imgTop + 107) / scale, 0xFFFFFF, true);
        pose.popPose();

        Component desc = stage.description;
        if (stage == TutorialStage.ACTIVATE && !AdventureConfig.enableManualWorldTierChanges) {
            desc = Apotheosis.lang("tutorial", "world_tier.activate_disabled.desc").withStyle(ChatFormatting.DARK_AQUA);
        }

        List<FormattedCharSequence> split = this.font.split(desc, 200);

        for (int i = 0; i < split.size(); i++) {
            FormattedCharSequence line = split.get(i);
            gfx.drawString(font, line, imgLeft + 280, imgTop + 100 + font.lineHeight * 3 + (2 + font.lineHeight) * i, 0xFFFFFF, true);
        }

        // Re-render the relevant buttons from the parent so users can see the hovered tooltip when the button is focused.
        if (this.stage == TutorialStage.WORLD_TIERS) {
            for (SimpleTexButton btn : this.parent.tierButtons.values()) {
                btn.render(gfx, mouseX, mouseY, partialTick);
            }
        }
        else if (this.stage == TutorialStage.DETAILED_INFO) {
            this.parent.detailButton.render(gfx, mouseX, mouseY, partialTick);
        }
        else if (this.stage == TutorialStage.ACTIVATE) {
            this.parent.activateButton.render(gfx, mouseX, mouseY, partialTick);
        }

    }

    private void updateButtons() {
        skipButton.active = true;
        prevButton.active = stage != TutorialStage.INTRODUCTION;
        if (stage == TutorialStage.ACTIVATE) {
            nextButton.setButtonText(Apotheosis.lang("button", "done"));
        }
        else {
            nextButton.setButtonText(Apotheosis.lang("button", "next_tutorial"));
        }
    }

    private void closeTutorial() {
        this.minecraft.popGuiLayer();
        this.parent.closeTutorial();
    }

    private static enum TutorialStage {
        INTRODUCTION("introduction"),
        WORLD_TIERS("world_tiers"),
        TIER_NAME("tier_name"),
        TIER_DIFFICULTY("tier_difficulty"),
        DETAILED_INFO("detailed_info"),
        ACTIVATE("activate");

        private final ResourceLocation overlay;
        private final Component title;
        private final Component description;

        private TutorialStage(String name) {
            this.overlay = Apotheosis.loc("textures/gui/tutorial/" + name + ".png");
            this.title = Apotheosis.lang("tutorial", "world_tier." + name + ".title");
            this.description = Apotheosis.lang("tutorial", "world_tier." + name + ".desc");
        }

        @Nullable
        public TutorialStage next() {
            return switch (this) {
                case INTRODUCTION -> WORLD_TIERS;
                case WORLD_TIERS -> TIER_NAME;
                case TIER_NAME -> TIER_DIFFICULTY;
                case TIER_DIFFICULTY -> DETAILED_INFO;
                case DETAILED_INFO -> ACTIVATE;
                case ACTIVATE -> null;
            };
        }

        @Nullable
        public TutorialStage prev() {
            return switch (this) {
                case INTRODUCTION -> null;
                case WORLD_TIERS -> INTRODUCTION;
                case TIER_NAME -> WORLD_TIERS;
                case TIER_DIFFICULTY -> TIER_NAME;
                case DETAILED_INFO -> TIER_DIFFICULTY;
                case ACTIVATE -> DETAILED_INFO;
            };
        }
    }

}
