package shadows.apotheosis.adventure.affix.reforging;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.client.SimpleTexButton;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.ClientUtil;

public class ReforgingScreen extends AbstractContainerScreen<ReforgingMenu> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/reforge.png");

	protected Button forgeButton;
	protected int topAnimTime;
	protected int botAnimTime;
	protected int midAnimTime;
	protected int cenAnimTime;
	protected int finAnimTime;

	public ReforgingScreen(ReforgingMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.titleLabelX = 176 / 2 - Minecraft.getInstance().font.width(title) / 2;
		this.titleLabelY = 4;
	}

	@Override
	protected void init() {
		super.init();
		int left = this.getGuiLeft();
		int top = this.getGuiTop();
		forgeButton = this.addRenderableWidget(new SimpleTexButton(left + 110, top + 33, 20, 20, 176, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0)));
		forgeButton.active = false;
	}

	@Override
	protected void containerTick() {
		ItemStack topMat = this.menu.getSlot(0).getItem();
		if (!topMat.isEmpty()) {
			topAnimTime++;
		} else topAnimTime = 0;

		if (!this.menu.getSlot(1).getItem().isEmpty()) {
			botAnimTime++;
		} else botAnimTime = 0;

		if (botAnimTime >= 20 && topAnimTime >= 20) {
			midAnimTime++;
		} else midAnimTime = 0;

		ItemStack dust = this.menu.getSlot(2).getItem();
		LootRarity rarity = AdventureModule.RARITY_MATERIALS.inverse().get(topMat.getItem().delegate);
		if (rarity != null && midAnimTime >= 12 && !dust.isEmpty()) {
			int dustNeeded = rarity.ordinal() + 1;
			float dustHad = dust.getCount();
			float maxWidth = Math.min(15, (15 * (dustHad / dustNeeded)));

			if (Math.min(d(cenAnimTime), maxWidth) < maxWidth) {
				cenAnimTime++;
			} else if (Math.min(d(cenAnimTime), maxWidth) > maxWidth) {
				cenAnimTime--;
			}
		} else cenAnimTime = 0;

		if (cenAnimTime >= 12 && !this.menu.getSlot(3).getItem().isEmpty()) {
			finAnimTime++;
		} else finAnimTime = 0;

		if (finAnimTime >= 30) {
			forgeButton.active = true;
		} else forgeButton.active = false;
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

	protected float getTopAnimX() {
		return Math.min(d(topAnimTime), 9);
	}

	protected float getTopAnimY() {
		return Mth.clamp(d(topAnimTime) - 6, 3, 15);
	}

	float d(int t) {
		return (t + this.minecraft.getDeltaFrameTime()) / 0.75F;
	}

	protected float getBotAnimX() {
		return Math.min(d(botAnimTime), 9);
	}

	protected float getBotAnimY() {
		return Mth.clamp(d(botAnimTime) - 6, 3, 14);
	}

	@Override
	protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pX, int pY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
		ItemStack topMat = this.menu.getSlot(0).getItem();
		ItemStack botMat = this.menu.getSlot(1).getItem();
		if (!topMat.isEmpty()) {
			LootRarity rarity = AdventureModule.RARITY_MATERIALS.inverse().get(topMat.getItem().delegate);
			ClientUtil.colorBlit(pPoseStack, i + 27, j + 27, 0, 166, getTopAnimX(), getTopAnimY(), rarity.color().getValue());
			if (!botMat.isEmpty()) {
				if (botMat.getItem() == topMat.getItem()) {
					if (midAnimTime > 0) ClientUtil.colorBlit(pPoseStack, i + 33, j + 42, 6, 181, Math.min(d(midAnimTime), 8), 3, rarity.color().getValue());
					ItemStack dust = this.menu.getSlot(2).getItem();
					if (!dust.isEmpty()) {
						int dustNeeded = rarity.ordinal() + 1;
						float dustHad = dust.getCount();
						float maxWidth = Math.min(15, (15 * (dustHad / dustNeeded)));
						ClientUtil.colorBlit(pPoseStack, i + 63, j + 42, 36, 181, Math.min(d(cenAnimTime), maxWidth), 3, rarity.color().getValue());
						ItemStack main = this.menu.getSlot(3).getItem();
						if (!main.isEmpty() && dustHad >= dustNeeded) {
							ClientUtil.colorBlit(pPoseStack, i + 100, j + 36, 73, 175, Math.min(d(finAnimTime), 40), 15, rarity.color().getValue());
						}
					}
				} else {
					ClientUtil.colorBlit(pPoseStack, i + 33, j + 42, 6, 181, Math.min(d(midAnimTime), 8), 3, 0);
				}
			}
		}
		if (!botMat.isEmpty()) {
			LootRarity rarity = AdventureModule.RARITY_MATERIALS.inverse().get(botMat.getItem().delegate);
			float animX = getBotAnimX();
			float animY = getBotAnimY();
			ClientUtil.colorBlit(pPoseStack, i + 27, j + 45 + (14 - animY), 0, 184 + (14 - animY), animX, animY, rarity.color().getValue());
		}

		//this.blit(pPoseStack, i + 59, j + 20, 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);
		if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
			//this.blit(pPoseStack, i + 99, j + 45, this.imageWidth, 0, 28, 21);
		}
	}

	@Override
	protected void renderLabels(PoseStack pPoseStack, int pX, int pY) {
		RenderSystem.disableBlend();
		super.renderLabels(pPoseStack, pX, pY);
	}
}