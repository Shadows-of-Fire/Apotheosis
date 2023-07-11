package shadows.apotheosis.core.attributeslib.client;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.core.attributeslib.AttributesLib;
import shadows.apotheosis.core.attributeslib.api.IFormattableAttribute;
import shadows.placebo.PlaceboClient;

public class AttributesGui extends GuiComponent implements Widget, GuiEventListener {

	public static final ResourceLocation TEXTURES = Apotheosis.loc("textures/gui/attributes_gui.png");
	public static final int ENTRY_HEIGHT = 22;
	public static final int MAX_ENTRIES = 6;
	public static final int WIDTH = 131;

	// There's only one player, so we can just happily track if this menu was open via static field.
	// It isn't persistent through sessions, but that's not a huge issue.
	public static boolean wasOpen = false;
	// Similar to the above, we use a static field to record where the scroll bar was.
	protected static float scrollOffset = 0;
	// Ditto.
	protected static boolean hideUnchanged = false;

	protected final InventoryScreen parent;
	protected final Player player;
	protected final Font font = Minecraft.getInstance().font;
	protected final ImageButton toggleBtn;
	protected final ImageButton recipeBookButton;
	protected final HideUnchangedButton hideUnchangedBtn;

	protected int leftPos, topPos;
	protected boolean scrolling;
	protected int startIndex;
	protected List<AttributeInstance> data = new ArrayList<>();
	@Nullable
	protected AttributeInstance selected = null;
	protected boolean open = false;
	protected long lastRenderTick = -1;

	public AttributesGui(InventoryScreen parent) {
		this.parent = parent;
		this.player = Minecraft.getInstance().player;
		this.refreshData();
		this.leftPos = parent.getGuiLeft() - WIDTH;
		this.topPos = parent.getGuiTop();
		this.toggleBtn = new ImageButton(parent.getGuiLeft() + 63, parent.getGuiTop() + 10, 10, 10, WIDTH, 0, 10, TEXTURES, 256, 256, btn -> {
			this.toggleVisibility();
		}, Component.translatable("attributeslib.gui.show_attributes"));
		if (this.parent.children().size() > 1) {
			GuiEventListener btn = this.parent.children().get(0);
			this.recipeBookButton = btn instanceof ImageButton imgBtn ? imgBtn : null;
		} else this.recipeBookButton = null;
		this.hideUnchangedBtn = new HideUnchangedButton(0, 0);
	}

	public void refreshData() {
		this.data.clear();
		ForgeRegistries.ATTRIBUTES.getValues().stream().map(player::getAttribute).filter(Objects::nonNull).filter(ai -> {
			if (!hideUnchanged) return true;
			return ai.getBaseValue() != ai.getValue();
		}).forEach(data::add);
		this.data.sort(this::compareAttrs);
		this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
	}

	public void toggleVisibility() {
		this.open = !this.open;
		if (this.open && parent.getRecipeBookComponent().isVisible()) {
			parent.getRecipeBookComponent().toggleVisibility();
		}
		this.hideUnchangedBtn.visible = this.open;

		int newLeftPos;
		if (this.open && parent.width >= 379) {
			newLeftPos = 177 + (parent.width - parent.imageWidth - 200) / 2;
		} else {
			newLeftPos = (parent.width - parent.imageWidth) / 2;
		}

		parent.leftPos = newLeftPos;
		this.leftPos = parent.getGuiLeft() - WIDTH;
		this.topPos = parent.getGuiTop();

		if (this.recipeBookButton != null) this.recipeBookButton.setPosition(parent.getGuiLeft() + 104, parent.height / 2 - 22);
		this.hideUnchangedBtn.setPosition(this.leftPos + 7, this.topPos + 151);
	}

	protected int compareAttrs(AttributeInstance a1, AttributeInstance a2) {
		String name = I18n.get(a1.getAttribute().getDescriptionId());
		String name2 = I18n.get(a2.getAttribute().getDescriptionId());
		return name.compareTo(name2);
	}

