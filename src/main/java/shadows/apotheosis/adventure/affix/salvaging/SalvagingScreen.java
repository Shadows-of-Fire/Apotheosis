package shadows.apotheosis.adventure.affix.salvaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.client.SimpleTexButton;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.ClientUtil;

public class SalvagingScreen extends AbstractContainerScreen<SalvagingMenu> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/salvage.png");

	protected final int[] numOuts = new int[12];
	protected final ItemStack[] mats = new ItemStack[6];
	protected final TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
	protected final Component results = Component.translatable("text.apotheosis.results");
	protected SimpleTexButton salvageBtn;

	public SalvagingScreen(SalvagingMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.menu.setButtonUpdater(this::updateButtons);
		for (int i = 0; i < 5; i++) {
			mats[i] = new ItemStack(AdventureModule.RARITY_MATERIALS.get(LootRarity.values().get(i)).get());
			sprites[i] = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(Apotheosis.MODID, "items/" + mats[i].getItem().getRegistryName().getPath()));
		}
		this.titleLabelX--;
		this.inventoryLabelX--;
		this.inventoryLabelY++;
	}

	@Override
	protected void init() {
		super.init();
		int left = this.getGuiLeft();
		int top = this.getGuiTop();
		//Formatter::off
		salvageBtn = this.addRenderableWidget(
				new SimpleTexButton(left + 105, top + 33, 20, 20, 196, 0, TEXTURE, 256, 256, 
						(btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0), 
						Component.translatable("button.apotheosis.salvage"))
						.setInactiveMessage(Component.translatable("button.apotheosis.no_salvage").withStyle(ChatFormatting.RED))
				);
		//Formatter::on
		updateButtons();
	}

	public void updateButtons() {
		if (this.salvageBtn == null) return;
		Arrays.fill(numOuts, 0);
		boolean btnActive = false;

		for (int i = 0; i < 15; i++) {
			Slot s = this.menu.getSlot(i);
			ItemStack stack = s.getItem();
			LootRarity rarity = AffixHelper.getRarity(stack);
			if (rarity != null) {
				btnActive = true;
				int ord = rarity.ordinal();
				int[] counts = SalvagingMenu.getSalvageCounts(stack);
				numOuts[ord * 2] += counts[0];
				numOuts[ord * 2 + 1] += counts[1];
			}
		}

		this.salvageBtn.active = btnActive;
	}

	@Override
	public void render(PoseStack stack, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBackground(stack);
		super.render(stack, pMouseX, pMouseY, pPartialTick);
		int left = this.getGuiLeft();
		int top = this.getGuiTop();

		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.enableBlend();

		for (int i = 0; i < 5; i++) {
			if (numOuts[i * 2 + 1] > 0) {
				ClientUtil.colorBlit(stack, left + 134 + i % 2 * 18, top + 17 + i / 2 * 18, 100, 16, 16, sprites[i], 0x99FFFFFF);
			}
		}

		this.renderTooltip(stack, pMouseX, pMouseY);
	}

	@Override
	protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pX, int pY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		this.blit(pPoseStack, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderTooltip(PoseStack stack, int x, int y) {
		stack.pushPose();
		stack.translate(0, 0, -100);
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(Component.translatable("text.apotheosis.salvage_results").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));

		for (int i = 0; i < 5; i++) {
			if (numOuts[i * 2 + 1] > 0) {
				tooltip.add(Component.translatable("%s-%s %s", numOuts[i * 2], numOuts[i * 2 + 1], mats[i].getHoverName()));
			}
		}

		if (tooltip.size() > 1) drawOnLeft(stack, tooltip, this.getGuiTop() + 29);
		stack.popPose();

		super.renderTooltip(stack, x, y);
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
		list.forEach(comp -> split.addAll(this.font.getSplitter().splitLines(comp, lambdastupid, comp.getStyle())));

		this.renderComponentTooltip(stack, split, xPos, y, this.font);

		//GuiUtils.drawHoveringText(stack, list, xPos, y, width, height, maxWidth, this.font);
	}

	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
		this.font.draw(stack, this.results, 133, (float) this.titleLabelY, 4210752);
		super.renderLabels(stack, mouseX, mouseY);
	}

}