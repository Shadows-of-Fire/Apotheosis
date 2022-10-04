package shadows.apotheosis.adventure.affix.salvaging;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.client.SimpleTexButton;
import shadows.apotheosis.adventure.loot.LootRarity;

public class SalvagingScreen extends AbstractContainerScreen<SalvagingMenu> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/salvage.png");

	protected SimpleTexButton salvage;
	protected SimpleTexButton salvageCommon;
	protected SimpleTexButton salvageUncommon;
	protected SimpleTexButton salvageRare;
	protected SimpleTexButton salvageEpic;

	public SalvagingScreen(SalvagingMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.titleLabelX = 176 / 2 - Minecraft.getInstance().font.width(title) / 2;
		this.titleLabelY = 10;
		this.menu.setButtonUpdater(this::updateButtons);
	}

	@Override
	protected void init() {
		super.init();
		int left = this.getGuiLeft();
		int top = this.getGuiTop();
		//Formatter::off
		salvage = this.addRenderableWidget(new SimpleTexButton(left + 36, top + 27, 32, 32, 176, 48, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0), 
				new TranslatableComponent("button.apotheosis.salvage")).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage").withStyle(ChatFormatting.RED)));
		salvageCommon = this.addRenderableWidget(new SimpleTexButton(left + 84, top + 35, 16, 16, 176, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1), 
				new TranslatableComponent("button.apotheosis.salvage_all", LootRarity.COMMON.toComponent())).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage_all").withStyle(ChatFormatting.RED)));
		salvageUncommon = this.addRenderableWidget(new SimpleTexButton(left + 103, top + 35, 16, 16, 176 + 16, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 2), 
				new TranslatableComponent("button.apotheosis.salvage_all", LootRarity.UNCOMMON.toComponent())).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage_all").withStyle(ChatFormatting.RED)));
		salvageRare = this.addRenderableWidget(new SimpleTexButton(left + 122, top + 35, 16, 16, 176 + 32, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 3), 
				new TranslatableComponent("button.apotheosis.salvage_all", LootRarity.RARE.toComponent())).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage_all").withStyle(ChatFormatting.RED)));
		salvageEpic = this.addRenderableWidget(new SimpleTexButton(left + 141, top + 35, 16, 16, 176 + 48, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 4), 
				new TranslatableComponent("button.apotheosis.salvage_all", LootRarity.EPIC.toComponent())).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage_all").withStyle(ChatFormatting.RED)));
		//Formatter::on
		updateButtons();
	}

	public void updateButtons() {
		if (this.salvage == null) return;
		this.salvage.active = !this.menu.getSlot(0).getItem().isEmpty();

		this.salvageCommon.active = false;
		this.salvageUncommon.active = false;
		this.salvageRare.active = false;
		this.salvageEpic.active = false;

		for (int i = 1; i < this.menu.slots.size(); i++) {
			Slot s = this.menu.getSlot(i);
			ItemStack stack = s.getItem();
			LootRarity rarity = stack.hasTag() ? AffixHelper.getRarity(stack) : null;
			if (rarity != null) {
				switch (rarity.ordinal()) {
				case 0:
					this.salvageCommon.active = true;
					break;
				case 1:
					this.salvageUncommon.active = true;
					break;
				case 2:
					this.salvageRare.active = true;
					break;
				case 3:
					this.salvageEpic.active = true;
					break;
				default:
				}
			}
		}
	}

	@Override
	public void removed() {
		super.removed();
	}

	@Override
	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBackground(pPoseStack);
		super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		RenderSystem.disableBlend();
		this.renderTooltip(pPoseStack, pMouseX, pMouseY);
	}

	@Override
	protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pX, int pY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);

	}

	@Override
	protected void renderLabels(PoseStack pPoseStack, int pX, int pY) {
		RenderSystem.disableBlend();
		super.renderLabels(pPoseStack, pX, pY);
	}

}