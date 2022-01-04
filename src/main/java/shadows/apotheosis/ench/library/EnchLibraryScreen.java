package shadows.apotheosis.ench.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2ShortMap.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import shadows.apotheosis.Apotheosis;
import shadows.placebo.Placebo;
import shadows.placebo.packets.ButtonClickMessage;
import shadows.placebo.util.ClientUtil;

public class EnchLibraryScreen extends AbstractContainerScreen<EnchLibraryContainer> {
	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/library.png");

	protected float scrollOffs;
	protected boolean scrolling;
	protected int startIndex;

	protected List<LibrarySlot> data = new ArrayList<>();

	public EnchLibraryScreen(EnchLibraryContainer container, Inventory inv, Component title) {
		super(container, inv, title);
		this.width = this.imageWidth = 176;
		this.height = this.imageHeight = 241;
		this.titleLabelX = this.inventoryLabelX = 7;
		this.titleLabelY = 4;
		this.inventoryLabelY = 149;
		this.containerChanged();
		container.setNotifier(this::containerChanged);
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}

	@Override
	protected void renderTooltip(PoseStack stack, int mouseX, int mouseY) {
		super.renderTooltip(stack, mouseX, mouseY);
		LibrarySlot libSlot = this.getHoveredSlot(mouseX, mouseY);
		if (libSlot != null) {
			List<MutableComponent> list = new ArrayList<>();
			list.add(new TranslatableComponent(libSlot.ench.getDescriptionId()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF80))));
			list.add(new TextComponent(""));
			list.add(new TranslatableComponent("tooltip.enchlib.1", new TranslatableComponent("enchantment.level." + libSlot.maxLvl)));
			list.add(new TranslatableComponent("tooltip.enchlib.2", libSlot.points));
			ItemStack outSlot = this.menu.ioInv.getItem(1);
			int current = EnchantmentHelper.getEnchantments(outSlot).getOrDefault(libSlot.ench, 0);
			boolean shift = ClientUtil.isHoldingShift();
			if (!shift && (outSlot.isEmpty() || current == 0)) list.add(new TranslatableComponent("tooltip.enchlib.4", 1).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
			else if (!shift) {
				int cost = EnchLibraryTile.levelToPoints(current + 1) - EnchLibraryTile.levelToPoints(current);
				if (current + 1 > libSlot.maxLvl) list.add(new TranslatableComponent("tooltip.enchlib.5").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
				else list.add(new TranslatableComponent("tooltip.enchlib.4", cost).setStyle(Style.EMPTY.withColor(cost > libSlot.points ? ChatFormatting.RED : ChatFormatting.GOLD)));
			} else {
				int maxCost = EnchLibraryTile.levelToPoints(libSlot.maxLvl) - EnchLibraryTile.levelToPoints(current);
				if (current < libSlot.maxLvl) list.add(new TranslatableComponent("tooltip.enchlib.6", maxCost).setStyle(Style.EMPTY.withColor(maxCost > libSlot.points ? ChatFormatting.RED : ChatFormatting.GOLD)));
				else list.add(new TranslatableComponent("tooltip.enchlib.5").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
			}

			this.renderComponentTooltip(stack, list, this.getGuiLeft() - 16 - this.font.width(list.get(3)), mouseY, this.font);
		}
	}

	@Override
	protected void renderBg(PoseStack stack, float partial, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		//this.minecraft.getTextureManager().bind(TEXTURES);
		RenderSystem.setShaderTexture(0, TEXTURES);
		int left = this.leftPos;
		int top = this.topPos;
		this.blit(stack, left, top, 0, 0, this.imageWidth, this.imageHeight);
		int scrollbarPos = (int) (118F * this.scrollOffs);
		this.blit(stack, left + 75, top + 14 + scrollbarPos, 244, this.isScrollBarActive() ? 0 : 15, 12, 15);
		int idx = this.startIndex;
		while (idx < this.startIndex + 7 && idx < this.data.size()) {
			this.renderEntry(stack, this.data.get(idx), this.leftPos + 8, this.topPos + 14 + 19 * (idx - this.startIndex), mouseX, mouseY);
			idx++;
		}
	}

	private void renderEntry(PoseStack stack, LibrarySlot data, int x, int y, int mouseX, int mouseY) {
		//this.minecraft.getTextureManager().bind(TEXTURES);
		RenderSystem.setShaderTexture(0, TEXTURES);
		boolean hover = this.isHovering(x - this.leftPos, y - this.topPos, 64, 17, mouseX, mouseY);
		this.blit(stack, x, y, 178, hover ? 19 : 0, 64, 19);
		int progress = (int) Math.round(62 * Math.sqrt(data.points) / (float) Math.sqrt(32767));
		this.blit(stack, x + 1, y + 12, 179, 38, progress, 5);
		stack.pushPose();
		Component txt = new TranslatableComponent(data.ench.getDescriptionId());
		float scale = 1;
		if (this.font.width(txt) > 60) {
			scale = 60F / this.font.width(txt);
		}
		stack.scale(scale, scale, 1);
		this.font.draw(stack, txt, (x + 2) / scale, (y + 2) / scale, 0xFFFF80);
		stack.popPose();
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		this.scrolling = false;
		int left = this.leftPos + 52;
		int top = this.topPos + 14;

		LibrarySlot libSlot = this.getHoveredSlot((int) pMouseX, (int) pMouseY);
		if (libSlot != null) {
			int id = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(libSlot.ench);
			if (ClientUtil.isHoldingShift()) id |= 0x80000000;
			Placebo.CHANNEL.sendToServer(new ButtonClickMessage(id));
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
		}

		left = this.leftPos + 75;
		top = this.topPos + 9;
		if (pMouseX >= left && pMouseX < left + 12 && pMouseY >= top && pMouseY < top + 131) {
			this.scrolling = true;
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
		if (this.scrolling && this.isScrollBarActive()) {
			int i = this.topPos + 14;
			int j = i + 131;
			this.scrollOffs = ((float) pMouseY - i - 7.5F) / (j - i - 15.0F);
			this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
			this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5D);
			return true;
		} else {
			return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
		}
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (this.isScrollBarActive()) {
			int i = this.getOffscreenRows();
			this.scrollOffs = (float) (this.scrollOffs - pDelta / i);
			this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
			this.startIndex = (int) (this.scrollOffs * i + 0.5D);
		}
		return true;
	}

	private boolean isScrollBarActive() {
		return this.data.size() > 7;
	}

	protected int getOffscreenRows() {
		return this.data.size() - 7;
	}

	private void containerChanged() {
		this.data.clear();
		for (Entry<Enchantment> e : this.menu.getPointsForDisplay()) {
			this.data.add(new LibrarySlot(e.getKey(), e.getShortValue(), this.menu.tile.getLevelsMap().getByte(e.getKey())));
		}

		if (!this.isScrollBarActive()) {
			this.scrollOffs = 0.0F;
			this.startIndex = 0;
		}
		Collections.sort(data, (a, b) -> I18n.get(a.ench.getDescriptionId()).compareTo(I18n.get(b.ench.getDescriptionId())));
	}

	@Nullable
	public LibrarySlot getHoveredSlot(int mouseX, int mouseY) {
		for (int i = 0; i < 7; i++) {
			if (this.startIndex + i < this.data.size()) {
				if (this.isHovering(8, 14 + 19 * i, 64, 17, mouseX, mouseY)) return this.data.get(this.startIndex + i);
			}
		}
		return null;
	}

	private static class LibrarySlot {
		protected final Enchantment ench;
		protected final short points;
		protected final byte maxLvl;

		private LibrarySlot(Enchantment ench, short points, byte maxLvl) {
			this.ench = ench;
			this.points = points;
			this.maxLvl = maxLvl;
		}
	}

}
