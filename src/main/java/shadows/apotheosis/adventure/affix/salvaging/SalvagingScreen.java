package shadows.apotheosis.adventure.affix.salvaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.client.SimpleTexButton;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.ClientUtil;

public class SalvagingScreen extends AbstractContainerScreen<SalvagingMenu> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/salvage.png");

	protected final Map<LootRarity, List<Slot>> itemSlots = new HashMap<>();
	protected final ItemStack[] mats = new ItemStack[4];
	protected SimpleTexButton[] buttons = new SimpleTexButton[5];

	public SalvagingScreen(SalvagingMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.titleLabelX = 176 / 2 - Minecraft.getInstance().font.width(title) / 2;
		this.titleLabelY = 6;
		this.menu.setButtonUpdater(this::updateButtons);
		for (int i = 0; i < 4; i++) {
			mats[i] = new ItemStack(AdventureModule.RARITY_MATERIALS.get(LootRarity.values().get(i)).get());
		}
	}

	@Override
	protected void init() {
		super.init();
		int left = this.getGuiLeft();
		int top = this.getGuiTop();
		//Formatter::off
		buttons[0] = this.addRenderableWidget(new SimpleTexButton(left + 36, top + 33, 20, 20, 196, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0), 
				new TranslatableComponent("button.apotheosis.salvage")).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage").withStyle(ChatFormatting.RED)));
		buttons[1] = this.addRenderableWidget(new SimpleTexButton(left + 70, top + 25, 20, 36, 176, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 1), 
				new TranslatableComponent("button.apotheosis.salvage_all", LootRarity.COMMON.toComponent())).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage_all").withStyle(ChatFormatting.RED)));
		buttons[2] = this.addRenderableWidget(new SimpleTexButton(left + 95, top + 25, 20, 36, 176, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 2), 
				new TranslatableComponent("button.apotheosis.salvage_all", LootRarity.UNCOMMON.toComponent())).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage_all").withStyle(ChatFormatting.RED)));
		buttons[3] = this.addRenderableWidget(new SimpleTexButton(left + 120, top + 25, 20, 36, 176, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 3), 
				new TranslatableComponent("button.apotheosis.salvage_all", LootRarity.RARE.toComponent())).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage_all").withStyle(ChatFormatting.RED)));
		buttons[4] = this.addRenderableWidget(new SimpleTexButton(left + 145, top + 25, 20, 36, 176, 0, TEXTURE, 256, 256, (btn) -> this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 4), 
				new TranslatableComponent("button.apotheosis.salvage_all", LootRarity.EPIC.toComponent())).setInactiveMessage(new TranslatableComponent("button.apotheosis.no_salvage_all").withStyle(ChatFormatting.RED)));
		//Formatter::on
		updateButtons();
	}

	public void updateButtons() {
		if (this.buttons[0] == null) return;
		this.itemSlots.clear();
		this.buttons[0].active = AffixHelper.getRarity(this.menu.getSlot(0).getItem()) != null;

		this.buttons[1].active = false;
		this.buttons[2].active = false;
		this.buttons[3].active = false;
		this.buttons[4].active = false;

		for (int i = 1; i < this.menu.slots.size(); i++) {
			Slot s = this.menu.getSlot(i);
			ItemStack stack = s.getItem();
			LootRarity rarity = stack.hasTag() ? AffixHelper.getRarity(stack) : null;
			if (rarity != null && rarity.ordinal() <= LootRarity.EPIC.ordinal()) {
				buttons[rarity.ordinal() + 1].active = true;
				this.itemSlots.computeIfAbsent(rarity, r -> new ArrayList<>()).add(s);
			}
		}
	}

	@Override
	public void render(PoseStack stack, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBackground(stack);
		super.render(stack, pMouseX, pMouseY, pPartialTick);
		int left = this.getGuiLeft();
		int top = this.getGuiTop();

		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.enableBlend();
		stack.pushPose();
		//stack.translate(0, 0, 1000);

		for (int i = 0; i < 4; i++) {
			if (buttons[i + 1].isHoveredOrFocused()) {
				LootRarity rarity = LootRarity.values().get(i);
				List<Slot> slots = this.itemSlots.getOrDefault(rarity, Collections.emptyList());
				for (Slot s : slots) {
					ClientUtil.colorBlit(stack, left + s.x, top + s.y, 80, 20, 16, 16, 0xAA000000 | rarity.color().getValue());
					//this.blit(stack, left + s.x, top + s.y, 216, 0, 16, 16);
				}
			}
		}
		stack.popPose();

		for (int i = 0; i < 4; i++) {
			Minecraft.getInstance().getItemRenderer().renderGuiItem(mats[i], left + 73 + 25 * i, top + 44);
		}

		this.renderTooltip(stack, pMouseX, pMouseY);
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

}