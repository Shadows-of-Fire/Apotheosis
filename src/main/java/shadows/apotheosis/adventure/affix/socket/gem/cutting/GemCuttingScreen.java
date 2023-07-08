package shadows.apotheosis.adventure.affix.socket.gem.cutting;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apoth.Items;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingScreen;
import shadows.apotheosis.adventure.affix.socket.gem.GemInstance;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingMenu.GemCuttingRecipe;
import shadows.apotheosis.adventure.client.GrayBufferSource;
import shadows.apotheosis.adventure.client.SimpleTexButton;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.core.attributeslib.api.AttributeHelper;
import shadows.placebo.screen.PlaceboContainerScreen;

public class GemCuttingScreen extends PlaceboContainerScreen<GemCuttingMenu> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/gem_cutting.png");

	protected final ItemStack displayDust = Apoth.Items.GEM_DUST.get().getDefaultInstance();

	protected ItemStack displayMat;
	protected SimpleTexButton upgradeBtn;

	public GemCuttingScreen(GemCuttingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.menu.addSlotListener((id, stack) -> this.updateBtnStatus());
		this.imageHeight = 180;
		this.titleLabelY = 5;
		//this.titleLabelX = 5;
		//this.inventoryLabelX = 5;
		this.inventoryLabelY = 86;
	}

	@Override
	protected void init() {
		super.init();
		int left = this.getGuiLeft();
		int top = this.getGuiTop();
		//Formatter::off
		this.upgradeBtn = this.addRenderableWidget(
				new SimpleTexButton(left + 125, top + 30, 40, 40, 216, 0, TEXTURE, 256, 256, 
						this::clickUpgradeBtn, 
						Component.translatable("button.apotheosis.upgrade"))
						.setInactiveMessage(Component.translatable("button.apotheosis.upgrade.no").withStyle(ChatFormatting.RED))
				);
		//Formatter::on
		updateBtnStatus();
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
		this.displayMat = gem.isEmpty() ? ItemStack.EMPTY : GemItem.getLootRarity(gem).getMaterial();
		if (this.upgradeBtn != null) this.upgradeBtn.active = false;
	}

	@Override
	protected void renderBg(PoseStack stack, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int xCenter = (this.width - this.imageWidth) / 2;
		int yCenter = (this.height - this.imageHeight) / 2;
		this.blit(stack, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight);
		if (hasItem(0) && GemItem.getLootRarity(this.menu.getSlot(0).getItem()) != LootRarity.ANCIENT) {
			if (!hasItem(1)) {
				renderItem(this.displayDust, this.menu.getSlot(1));
			}
			if (!hasItem(2)) {
				renderItem(this.menu.getSlot(0).getItem(), this.menu.getSlot(2));
			}
			if (!hasItem(3)) {
				renderItem(this.displayMat, this.menu.getSlot(3));
			}
		}
	}

	protected boolean hasItem(int slot) {
		return this.menu.getSlot(slot).hasItem();
	}

	protected void renderItem(ItemStack stack, Slot slot) {
		var model = itemRenderer.getModel(stack, null, null, 0);
		SalvagingScreen.renderGuiItem(stack, this.getGuiLeft() + slot.x, this.getGuiTop() + slot.y, model, GrayBufferSource::new);
	}

	@Override
	protected void renderTooltip(PoseStack poseStack, int pX, int pY) {
		ItemStack gemStack = this.menu.getSlot(0).getItem();
		GemInstance gem = GemInstance.unsocketed(gemStack);
		GemInstance secondary = GemInstance.unsocketed(this.menu.getSlot(2).getItem());
		List<Component> list = new ArrayList<>();
		if (gem.isValidUnsocketed()) {
			int dust = this.menu.getSlot(1).getItem().getCount();
			LootRarity rarity = gem.rarity();
			if (rarity == LootRarity.ANCIENT) {
				list.add(Component.translatable("text.apotheosis.no_upgrade").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE));
			} else {
				list.add(Component.translatable("text.apotheosis.cut_cost").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE));
				list.add(CommonComponents.EMPTY);
				boolean hasDust = dust > GemCuttingMenu.getDustCost(rarity);
				list.add(Component.translatable("text.apotheosis.cost", GemCuttingMenu.getDustCost(rarity), Items.GEM_DUST.get().getName(ItemStack.EMPTY)).withStyle(hasDust ? ChatFormatting.GREEN : ChatFormatting.RED));
				boolean hasGem2 = secondary.isValidUnsocketed() && gem.gem() == secondary.gem() && rarity == secondary.rarity();
				list.add(Component.translatable("text.apotheosis.cost", 1, gemStack.getHoverName().getString()).withStyle(hasGem2 ? ChatFormatting.GREEN : ChatFormatting.RED));
				list.add(Component.translatable("text.apotheosis.one_rarity_mat").withStyle(ChatFormatting.GRAY));
				addMatTooltip(rarity.next(), GemCuttingMenu.NEXT_MAT_COST, list);
				addMatTooltip(rarity, GemCuttingMenu.STD_MAT_COST, list);
				if (rarity != LootRarity.COMMON) {
					addMatTooltip(rarity.prev(), GemCuttingMenu.PREV_MAT_COST, list);
				}
			}
		}
		drawOnLeft(poseStack, list, this.getGuiTop() + 16);
		super.renderTooltip(poseStack, pX, pY);
	}

	private void addMatTooltip(LootRarity rarity, int cost, List<Component> list) {
		if (rarity == LootRarity.ANCIENT) {
			list.add(AttributeHelper.list().append(Component.translatable("text.apotheosis.cost", 1, Component.literal("Manifestation of Infinity").withStyle(ChatFormatting.OBFUSCATED)).withStyle(ChatFormatting.RED)));
		} else {
			Item rarityMat = rarity.getMaterial().getItem();
			ItemStack slotMat = this.menu.getSlot(3).getItem();
			boolean hasMats = slotMat.getItem() == rarityMat && slotMat.getCount() >= cost;
			list.add(AttributeHelper.list().append(Component.translatable("text.apotheosis.cost", cost, rarityMat.getName(ItemStack.EMPTY).getString()).withStyle(!hasMats ? ChatFormatting.RED : ChatFormatting.YELLOW)));
		}
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

		public void tick() {
			if (ticks == 4 || ticks == 9) {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_BREAK, this.pitch + this.pitchOff, 1.5F));
				pitchOff = -pitchOff;
			}
			if (ticks++ > 8) this.stop();
		}

		public static void start(BlockPos pos) {
			Minecraft.getInstance().getSoundManager().play(new GemUpgradeSound(pos));
		}
	}

}