	@Override
	public boolean isMouseOver(double pMouseX, double pMouseY) {
		if (!open) return false;
		return isHovering(0, 0, WIDTH, 166, pMouseX, pMouseY);
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		this.toggleBtn.x = this.parent.getGuiLeft() + 63;
		this.toggleBtn.y = this.parent.getGuiTop() + 10;
		if (this.parent.getRecipeBookComponent().isVisible()) this.open = false;
		wasOpen = this.open;
		if (!open) return;

		if (this.lastRenderTick != PlaceboClient.ticks) {
			this.lastRenderTick = PlaceboClient.ticks;
			this.refreshData();
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURES);
		int left = this.leftPos;
		int top = this.topPos;
		this.blit(stack, left, top, 0, 0, WIDTH, 166);
		int scrollbarPos = (int) (117 * scrollOffset);
		this.blit(stack, left + 111, top + 16 + scrollbarPos, 244, this.isScrollBarActive() ? 0 : 15, 12, 15);
		int idx = this.startIndex;
		while (idx < this.startIndex + MAX_ENTRIES && idx < this.data.size()) {
			this.renderEntry(stack, this.data.get(idx), this.leftPos + 8, this.topPos + 16 + ENTRY_HEIGHT * (idx - this.startIndex), mouseX, mouseY);
			idx++;
		}
		this.renderTooltip(stack, mouseX, mouseY);
		this.font.draw(stack, Component.translatable("attributeslib.gui.attributes"), this.leftPos + 8, this.topPos + 5, 0x404040);
		this.font.draw(stack, Component.literal("Hide Unchanged"), this.leftPos + 20, this.topPos + 152, 0x404040);
	}

