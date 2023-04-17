package shadows.apotheosis.ench.library;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
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

public class EnchLibraryScreen extends AbstractContainerScreen<EnchLibraryContainer> {
	public static final ResourceLocation TEXTURES = new ResourceLocation(Apotheosis.MODID, "textures/gui/library.png");

	protected float scrollOffs;
	protected boolean scrolling;
	protected int startIndex;

	protected List<LibrarySlot> data = new ArrayList<>();
	protected EditBox filter = null;

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
	protected void init() {
		super.init();
		this.filter = this.addRenderableWidget(new EditBox(this.font, this.getGuiLeft() + 91, this.getGuiTop() + 20 + this.font.lineHeight + 2, 78, this.font.lineHeight + 4, this.filter, Component.literal("")));
		this.filter.setResponder(t -> this.containerChanged());
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
		if (pKeyCode == GLFW.GLFW_KEY_ESCAPE && this.getFocused() == this.filter) {
			this.setFocused(null);
			this.filter.setFocus(false);
			return true;
		} else if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.getFocused() == this.filter) {
			return true;
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
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
			List<FormattedText> list = new ArrayList<>();
			list.add(Component.translatable(libSlot.ench.getDescriptionId()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF80)).withUnderlined(true)));
			if (I18n.exists(libSlot.ench.getDescriptionId() + ".desc")) {
				Component txt = Component.translatable(libSlot.ench.getDescriptionId() + ".desc").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
				list.addAll(this.font.getSplitter().splitLines(txt, this.getGuiLeft() - 16, txt.getStyle()));
				list.add(Component.literal(""));
			}

			list.add(Component.translatable("tooltip.enchlib.max_lvl", Component.translatable("enchantment.level." + libSlot.maxLvl)).withStyle(ChatFormatting.GRAY));
			list.add(Component.translatable("tooltip.enchlib.points", format(libSlot.points), format(this.menu.getPointCap())).withStyle(ChatFormatting.GRAY));
			list.add(Component.literal(""));
			ItemStack outSlot = this.menu.ioInv.getItem(1);
			int current = EnchantmentHelper.getEnchantments(outSlot).getOrDefault(libSlot.ench, 0);
			boolean shift = Screen.hasShiftDown();
			int targetLevel = shift ? Math.min(libSlot.maxLvl, 1 + (int) (Math.log(libSlot.points + EnchLibraryTile.levelToPoints(current)) / Math.log(2))) : current + 1;
			if (targetLevel == current) targetLevel++;
			int cost = EnchLibraryTile.levelToPoints(targetLevel) - EnchLibraryTile.levelToPoints(current);
			if (targetLevel > libSlot.maxLvl) list.add(Component.translatable("tooltip.enchlib.unavailable").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
			else {
				list.add(Component.translatable("tooltip.enchlib.extracting", Component.translatable("enchantment.level." + targetLevel)).withStyle(ChatFormatting.BLUE));
				list.add(Component.translatable("tooltip.enchlib.cost", cost).withStyle(cost > libSlot.points ? ChatFormatting.RED : ChatFormatting.GOLD));
			}
			this.renderComponentTooltip(stack, list, this.getGuiLeft() - 16 - list.stream().map(this.font::width).max(Integer::compare).get(), mouseY, this.font);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void renderBg(PoseStack stack, float partial, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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

		this.font.draw(stack, Component.translatable("tooltip.enchlib.nfilt"), this.getGuiLeft() + 91, this.getGuiTop() + 20, 4210752);
		this.font.draw(stack, Component.translatable("tooltip.enchlib.ifilt"), this.getGuiLeft() + 91, this.getGuiTop() + 50, 4210752);
	}

	private void renderEntry(PoseStack stack, LibrarySlot data, int x, int y, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURES);
		boolean hover = this.isHovering(x - this.leftPos, y - this.topPos, 64, 17, mouseX, mouseY);
		this.blit(stack, x, y, 178, hover ? 19 : 0, 64, 19);
		int progress = (int) Math.round(62 * Math.sqrt(data.points) / (float) Math.sqrt(this.menu.getPointCap()));
		this.blit(stack, x + 1, y + 12, 179, 38, progress, 5);
		stack.pushPose();
		Component txt = Component.translatable(data.ench.getDescriptionId());
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
			if (Screen.hasShiftDown()) id |= 0x80000000;
			this.menu.onButtonClick(id);
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
		List<Entry<Enchantment>> entries = this.filter(this.menu.getPointsForDisplay());
		for (Entry<Enchantment> e : entries) {
			this.data.add(new LibrarySlot(e.getKey(), e.getIntValue(), this.menu.getMaxLevel(e.getKey())));
		}

		if (!this.isScrollBarActive()) {
			this.scrollOffs = 0.0F;
			this.startIndex = 0;
		}
		Collections.sort(this.data, (a, b) -> I18n.get(a.ench.getDescriptionId()).compareTo(I18n.get(b.ench.getDescriptionId())));
	}

	private List<Entry<Enchantment>> filter(List<Entry<Enchantment>> list) {
		return list.stream().filter(this::isAllowedByItem).filter(this::isAllowedBySearch).toList();
	}

	private boolean isAllowedByItem(Entry<Enchantment> e) {
		ItemStack stack = this.menu.ioInv.getItem(2);
		return stack.isEmpty() || e.getKey().canEnchant(stack);
	}

	private boolean isAllowedBySearch(Entry<Enchantment> e) {
		String name = I18n.get(e.getKey().getDescriptionId()).toLowerCase(Locale.ROOT);
		String search = this.filter == null ? "" : this.filter.getValue().trim().toLowerCase(Locale.ROOT);
		return Strings.isNullOrEmpty(search) || ChatFormatting.stripFormatting(name).contains(search);
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
		protected final int points;
		protected final int maxLvl;

		private LibrarySlot(Enchantment ench, int points, int maxLvl) {
			this.ench = ench;
			this.points = points;
			this.maxLvl = maxLvl;
		}
	}

	private static DecimalFormat f = new DecimalFormat("##.#");

	public static String format(int n) {
		int log = (int) StrictMath.log10(n);
		if (log <= 4) return String.valueOf(n);
		if (log == 5) return f.format(n / 1000D) + "K";
		if (log <= 8) return f.format(n / 1000000D) + "M";
		else return f.format(n / 1000000000D) + "B";
	}

}
