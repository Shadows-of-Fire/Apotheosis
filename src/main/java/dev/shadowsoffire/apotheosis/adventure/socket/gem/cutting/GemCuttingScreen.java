package dev.shadowsoffire.apotheosis.adventure.socket.gem.cutting;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.adventure.client.AdventureContainerScreen;
import dev.shadowsoffire.apotheosis.adventure.client.GrayBufferSource;
import dev.shadowsoffire.apotheosis.adventure.client.SimpleTexButton;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.cutting.GemCuttingMenu.GemCuttingRecipe;
import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GemCuttingScreen extends AdventureContainerScreen<GemCuttingMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/gem_cutting.png");

    protected final ItemStack displayDust = dev.shadowsoffire.apotheosis.adventure.Adventure.Items.GEM_DUST.get().getDefaultInstance();

    protected ItemStack displayMat;
    protected SimpleTexButton upgradeBtn;

    public GemCuttingScreen(GemCuttingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.menu.addSlotListener((id, stack) -> this.updateBtnStatus());
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
        ItemStack gem = this.menu.getSlot(0).getItem();
        ItemStack left = this.menu.getSlot(1).getItem();
        ItemStack bot = this.menu.getSlot(2).getItem();
        ItemStack right = this.menu.getSlot(3).getItem();
        for (GemCuttingRecipe r : GemCuttingMenu.RECIPES) {
            if (r.matches(gem, left, bot, right)) {
                this.upgradeBtn.active = true;
                return;
            }
        }
        this.displayMat = gem.isEmpty() ? ItemStack.EMPTY : new ItemStack(AffixHelper.getRarity(gem).get().getMaterial());
        if (this.upgradeBtn != null) this.upgradeBtn.active = false;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float pPartialTick, int pMouseX, int pMouseY) {
        int xCenter = (this.width - this.imageWidth) / 2;
        int yCenter = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURE, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight);
        if (this.hasItem(0) && GemCuttingMenu.isValidMainGem(this.menu.getSlot(0).getItem())) {
            if (!this.hasItem(1)) {
                this.renderGrayItem(gfx, this.displayDust, this.menu.getSlot(1));
            }
            if (!this.hasItem(2)) {
                this.renderGrayItem(gfx, this.menu.getSlot(0).getItem(), this.menu.getSlot(2));
            }
            if (!this.hasItem(3)) {
                this.renderGrayItem(gfx, this.displayMat, this.menu.getSlot(3));
            }
        }
    }

    protected boolean hasItem(int slot) {
        return this.menu.getSlot(slot).hasItem();
    }

    protected void renderGrayItem(GuiGraphics gfx, ItemStack stack, Slot slot) {
        SalvagingScreen.renderGuiItem(gfx, stack, this.getGuiLeft() + slot.x, this.getGuiTop() + slot.y, GrayBufferSource::new);
    }

    @Override
    protected void renderTooltip(GuiGraphics gfx, int pX, int pY) {
        ItemStack gemStack = this.menu.getSlot(0).getItem();
        GemInstance gem = GemInstance.unsocketed(gemStack);
        GemInstance secondary = GemInstance.unsocketed(this.menu.getSlot(2).getItem());
        List<Component> list = new ArrayList<>();
        if (gem.isValidUnsocketed()) {
            int dust = this.menu.getSlot(1).getItem().getCount();
            DynamicHolder<LootRarity> rarity = gem.rarity();
            if (rarity == RarityRegistry.getMaxRarity()) {
                list.add(Component.translatable("text.apotheosis.no_upgrade").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE));
            }
            else {
                list.add(Component.translatable("text.apotheosis.cut_cost").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE));
                list.add(CommonComponents.EMPTY);
                int dustCost = GemCuttingMenu.getDustCost(rarity.get());
                boolean hasDust = dust > dustCost;
                list.add(Component.translatable("text.apotheosis.cost", dustCost, Adventure.Items.GEM_DUST.get().getName(ItemStack.EMPTY))
                    .withStyle(hasDust ? ChatFormatting.GREEN : ChatFormatting.RED));
                boolean hasGem2 = secondary.isValidUnsocketed() && gem.gem() == secondary.gem() && rarity == secondary.rarity();
                list.add(Component.translatable("text.apotheosis.cost", 1, gemStack.getHoverName().getString()).withStyle(hasGem2 ? ChatFormatting.GREEN : ChatFormatting.RED));
                list.add(Component.translatable("text.apotheosis.one_rarity_mat").withStyle(ChatFormatting.GRAY));
                this.addMatTooltip(RarityRegistry.next(rarity), GemCuttingMenu.NEXT_MAT_COST, list);
                this.addMatTooltip(rarity, GemCuttingMenu.STD_MAT_COST, list);
                if (rarity != RarityRegistry.getMinRarity()) {
                    this.addMatTooltip(RarityRegistry.prev(rarity), GemCuttingMenu.PREV_MAT_COST, list);
                }
            }
        }
        this.drawOnLeft(gfx, list, this.getGuiTop() + 16);
        super.renderTooltip(gfx, pX, pY);
    }

    private void addMatTooltip(DynamicHolder<LootRarity> rarity, int cost, List<Component> list) {
        Item rarityMat = rarity.get().getMaterial();
        ItemStack slotMat = this.menu.getSlot(3).getItem();
        boolean hasMats = slotMat.getItem() == rarityMat && slotMat.getCount() >= cost;
        list.add(AttributeHelper.list().append(Component.translatable("text.apotheosis.cost", cost, rarityMat.getName(ItemStack.EMPTY)).withStyle(!hasMats ? ChatFormatting.RED : ChatFormatting.YELLOW)));
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
            if (this.ticks++ > 8) this.stop();
        }

        public static void start(BlockPos pos) {
            Minecraft.getInstance().getSoundManager().play(new GemUpgradeSound(pos));
        }
    }

}
