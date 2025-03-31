package dev.shadowsoffire.apotheosis.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.shadowsoffire.placebo.PlaceboClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record StoneformingTooltipRenderer(StoneformingComponent comp) implements ClientTooltipComponent {

    public static final ResourceLocation SOCKET = Apotheosis.loc("textures/gui/socket.png");

    public static final Component EMPTY_SPACE_PLACEHOLDER = Component.literal(" ".repeat(8));

    @Override
    public int getHeight() {
        return Minecraft.getInstance().font.lineHeight + 2;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(getText(this.comp.inst));
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
        if (affix().getCandidates().size() == 0) {
            return;
        }

        String text = I18n.get("affix.apotheosis.stoneforming.desc", "<M1>", "<M2>");
        int xPos = font.width(text.substring(0, text.indexOf("<M1>") + 1));

        StoneformingAffix affix = affix();
        Block[] selected = new Block[3];
        int start = (int) (PlaceboClient.ticks / 20) * 3;
        for (int i = 0; i < 3; i++) {
            selected[i] = affix.getCandidates().get((start + i) % affix.getCandidates().size()).value();
        }

        PoseStack pose = gfx.pose();
        pose.pushPose();
        pose.translate(0, -0.25F, 0);
        pose.scale(0.5F, 0.5F, 1);
        for (Block block : selected) {
            ItemStack stack = new ItemStack(block);
            gfx.renderFakeItem(stack, (x + xPos) * 2 + 8, y * 2);
            xPos += 10;
        }
        pose.popPose();
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        font.drawInBatch(getText(this.comp.inst()), x, y, 0xAABBCC, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

    }

    private StoneformingAffix affix() {
        return ((StoneformingAffix) this.comp.inst.getAffix());
    }

    public static Component getText(AffixInstance inst) {
        Component blockName = ((StoneformingAffix) inst.getAffix()).getTarget(inst).getName().withStyle(ChatFormatting.BLUE);
        Component afxDesc = Apotheosis.lang("affix", "stoneforming.desc", Component.literal(" ".repeat(8)), blockName);
        return Apotheosis.lang("text", "dot_prefix", afxDesc).withStyle(ChatFormatting.YELLOW);
    }

    public static record StoneformingComponent(AffixInstance inst) implements TooltipComponent {}

}
