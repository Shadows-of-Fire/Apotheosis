package shadows.apotheosis.ench.library;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.objects.Object2ShortMap.Entry;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import shadows.apotheosis.Apotheosis;
import shadows.placebo.Placebo;
import shadows.placebo.net.MessageButtonClick;
import shadows.placebo.util.ClientUtil;

public class EnchLibraryScreen extends ContainerScreen<EnchLibraryContainer> {
	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/library.png");

	protected float scrollOffs;
	protected boolean scrolling;
	protected int startIndex;

	protected List<LibrarySlot> data = new ArrayList<>();

	public EnchLibraryScreen(EnchLibraryContainer container, PlayerInventory inv, ITextComponent title) {
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
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}

	@Override
	protected void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
		super.renderTooltip(stack, mouseX, mouseY);
		LibrarySlot libSlot = this.getHoveredSlot(mouseX, mouseY);
		if (libSlot != null) {
			List<IFormattableTextComponent> list = new ArrayList<>();
			list.add(new TranslationTextComponent(libSlot.ench.getDescriptionId()).setStyle(Style.EMPTY.withColor(Color.fromRgb(0xFFFF80))));
			list.add(new StringTextComponent(""));
			list.add(new TranslationTextComponent("tooltip.enchlib.1", new TranslationTextComponent("enchantment.level." + libSlot.maxLvl)));
			list.add(new TranslationTextComponent("tooltip.enchlib.2", libSlot.points));
			ItemStack outSlot = this.menu.ioInv.getItem(1);
			int current = EnchantmentHelper.getEnchantments(outSlot).getOrDefault(libSlot.ench, 0);
			boolean shift = ClientUtil.isHoldingShift();
			if (!shift && (outSlot.isEmpty() || current == 0)) list.add(new TranslationTextComponent("tooltip.enchlib.4", 1).setStyle(Style.EMPTY.withColor(TextFormatting.GOLD)));
			else if (!shift) {
				int cost = EnchLibraryTile.levelToPoints(current + 1) - EnchLibraryTile.levelToPoints(current);
				if (current + 1 > libSlot.maxLvl) list.add(new TranslationTextComponent("tooltip.enchlib.5").setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
				else list.add(new TranslationTextComponent("tooltip.enchlib.4", cost).setStyle(Style.EMPTY.withColor(cost > libSlot.points ? TextFormatting.RED : TextFormatting.GOLD)));
			} else {
				int maxCost = EnchLibraryTile.levelToPoints(libSlot.maxLvl) - EnchLibraryTile.levelToPoints(current);
				if (current < libSlot.maxLvl) list.add(new TranslationTextComponent("tooltip.enchlib.6", maxCost).setStyle(Style.EMPTY.withColor(maxCost > libSlot.points ? TextFormatting.RED : TextFormatting.GOLD)));
				else list.add(new TranslationTextComponent("tooltip.enchlib.5").setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
			}

			this.renderWrappedToolTip(stack, list, mouseX, mouseY, this.font);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void renderBg(MatrixStack stack, float partial, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(TEXTURES);
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

	private void renderEntry(MatrixStack stack, LibrarySlot data, int x, int y, int mouseX, int mouseY) {
		this.minecraft.getTextureManager().bind(TEXTURES);
		boolean hover = this.isHovering(x - this.leftPos, y - this.topPos, 64, 17, mouseX, mouseY);
		this.blit(stack, x, y, 178, hover ? 19 : 0, 64, 19);
		int progress = (int) Math.round(62 * Math.sqrt(data.points) / (float) Math.sqrt(32767));
		this.blit(stack, x + 1, y + 12, 179, 38, progress, 5);
		stack.pushPose();
		ITextComponent txt = new TranslationTextComponent(data.ench.getDescriptionId());
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
			Placebo.CHANNEL.sendToServer(new MessageButtonClick(id));
			this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
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
			this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0F, 1.0F);
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
			this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0F, 1.0F);
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
