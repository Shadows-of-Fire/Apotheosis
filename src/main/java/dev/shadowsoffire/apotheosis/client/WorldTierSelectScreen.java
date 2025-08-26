package dev.shadowsoffire.apotheosis.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.net.WorldTierPayload;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorldTierSelectScreen extends Screen {

    public static final ResourceLocation TEXTURE = Apotheosis.loc("textures/gui/mountain.png");
    public static final ResourceLocation SEPARATOR_LINE = Apotheosis.loc("textures/gui/separator_line.png");
    public static final ResourceLocation SWORD_EMPTY = Apotheosis.loc("textures/gui/sword_empty.png");
    public static final ResourceLocation SWORD_FULL = Apotheosis.loc("textures/gui/sword_full.png");

    public static final AnimationData HAVEN_ANIMATION = new AnimationData(138, 156, 21, 320, 10, Apotheosis.loc("textures/gui/animations/haven.png"));
    public static final AnimationData FRONTIER_ANIMATION = new AnimationData(210, 236, 45, 588, 21, Apotheosis.loc("textures/gui/animations/frontier.png"));
    public static final AnimationData ASCENT_ANIMATION = new AnimationData(251, 106, 42, 1380, 30, Apotheosis.loc("textures/gui/animations/ascent.png"));
    public static final AnimationData SUMMIT_ANIMATION = new AnimationData(349, 41, 5, 640, 20, Apotheosis.loc("textures/gui/animations/summit.png"));
    public static final AnimationData PINNACLE_ANIMATION = new AnimationData(356, -2, 47, 960, 12, Apotheosis.loc("textures/gui/animations/pinnacle.png"));

    public static final int GUI_WIDTH = 480;
    public static final int GUI_HEIGHT = 270;
    public static final int IMAGE_WIDTH = 498;
    public static final int IMAGE_HEIGHT = 286;

    protected SimpleTexButton activateButton, detailButton, tutorialButton;
    protected WorldTier displayedTier = WorldTier.getTier(Minecraft.getInstance().player);
    protected int leftPos, topPos;
    protected Map<WorldTier, SimpleTexButton> tierButtons = new EnumMap<>(WorldTier.class);
    protected int animTicks = 0;

    public WorldTierSelectScreen() {
        super(Apotheosis.lang("title", "select_world_tier"));
    }

    @Override
    protected void init() {
        this.leftPos = Math.max(0, (this.width - GUI_WIDTH) / 2);
        this.topPos = Math.max(0, (this.height - GUI_HEIGHT) / 2);

        addTierButton(WorldTier.HAVEN, b -> b.pos(leftPos + 100, topPos + 215));
        addTierButton(WorldTier.FRONTIER, b -> b.pos(leftPos + 210, topPos + 205));
        addTierButton(WorldTier.ASCENT, b -> b.pos(leftPos + 230, topPos + 115));
        addTierButton(WorldTier.SUMMIT, b -> b.pos(leftPos + 315, topPos + 60));
        addTierButton(WorldTier.PINNACLE, b -> b.pos(leftPos + 395, topPos));

        this.activateButton = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(60, 24)
                .pos(leftPos + 198, topPos + 15)
                .texture(SimpleTexButton.APOTH_SPRITES)
                .action(activateSelectedTier())
                .buttonText(Apotheosis.lang("button", "activate_tier"))
                .build());

        this.detailButton = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(80, 20)
                .pos(leftPos + 178, topPos + 75)
                .texture(SimpleTexButton.APOTH_SPRITES)
                .action(openDetailedInfoScreen())
                .buttonText(Apotheosis.lang("button", "show_detailed_info"))
                .message(Apotheosis.lang("button", "show_detailed_info.desc"))
                .build());

        this.tutorialButton = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(12, 15)
                .pos(leftPos + GUI_WIDTH - 14, topPos + GUI_HEIGHT - 17)
                .texture(SimpleTexButton.APOTH_SPRITES)
                .action(btn -> {
                    this.minecraft.pushGuiLayer(new WorldTierTutorialScreen(this, Apotheosis.lang("title", "world_tier_tutorial")));
                })
                .buttonText(Component.literal("?"))
                .message(Apotheosis.lang("button", "open_world_tier_tutorial"))
                .build());

        this.updateButtonStatus();

        if (this.minecraft.screen == this && WorldTier.isTutorialActive(this.minecraft.player) && WorldTier.isUnlocked(this.minecraft.player, WorldTier.HAVEN)) {
            this.minecraft.pushGuiLayer(new WorldTierTutorialScreen(this, Apotheosis.lang("title", "world_tier_tutorial")));
        }
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gfx, mouseX, mouseY, partialTick);

        int imgLeft = (this.width - IMAGE_WIDTH) / 2;
        int imgTop = (this.height - IMAGE_HEIGHT) / 2;

        gfx.blit(TEXTURE, imgLeft, imgTop, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

        PoseStack pose = gfx.pose();
        pose.pushPose();

        float scale = 3;
        pose.scale(scale, scale, 1);
        Component title = Apotheosis.lang("text", "world_tier." + this.displayedTier.getSerializedName());
        gfx.drawString(font, title.getVisualOrderText(), (leftPos + 15) / scale, (topPos + 15) / scale, 0xFFFFFF, true);
        pose.popPose();

        Component desc = Apotheosis.lang("text", "world_tier." + this.displayedTier.getSerializedName() + ".desc");
        gfx.drawString(font, desc, leftPos + 15, topPos + 45, 0xC8C86E);

        gfx.blit(SEPARATOR_LINE, leftPos, topPos + 50, 0, 0, 0, 275, 30, 275, 30);

        Component diffText = Component.literal("Difficulty:").withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
        gfx.drawString(font, diffText.getVisualOrderText(), leftPos + 15, topPos + 80, 0xFFFFFF, true);

        pose.pushPose();
        scale = 0.5F;
        pose.scale(scale, scale, 1);
        for (int i = 0; i < 5; i++) {
            ResourceLocation tex = this.displayedTier.ordinal() >= i ? SWORD_FULL : SWORD_EMPTY;
            int swordLeft = leftPos + font.width(diffText) + 20 + i * (int) (30 * scale);
            gfx.blit(tex, (int) (swordLeft / scale), (int) ((topPos + 77) / scale), 0, 0, 0, 30, 30, 30, 30);
        }
        pose.popPose();

        AnimationData anim = switch (this.displayedTier) {
            case HAVEN -> HAVEN_ANIMATION;
            case FRONTIER -> FRONTIER_ANIMATION;
            case ASCENT -> ASCENT_ANIMATION;
            case SUMMIT -> SUMMIT_ANIMATION;
            case PINNACLE -> PINNACLE_ANIMATION;
        };

        anim.render(gfx, leftPos, topPos, this.animTicks, partialTick);
    }

    @Override
    public void tick() {
        this.animTicks++;
    }

    protected OnPress displayTier(WorldTier tier) {
        // Switches the main screen display to the selected world tier.
        // There's a separate button for actually locking in that world tier.
        return btn -> {
            this.displayedTier = tier;
            this.updateButtonStatus();
            this.animTicks = 0;
        };
    }

    private OnPress activateSelectedTier() {
        return btn -> {
            WorldTier tier = this.displayedTier;
            if (WorldTier.getTier(Minecraft.getInstance().player) != tier || WorldTier.isTutorialActive(Minecraft.getInstance().player)) {
                PacketDistributor.sendToServer(new WorldTierPayload(tier));
                this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
            }
            btn.active = false;
            this.activateButton.setButtonText(Apotheosis.lang("button", "activated").withColor(0x9A669C));
            this.activateButton.setMessage(Apotheosis.lang("button", "already_activated").withStyle(ChatFormatting.RED));
        };
    }

    protected OnPress openDetailedInfoScreen() {
        return btn -> {
            Minecraft.getInstance().pushGuiLayer(new WorldTierDetailScreen(this.displayedTier));
        };
    }

    void closeTutorial() {
        if (this.activateButton.isActive()) {
            this.activateButton.onPress();
        }
    }

    protected void updateButtonStatus() {
        LocalPlayer player = Minecraft.getInstance().player;

        for (WorldTier tier : WorldTier.values()) {
            SimpleTexButton button = this.tierButtons.get(tier);
            if (WorldTier.isUnlocked(player, tier)) {
                button.active = true;
                button.setMessage(Apotheosis.lang("button", tier.getSerializedName()));
            }
            else {
                button.active = false;
                button.setMessage(Apotheosis.lang("button", "tier_locked", Apotheosis.lang("button", tier.getSerializedName())).withStyle(ChatFormatting.RED));
            }
            button.forceHovered = this.displayedTier == tier;
        }

        this.activateButton.active = WorldTier.getTier(player) != this.displayedTier;
        if (WorldTier.isTutorialActive(player) && this.displayedTier == WorldTier.HAVEN) {
            this.activateButton.active = WorldTier.isUnlocked(player, displayedTier);
        }

        if (this.activateButton.active) {
            this.activateButton.setButtonText(Apotheosis.lang("button", "activate").withColor(0xFAA8FF));
            Component tierName = Apotheosis.lang("text", "world_tier." + this.displayedTier.getSerializedName()).withStyle(ChatFormatting.GOLD);
            this.activateButton.setMessage(Apotheosis.lang("button", "activate_tier", tierName));

            if (!AdventureConfig.enableManualWorldTierChanges) {
                this.activateButton.active = false;
                this.activateButton.setButtonText(Apotheosis.lang("button", "disabled").withStyle(ChatFormatting.RED));
                this.activateButton.setMessage(Apotheosis.lang("button", "tier_changes_disabled").withStyle(ChatFormatting.RED));
            }
        }
        else if (WorldTier.isTutorialActive(player) && !WorldTier.isUnlocked(player, displayedTier)) {
            this.activateButton.setButtonText(Apotheosis.lang("button", "inactive").withStyle(ChatFormatting.RED));
            this.activateButton.setMessage(Apotheosis.lang("button", "locked").withStyle(ChatFormatting.RED));
        }
        else {
            this.activateButton.setButtonText(Apotheosis.lang("button", "activated").withColor(0x9A669C));
            this.activateButton.setMessage(Apotheosis.lang("button", "already_activated").withStyle(ChatFormatting.GOLD));
        }
    }

    private void addTierButton(WorldTier tier, UnaryOperator<SimpleTexButton.Builder> config) {
        SimpleTexButton button = config.apply(
            SimpleTexButton.builder()
                .size(30, 30)
                .texture(Apotheosis.loc("textures/gui/buttons/" + tier.getSerializedName() + ".png"))
                .texSize(30, 90)
                .action(displayTier(tier))
                .message(Apotheosis.lang("button", tier.getSerializedName()))
                .inactiveMessage(tierLocked(tier)))
            .build();
        this.tierButtons.put(tier, button);
        this.addRenderableWidget(button);
    }

    private static List<Component> tierLocked(WorldTier tier) {
        ClientAdvancements advancements = Minecraft.getInstance().getConnection().getAdvancements();
        AdvancementHolder advancement = advancements.get(Apotheosis.loc("progression/" + tier.getSerializedName()));

        List<Component> list = new ArrayList<>();
        MutableComponent advName = Apotheosis.lang("advancements", "progression." + tier.getSerializedName() + ".title").withStyle(ChatFormatting.GOLD);

        if (advancement == null) {
            list.add(Apotheosis.lang("button", "tier_advancement", advName.withStyle(ChatFormatting.OBFUSCATED)).withStyle(ChatFormatting.RED));
            list.add(CommonComponents.SPACE);
            for (int i = 0; i < 3; i++) {
                list.add(Apotheosis.lang("info", "criteria_unknown", Component.literal("Do something, idk").withStyle(ChatFormatting.OBFUSCATED)).withStyle(ChatFormatting.GRAY));
            }
            return list;
        }

        list.add(Apotheosis.lang("button", "tier_advancement", advName).withStyle(ChatFormatting.RED));
        list.add(CommonComponents.SPACE);
        AdvancementProgress progress = advancements.progress.get(advancement);
        for (String criteria : progress.criteria.keySet()) {
            CriterionProgress critProg = progress.criteria.get(criteria);
            if (critProg.isDone()) {
                Component critDesc = Apotheosis.lang("advancements", "progression." + tier.getSerializedName() + ".criteria." + criteria).withStyle(ChatFormatting.GREEN);
                list.add(Apotheosis.lang("info", "criteria_done", critDesc));
            }
            else {
                Component critDesc = Apotheosis.lang("advancements", "progression." + tier.getSerializedName() + ".criteria." + criteria).withStyle(ChatFormatting.GRAY);
                list.add(Apotheosis.lang("info", "criteria_unfinished", critDesc));
            }
        }

        return list;
    }

    @Nullable
    private static AdvancementHolder getTierAdvancement(WorldTier tier) {
        return Minecraft.getInstance().getConnection().getAdvancements().get(Apotheosis.loc("progression/" + tier.getSerializedName()));
    }

    private static record AnimationData(int x, int y, int width, int height, int frames, ResourceLocation texture) {

        private void render(GuiGraphics gfx, int left, int top, int time, float partialTick) {
            int frameHeight = height / frames;
            int frame = (int) ((time + partialTick) / 2F);
            if (frame >= frames) {
                return;
            }
            RenderSystem.enableBlend();
            gfx.blit(texture, left + x, top + y, 0, (frame + 1F) * frameHeight, this.width, frameHeight, this.width, this.height);
        }

    }

}
