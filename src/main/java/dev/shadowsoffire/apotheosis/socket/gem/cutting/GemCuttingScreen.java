package dev.shadowsoffire.apotheosis.socket.gem.cutting;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.client.AdventureContainerScreen;
import dev.shadowsoffire.apotheosis.client.SimpleTexButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class GemCuttingScreen extends AdventureContainerScreen<GemCuttingMenu> {

    public static final ResourceLocation TEXTURE = Apotheosis.loc("textures/gui/gem_cutting.png");

    protected SimpleTexButton upgradeBtn;

    public GemCuttingScreen(GemCuttingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.menu.slotChangedCallback = this::updateBtnStatus;
        this.imageHeight = 180;
        this.titleLabelY = 5;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.getGuiLeft();
        int top = this.getGuiTop();

        this.upgradeBtn = this.addRenderableWidget(
            new SimpleTexButton(left + 135, top + 44, 18, 18, 238, 0, TEXTURE, 256, 256,
                this::clickUpgradeBtn,
                Component.translatable("button.apotheosis.upgrade"))
                .setInactiveMessage(Component.translatable("button.apotheosis.upgrade.no").withStyle(ChatFormatting.RED)));

        this.updateBtnStatus();
    }

    protected void clickUpgradeBtn(Button btn) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        GemUpgradeSound.start(this.menu.player.blockPosition());
    }

    protected void updateBtnStatus() {
        for (RecipeHolder<GemCuttingRecipe> holder : GemCuttingMenu.getRecipes(Minecraft.getInstance().level)) {
            GemCuttingRecipe r = holder.value();
            if (r.matches(this.menu.rInput, Minecraft.getInstance().level)) {
                this.upgradeBtn.active = true;
                return;
            }
        }
        if (this.upgradeBtn != null) {
            this.upgradeBtn.active = false;
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float pPartialTick, int pMouseX, int pMouseY) {
        int xCenter = (this.width - this.imageWidth) / 2;
        int yCenter = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURE, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected static class GemUpgradeSound extends AbstractTickableSoundInstance {

        protected int ticks = 0;
        protected float pitchOff;

        public GemUpgradeSound(BlockPos pos) {
            super(SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, Minecraft.getInstance().level.random);
            this.x = pos.getX() + 0.5F;
            this.y = pos.getY();
            this.z = pos.getZ() + 0.5F;
            this.volume = 1.5F;
            this.pitch = 1.5F + 0.35F * (1 - 2 * this.random.nextFloat());
            this.pitchOff = 0.35F * (1 - 2 * this.random.nextFloat());
            this.delay = 999;
        }

        @Override
        public void tick() {
            if (this.ticks == 4 || this.ticks == 9) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_BREAK, this.pitch + this.pitchOff, 1.5F));
                this.pitchOff = -this.pitchOff;
            }
            if (this.ticks++ > 8) {
                this.stop();
            }
        }

        public static void start(BlockPos pos) {
            Minecraft.getInstance().getSoundManager().play(new GemUpgradeSound(pos));
        }
    }

}