	@SuppressWarnings("deprecation")
	protected void renderTooltip(PoseStack stack, int mouseX, int mouseY) {
		AttributeInstance inst = this.getHoveredSlot(mouseX, mouseY);
		if (inst != null) {
			Attribute attr = inst.getAttribute();
			IFormattableAttribute fAttr = (IFormattableAttribute) attr;
			List<Component> list = new ArrayList<>();
			MutableComponent name = Component.translatable(attr.getDescriptionId()).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withUnderlined(true));
			if (AttributesLib.getTooltipFlag().isAdvanced()) {
				Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withUnderlined(false);
				name.append(Component.literal(" [" + Registry.ATTRIBUTE.getKey(attr).toString() + "]").withStyle(style));
			}
			list.add(name);
			if (I18n.exists(Registry.ATTRIBUTE.getKey(attr) + ".desc")) {
				Component txt = Component.translatable(Registry.ATTRIBUTE.getKey(attr) + ".desc").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC);
				list.add(txt);
			} else if (AttributesLib.getTooltipFlag().isAdvanced()) {
				Component txt = Component.literal(Registry.ATTRIBUTE.getKey(attr) + ".desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
				list.add(txt);
			}

			list.add(CommonComponents.EMPTY);

			ChatFormatting color = ChatFormatting.GRAY;
			if (attr instanceof RangedAttribute ra) {
				if (inst.getValue() > inst.getBaseValue()) {
					color = ChatFormatting.YELLOW;
				} else if (inst.getValue() < inst.getBaseValue()) {
					color = ChatFormatting.RED;
				}
			}
			MutableComponent valueComp = fAttr.toValueComponent(Operation.ADDITION, inst.getValue(), AttributesLib.getTooltipFlag());
			list.add(Component.translatable("Current: %s", valueComp.withStyle(color)).withStyle(ChatFormatting.GRAY));

			MutableComponent baseVal = fAttr.toValueComponent(Operation.ADDITION, inst.getBaseValue(), AttributesLib.getTooltipFlag());

			baseVal = Component.translatable("attributeslib.gui.base", baseVal);
			if (attr instanceof RangedAttribute ra) {
				Component min = fAttr.toValueComponent(Operation.ADDITION, ra.getMinValue(), AttributesLib.getTooltipFlag());
				min = Component.translatable("attributeslib.gui.min", min);
				Component max = fAttr.toValueComponent(Operation.ADDITION, ra.getMaxValue(), AttributesLib.getTooltipFlag());
				max = Component.translatable("attributeslib.gui.max", max);
				list.add(Component.translatable("%s \u2507 %s \u2507 %s", baseVal, min, max).withStyle(ChatFormatting.GRAY));
			} else {
				list.add(baseVal.withStyle(ChatFormatting.GRAY));
			}

			List<ClientTooltipComponent> finalTooltip = new ArrayList<>(list.size());
			for (Component txt : list) {
				addComp(txt, finalTooltip);
			}

			if (!inst.getModifiers().isEmpty()) {
				addComp(CommonComponents.EMPTY, finalTooltip);
				addComp(Component.translatable("attributeslib.gui.modifiers").withStyle(ChatFormatting.GOLD), finalTooltip);

				Map<UUID, ModifierSource<?>> modifiersToSources = new HashMap<>();

				for (ModifierSourceType<?> type : ModifierSourceType.getTypes()) {
					type.extract(player, (modif, source) -> modifiersToSources.put(modif.getId(), source));
				}

				Component[] opValues = new Component[3];

				for (Operation op : Operation.values()) {
					List<AttributeModifier> modifiers = new ArrayList<>(inst.getModifiers(op));
					double opValue = modifiers.stream().mapToDouble(AttributeModifier::getAmount).reduce(op == Operation.MULTIPLY_TOTAL ? 1 : 0, (res, elem) -> op == Operation.MULTIPLY_TOTAL ? res * (1 + elem) : res + elem);

					modifiers.sort(ModifierSourceType.compareBySource(modifiersToSources));
					for (AttributeModifier modif : modifiers) {
						if (modif.getAmount() != 0) {
							Component comp = fAttr.toComponent(modif, AttributesLib.getTooltipFlag());
							var src = modifiersToSources.get(modif.getId());
							finalTooltip.add(new AttributeModifierComponent(src, comp, font, this.leftPos - 16));
						}
					}
					color = ChatFormatting.GRAY;
					double threshold = op == Operation.MULTIPLY_TOTAL ? 1.0005 : 0.0005;

					if (opValue > threshold) {
						color = ChatFormatting.YELLOW;
					} else if (opValue < -threshold) {
						color = ChatFormatting.RED;
					}
					Component valueComp2 = fAttr.toValueComponent(op, opValue, AttributesLib.getTooltipFlag()).withStyle(color);
					Component comp = Component.translatable("attributeslib.gui." + op.name().toLowerCase(Locale.ROOT), valueComp2).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
					opValues[op.ordinal()] = comp;
				}

				if (AttributesLib.getTooltipFlag().isAdvanced()) {
					addComp(CommonComponents.EMPTY, finalTooltip);
					for (Component comp : opValues) {
						addComp(comp, finalTooltip);
					}
				}
			}

			parent.renderTooltip(stack, List.of(), 0, 0, font); // This no-op call sets Screen#tooltipFont, which is used in renderTooltipInternal
			parent.renderTooltipInternal(stack, finalTooltip, this.leftPos - 16 - finalTooltip.stream().map(c -> c.getWidth(font)).max(Integer::compare).get(), mouseY);
		}
	}

	private void addComp(Component comp, List<ClientTooltipComponent> finalTooltip) {
		if (comp == CommonComponents.EMPTY) {
			finalTooltip.add(ClientTooltipComponent.create(comp.getVisualOrderText()));
		} else {
			for (FormattedText fTxt : this.font.getSplitter().splitLines(comp, this.leftPos - 16, comp.getStyle())) {
				finalTooltip.add(ClientTooltipComponent.create(Language.getInstance().getVisualOrder(fTxt)));
			}
		}
	}

	private void renderEntry(PoseStack stack, AttributeInstance inst, int x, int y, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURES);
		boolean hover = this.getHoveredSlot(mouseX, mouseY) == inst;
		this.blit(stack, x, y, 142, hover ? ENTRY_HEIGHT : 0, 100, ENTRY_HEIGHT);

		Component txt = Component.translatable(inst.getAttribute().getDescriptionId());
		int splitWidth = 60;
		List<FormattedCharSequence> lines = this.font.split(txt, splitWidth);
		// We can only actually display two lines here, but we need to forcibly create two lines and then scale down.
		while (lines.size() > 2) {
			splitWidth += 10;
			lines = this.font.split(txt, splitWidth);
		}

		stack.pushPose();
		float scale = 1;
		int maxWidth = lines.stream().map(this.font::width).max(Integer::compareTo).get();
		if (maxWidth > 66) {
			scale = 66F / maxWidth;
			stack.scale(scale, scale, 1);
		}

		for (int i = 0; i < lines.size(); i++) {
			var line = lines.get(i);
			float width = this.font.width(line) * scale;
			float lineX = (x + 1 + (68 - width) / 2) / scale;
			float lineY = (y + (lines.size() == 1 ? 7 : 2) + i * 10) / scale;
			this.font.draw(stack, line, lineX, lineY, 0x404040);
		}
		stack.popPose();
		stack.pushPose();

		var attr = (IFormattableAttribute) inst.getAttribute();
		MutableComponent value = attr.toValueComponent(Operation.ADDITION, inst.getValue(), TooltipFlag.Default.NORMAL);

		scale = 1;
		if (this.font.width(value) > 27) {
			scale = 27F / this.font.width(value);
			stack.scale(scale, scale, 1);
		}

		int color = 0xFFFFFF;
		if (attr instanceof RangedAttribute ra) {
			if (inst.getValue() > inst.getBaseValue()) {
				color = 0x55DD55;
			} else if (inst.getValue() < inst.getBaseValue()) {
				color = 0xFF6060;
			}
		}
		this.font.drawShadow(stack, value, (x + 72 + (27 - this.font.width(value) * scale) / 2) / scale, (y + 7) / scale, color);
		stack.popPose();
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (!open || !this.isScrollBarActive()) return false;
		this.scrolling = false;
		int left = this.leftPos + 111;
		int top = this.topPos + 15;
		if (pMouseX >= left && pMouseX < left + 12 && pMouseY >= top && pMouseY < top + 155) {
			this.scrolling = true;
			int i = this.topPos + 15;
			int j = i + 138;
			scrollOffset = ((float) pMouseY - i - 7.5F) / (j - i - 15.0F);
			scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
			this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
		if (!open) return false;
		if (this.scrolling && this.isScrollBarActive()) {
			int i = this.topPos + 15;
			int j = i + 138;
			scrollOffset = ((float) pMouseY - i - 7.5F) / (j - i - 15.0F);
			scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
			this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (!open) return false;
		if (this.isScrollBarActive()) {
			int i = this.getOffScreenRows();
			scrollOffset = (float) (scrollOffset - pDelta / i);
			scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
			this.startIndex = (int) (scrollOffset * i + 0.5D);
			return true;
		}
		return false;
	}

	@Override
	public boolean changeFocus(boolean pFocus) {
		return true;
	}

	private boolean isScrollBarActive() {
		return this.data.size() > MAX_ENTRIES;
	}

	protected int getOffScreenRows() {
		return Math.max(0, this.data.size() - MAX_ENTRIES);
	}

	@Nullable
	public AttributeInstance getHoveredSlot(int mouseX, int mouseY) {
		for (int i = 0; i < MAX_ENTRIES; i++) {
			if (this.startIndex + i < this.data.size()) {
				if (this.isHovering(8, 14 + ENTRY_HEIGHT * i, 100, ENTRY_HEIGHT, mouseX, mouseY)) return this.data.get(this.startIndex + i);
			}
		}
		return null;
	}

	protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
		int i = this.leftPos;
		int j = this.topPos;
		pMouseX -= (double) i;
		pMouseY -= (double) j;
		return pMouseX >= (double) (pX - 1) && pMouseX < (double) (pX + pWidth + 1) && pMouseY >= (double) (pY - 1) && pMouseY < (double) (pY + pHeight + 1);
	}

	private static DecimalFormat f = ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

	public static String format(int n) {
		int log = (int) StrictMath.log10(n);
		if (log <= 4) return String.valueOf(n);
		if (log == 5) return f.format(n / 1000D) + "K";
		if (log <= 8) return f.format(n / 1000000D) + "M";
		else return f.format(n / 1000000000D) + "B";
	}

	public class HideUnchangedButton extends ImageButton {

		public HideUnchangedButton(int pX, int pY) {
			super(pX, pY, 10, 10, 131, 20, 10, TEXTURES, 256, 256, null, Component.literal("Hide Unchanged Attributes"));
			this.visible = false;
		}

		@Override
		public void onPress() {
			hideUnchanged = !hideUnchanged;
		}

		@Override
		public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, TEXTURES);
			int u = 131, v = 20;
			int vOffset = hideUnchanged ? 0 : 10;
			if (this.isHovered) {
				vOffset += 20;
			}

			RenderSystem.enableDepthTest();
			pPoseStack.pushPose();
			pPoseStack.translate(0, 0, 100);
			blit(pPoseStack, this.x, this.y, u, v + vOffset, 10, 10, 256, 256);
			pPoseStack.popPose();
		}

	}

}
