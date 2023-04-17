package shadows.apotheosis.adventure.affix.socket.gem.cutting;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth.Items;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.socket.gem.GemInstance;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.screen.PlaceboContainerScreen;

public class GemCuttingScreen extends PlaceboContainerScreen<GemCuttingMenu> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/gem_cutting.png");

	public GemCuttingScreen(GemCuttingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	@Override
	protected void renderBg(PoseStack stack, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int xCenter = (this.width - this.imageWidth) / 2;
		int yCenter = (this.height - this.imageHeight) / 2;
		this.blit(stack, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderTooltip(PoseStack poseStack, int pX, int pY) {
		ItemStack gemStack = this.menu.getSlot(1).getItem();
		GemInstance gem = new GemInstance(gemStack);
		List<Component> list = new ArrayList<>();
		if (gem.isValid()) {
			int dust = this.menu.getSlot(0).getItem().getCount();

			if (gem.isMaxed()) {
				if (gem.rarity() == LootRarity.ANCIENT) {
					list.add(Component.translatable("text.apotheosis.no_upgrade").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.YELLOW));
				} else {
					list.add(Component.translatable("text.apotheosis.rarity_up_cost").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
					list.add(Component.translatable("text.apotheosis.dust_cost", "4x", Items.GEM_DUST.get().getName(ItemStack.EMPTY)).withStyle(dust < 4 ? ChatFormatting.RED : ChatFormatting.GRAY));
					list.add(Component.translatable("text.apotheosis.mat_cost", "1x", gemStack.getHoverName()).withStyle(dust < 4 ? ChatFormatting.RED : ChatFormatting.GRAY));
				}
			} else {
				list.add(Component.translatable("text.apotheosis.cut_cost").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
				list.add(Component.translatable("text.apotheosis.dust_cost", "1x", Items.GEM_DUST.get().getName(ItemStack.EMPTY)).withStyle(dust < 1 ? ChatFormatting.RED : ChatFormatting.GRAY));
				list.add(Component.empty());
				if (gem.rarity() == LootRarity.ANCIENT) {
					list.add(Component.translatable("text.apotheosis.mat_cost", "1x", Component.literal("Manifestation of Infinity").withStyle(ChatFormatting.OBFUSCATED)).withStyle(ChatFormatting.RED));
				} else {
					Item rarityMat = AdventureModule.RARITY_MATERIALS.get(gem.rarity());
					ItemStack slotMat = this.menu.getSlot(2).getItem();
					boolean hasMats = slotMat.getItem() == rarityMat;
					list.add(Component.translatable("text.apotheosis.mat_cost", "1x", rarityMat.getName(ItemStack.EMPTY)).withStyle(!hasMats ? ChatFormatting.RED : ChatFormatting.GRAY));
				}

				if (gem.rarity() != LootRarity.COMMON) {
					list.add(Component.literal("or").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
					Item rarityMat = AdventureModule.RARITY_MATERIALS.get(gem.rarity().prev());
					ItemStack slotMat = this.menu.getSlot(2).getItem();
					boolean hasMats = slotMat.getItem() == rarityMat;
					list.add(Component.translatable("text.apotheosis.mat_cost", "4x", rarityMat.getName(ItemStack.EMPTY)).withStyle(!hasMats ? ChatFormatting.RED : ChatFormatting.GRAY));
				}
			}
		}
		drawOnLeft(poseStack, list, this.getGuiTop() + 30);

		super.renderTooltip(poseStack, pX, pY);
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
		list.forEach(comp -> {
			if (comp.getContents() == ComponentContents.EMPTY) split.add(comp);
			else split.addAll(this.font.getSplitter().splitLines(comp, lambdastupid, comp.getStyle()));
		});

		this.renderComponentTooltip(stack, split, xPos, y, this.font);

		//GuiUtils.drawHoveringText(stack, list, xPos, y, width, height, maxWidth, this.font);
	}

}
