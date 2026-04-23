package dev.shadowsoffire.apotheosis.socket.gem.storage;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.client.PipelinedRenderer;
import dev.shadowsoffire.apotheosis.net.GemCaseSelectPayload;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseScreen.SafeSlot;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Gem Safe selection buttons make up the selection grid of fake slots in the Gem Safe GUI.
 * <p>
 * These slots do not actually hold any items, but are used to select which gem is currently
 * active in the Gem Safe.
 */
public class GemCaseSelectButton extends AbstractButton {

    protected final GemCaseScreen screen;
    protected final int index;

    public GemCaseSelectButton(GemCaseScreen screen, int index, int x, int y) {
        super(x, y, 16, 16, CommonComponents.EMPTY);
        this.screen = screen;
        this.index = index;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        SafeSlot slot = this.getSafeSlot();
        if (slot == null) {
            return;
        }

        int count = this.screen.getMenu().getGemCount(slot.gem());
        if (count == 0) {
            PipelinedRenderer.ghostFakeItem(gfx, slot.displayStack(), this.getX(), this.getY());
        }
        else {
            gfx.fakeItem(slot.displayStack(), this.getX(), this.getY());
        }

        if (count > 1) {
            String countStr = GemCaseBlock.format(count);
            float scale = 1.0f;
            if (countStr.length() > 2) {
                scale = 2.0f / countStr.length();
            }
            gfx.pose().pushMatrix();
            gfx.pose().scale(scale, scale);
            float textX = (this.getX() + 16 - (mc.font.width(countStr) - 1) * scale) / scale;
            float textY = (this.getY() + 16 - (mc.font.lineHeight - 2) * scale) / scale;
            gfx.text(mc.font, countStr, (int) textX, (int) textY, 0xAAFFFFFF, true);
            gfx.pose().popMatrix();
        }

        if (this.isHovered()) {
            gfx.fill(this.getX(), this.getY(), this.getX() + 16, this.getY() + 16, 0x40FFFFFF);

            Component desc = Component.translatable(((GemItem) Apoth.Items.GEM.value()).getGemDescriptionId(slot.displayStack()));
            gfx.setTooltipForNextFrame(mc.font, desc, mouseX, mouseY);
        }
    }

    @Override
    public void onPress(InputWithModifiers input) {
        SafeSlot slot = this.getSafeSlot();
        if (slot != null) {
            DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(this.getSafeSlot().gem());
            this.screen.getMenu().setSelectedGem(holder);
            ClientPacketDistributor.sendToServer(new GemCaseSelectPayload(holder));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Nullable
    private SafeSlot getSafeSlot() {
        int idx = this.screen.startIndex * GemCaseScreen.SLOTS_PER_ROW + this.index;
        if (idx >= 0 && idx < this.screen.data.size()) {
            return this.screen.data.get(idx);
        }
        return null;
    }
}
