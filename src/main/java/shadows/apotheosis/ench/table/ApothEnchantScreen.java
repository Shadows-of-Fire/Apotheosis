package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.table.ApothEnchantContainer.Arcana;
import shadows.placebo.util.EnchantmentUtils;

public class ApothEnchantScreen extends AbstractContainerScreen<ApothEnchantContainer> {

	private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/enchanting_table.png");
	private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
	private BookModel bookModel;
	private final Random random = new Random();
	public int ticks;
	public float flip;
	public float oFlip;
	public float flipT;
	public float flipA;
	public float open;
	public float oOpen;
	private ItemStack last = ItemStack.EMPTY;
	protected float eterna = 0, lastEterna = 0, quanta = 0, lastQuanta = 0, arcana = 0, lastArcana = 0;
	protected final Int2ObjectMap<List<EnchantmentInstance>> clues = new Int2ObjectOpenHashMap<>();
	protected boolean[] hasAllClues = { false, false, false };

	public ApothEnchantScreen(ApothEnchantContainer container, Inventory inv, Component title) {
		super(container, inv, title);
		this.imageHeight = 197;
		this.clues.defaultReturnValue(new ArrayList<>());
	}

	@Override
	protected void init() {
		super.init();
		this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
	}

	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
		this.font.draw(stack, this.title, 12.0F, 5.0F, 4210752);
		this.font.draw(stack, this.playerInventoryTitle, 7.0F, this.imageHeight - 96 + 4F, 4210752);
		this.font.draw(stack, I18n.get("gui.apotheosis.enchant.eterna"), 19, 74, 0x3DB53D);
		this.font.draw(stack, I18n.get("gui.apotheosis.enchant.quanta"), 19, 84, 0xFC5454);
		this.font.draw(stack, I18n.get("gui.apotheosis.enchant.arcana"), 19, 94, 0xA800A8);
	}

	@Override
	public void containerTick() {
		this.tickBook();
		float current = this.menu.eterna.get();
		if (current != this.eterna) {
			if (current > this.eterna) this.eterna += Math.min(current - this.eterna, Math.max(0.16F, (current - this.eterna) * 0.1F));
			else this.eterna = Math.max(this.eterna - this.lastEterna * 0.075F, current);
		}
		if (current > 0) this.lastEterna = current;

		current = this.menu.quanta.get();
		if (current != this.quanta) {
			if (current > this.quanta) this.quanta += Math.min(current - this.quanta, Math.max(0.04F, (current - this.quanta) * 0.1F));
			else this.quanta = Math.max(this.quanta - this.lastQuanta * 0.075F, current);
		}
		if (current > 0) this.lastQuanta = current;

		current = this.menu.arcana.get();
		if (current != this.arcana) {
			if (current > this.arcana) this.arcana += Math.min(current - this.arcana, Math.max(0.04F, (current - this.arcana) * 0.1F));
			else this.arcana = Math.max(this.arcana - this.lastArcana * 0.075F, current);
		}
		if (current > 0) this.lastArcana = current;
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;

		for (int k = 0; k < 3; ++k) {
			double d0 = pMouseX - (i + 60);
			double d1 = pMouseY - (j + 14 + 19 * k);
			if (d0 >= 0.0D && d1 >= 0.0D && d0 < 108.0D && d1 < 19.0D && this.menu.clickMenuButton(this.minecraft.player, k)) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, k);
				return true;
			}
		}

		if (this.menu.getSlot(0).hasItem() && this.isHovering(145, -15, 27, 15, pMouseX, pMouseY) && Arrays.stream(this.menu.enchantClue).boxed().map(Enchantment::byId).allMatch(Predicates.notNull())) {
			Minecraft.getInstance().pushGuiLayer(new EnchantingInfoScreen(this));
		}

		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
		Lighting.setupForFlatItems();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, ENCHANTMENT_TABLE_GUI_TEXTURE);
		int xCenter = (this.width - this.imageWidth) / 2;
		int yCenter = (this.height - this.imageHeight) / 2;
		this.blit(stack, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight);
		int guiScale = (int) this.minecraft.getWindow().getGuiScale();
		RenderSystem.viewport((this.width - 320) / 2 * guiScale, (this.height - 240) / 2 * guiScale, 320 * guiScale, 240 * guiScale);
		Matrix4f matrix4f = Matrix4f.createTranslateMatrix(-0.34F, 0.23F, 0.0F);
		matrix4f.multiply(Matrix4f.perspective(90.0D, 1.3333334F, 9.0F, 80.0F));
		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(matrix4f);
		stack.pushPose();
		PoseStack.Pose posestack$pose = stack.last();
		posestack$pose.pose().setIdentity();
		posestack$pose.normal().setIdentity();
		stack.translate(0.0D, 5.3F, 1984.0D);
		stack.scale(5.0F, 5.0F, 5.0F);
		stack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
		stack.mulPose(Vector3f.XP.rotationDegrees(20.0F));
		float f1 = Mth.lerp(partialTicks, this.oOpen, this.open);

		stack.translate((1.0F - f1) * 0.2F, (1.0F - f1) * 0.1F, (1.0F - f1) * 0.25F);
		float f2 = -(1.0F - f1) * 90.0F - 90.0F;
		stack.mulPose(Vector3f.YP.rotationDegrees(f2));
		stack.mulPose(Vector3f.XP.rotationDegrees(180.0F));

		float f3 = Mth.lerp(partialTicks, this.oFlip, this.flip) + 0.25F;
		float f4 = Mth.lerp(partialTicks, this.oFlip, this.flip) + 0.75F;
		f3 = (f3 - Mth.fastFloor(f3)) * 1.6F - 0.3F;
		f4 = (f4 - Mth.fastFloor(f4)) * 1.6F - 0.3F;
		if (f3 < 0.0F) {
			f3 = 0.0F;
		}

		if (f4 < 0.0F) {
			f4 = 0.0F;
		}

		if (f3 > 1.0F) {
			f3 = 1.0F;
		}

		if (f4 > 1.0F) {
			f4 = 1.0F;
		}

		this.bookModel.setupAnim(0.0F, f3, f4, f1);
		MultiBufferSource.BufferSource buf = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		VertexConsumer vertexconsumer = buf.getBuffer(this.bookModel.renderType(ENCHANTMENT_TABLE_BOOK_TEXTURE));
		this.bookModel.renderToBuffer(stack, vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		buf.endBatch();
		stack.popPose();
		RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
		RenderSystem.restoreProjectionMatrix();
		Lighting.setupFor3DItems();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		EnchantmentNames.getInstance().initSeed(this.menu.getEnchantmentSeed());
		int lapis = this.menu.getGoldCount();

		for (int slot = 0; slot < 3; ++slot) {
			int j1 = xCenter + 60;
			int k1 = j1 + 20;
			this.setBlitOffset(0);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, ENCHANTMENT_TABLE_GUI_TEXTURE);
			int level = this.menu.costs[slot];
			if (level == 0) {
				this.blit(stack, j1, yCenter + 14 + 19 * slot, 148, 218, 108, 19);
			} else {
				String s = "" + level;
				int width = 86 - this.font.width(s);
				FormattedText itextproperties = EnchantmentNames.getInstance().getRandomName(this.font, width);
				int color = 6839882;
				if ((lapis < slot + 1 || this.minecraft.player.experienceLevel < level) && !this.minecraft.player.getAbilities().instabuild || this.menu.enchantClue[slot] == -1) { // Forge: render buttons as disabled when enchantable but enchantability not met on lower levels
					this.blit(stack, j1, yCenter + 14 + 19 * slot, 148, 218, 108, 19);
					this.blit(stack, j1 + 1, yCenter + 15 + 19 * slot, 16 * slot, 239, 16, 16);
					this.font.drawWordWrap(itextproperties, k1, yCenter + 16 + 19 * slot, width, (color & 16711422) >> 1);
					color = 4226832;
				} else {
					int k2 = mouseX - (xCenter + 60);
					int l2 = mouseY - (yCenter + 14 + 19 * slot);
					if (k2 >= 0 && l2 >= 0 && k2 < 108 && l2 < 19) {
						this.blit(stack, j1, yCenter + 14 + 19 * slot, 148, 237, 108, 19);
						color = 16777088;
					} else {
						this.blit(stack, j1, yCenter + 14 + 19 * slot, 148, 199, 108, 19);
					}

					this.blit(stack, j1 + 1, yCenter + 15 + 19 * slot, 16 * slot, 223, 16, 16);
					this.font.drawWordWrap(itextproperties, k1, yCenter + 16 + 19 * slot, width, color);
					color = 8453920;
				}

				this.font.drawShadow(stack, s, k1 + 86 - this.font.width(s), yCenter + 16 + 19 * slot + 7, color);
			}
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, ENCHANTMENT_TABLE_GUI_TEXTURE);
		if (this.eterna > 0) {
			this.blit(stack, xCenter + 59, yCenter + 75, 0, 197, (int) (this.eterna / this.menu.eterna.getMax() * 110), 5);
		}
		if (this.quanta > 0) {
			this.blit(stack, xCenter + 59, yCenter + 85, 0, 202, (int) (this.quanta / 100 * 110), 5);
		}
		if (this.arcana > 0) {
			this.blit(stack, xCenter + 59, yCenter + 95, 0, 207, (int) (this.arcana / 100 * 110), 5);
		}

		if (this.menu.getSlot(0).hasItem() && Arrays.stream(this.menu.enchantClue).boxed().map(Enchantment::byId).allMatch(Predicates.notNull())) {
			int u = this.isHovering(145, -15, 27, 15, mouseX, mouseY) ? 15 : 0;
			this.blit(stack, xCenter + 145, yCenter - 15, this.imageWidth, u, 27, 15);
		}
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		partialTicks = this.minecraft.getFrameTime();
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
		boolean creative = this.minecraft.player.getAbilities().instabuild;
		int lapis = this.menu.getGoldCount();

		for (int slot = 0; slot < 3; ++slot) {
			int level = this.menu.costs[slot];
			Enchantment enchantment = Enchantment.byId(this.menu.enchantClue[slot]);
			int cost = slot + 1;
			if (this.isHovering(60, 14 + 19 * slot, 108, 17, mouseX, mouseY) && level > 0) {
				List<Component> list = Lists.newArrayList();
				if (enchantment != null) {
					if (!this.clues.get(slot).isEmpty()) {
						list.add(Component.translatable("info.apotheosis.runes" + (this.hasAllClues[slot] ? "_all" : "")).withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
						for (EnchantmentInstance i : this.clues.get(slot)) {
							list.add(((MutableComponent) i.enchantment.getFullname(i.level)));
						}
					} else {
						list.add(Component.translatable("info.apotheosis.no_clue").withStyle(ChatFormatting.DARK_RED, ChatFormatting.UNDERLINE));
					}
				} else list.add(Component.translatable("container.enchant.clue", "").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
				if (enchantment == null) {
					Collections.addAll(list, Component.literal(""), Component.translatable("forge.container.enchant.limitedEnchantability").withStyle(ChatFormatting.RED));
				} else if (!creative) {
					list.add(Component.literal(""));
					if (this.minecraft.player.experienceLevel < level) {
						list.add(Component.translatable("container.enchant.level.requirement", this.menu.costs[slot]).withStyle(ChatFormatting.RED));
					} else {
						String s;
						if (cost == 1) {
							s = I18n.get("container.enchant.lapis.one");
						} else {
							s = I18n.get("container.enchant.lapis.many", cost);
						}

						ChatFormatting textformatting = lapis >= cost ? ChatFormatting.GRAY : ChatFormatting.RED;
						list.add(Component.literal(s).withStyle(textformatting));
						if (cost == 1) {
							s = I18n.get("container.enchant.level.one");
						} else {
							s = I18n.get("container.enchant.level.many", cost);
						}

						list.add(Component.literal(s).withStyle(ChatFormatting.GRAY));
					}
				}
				this.renderComponentTooltip(stack, list, mouseX, mouseY);
				break;
			}
		}

		if (this.isHovering(60, 14 + 19 * 3 + 5, 110, 5, mouseX, mouseY)) {
			List<Component> list = Lists.newArrayList();
			list.add(Component.literal(eterna() + I18n.get("gui.apotheosis.enchant.eterna.desc")));
			list.add(Component.translatable("gui.apotheosis.enchant.eterna.desc2").withStyle(ChatFormatting.GRAY));
			if (this.menu.eterna.get() > 0) {
				list.add(Component.literal(""));
				list.add(Component.literal(I18n.get("gui.apotheosis.enchant.eterna.desc3", f(this.menu.eterna.get()), this.menu.eterna.getMax())).withStyle(ChatFormatting.GRAY));
			}
			this.renderComponentTooltip(stack, list, mouseX, mouseY);
		} else if (this.isHovering(60, 14 + 19 * 3 + 15, 110, 5, mouseX, mouseY)) {
			List<Component> list = Lists.newArrayList();
			list.add(Component.literal(quanta() + I18n.get("gui.apotheosis.enchant.quanta.desc")));
			list.add(Component.translatable("gui.apotheosis.enchant.quanta.desc2").withStyle(ChatFormatting.GRAY));
			list.add(Component.literal(rectification() + I18n.get("gui.apotheosis.enchant.quanta.desc3")).withStyle(ChatFormatting.GRAY));
			if (this.menu.quanta.get() > 0) {
				list.add(Component.literal(""));
				list.add(Component.literal(I18n.get("gui.apotheosis.enchant.quanta.desc4", f(this.menu.quanta.get()))).withStyle(ChatFormatting.GRAY));
				list.add(Component.literal(I18n.get("info.apotheosis.gui_rectification", f(this.menu.rectification.get()))).withStyle(ChatFormatting.YELLOW));
			}
			this.renderComponentTooltip(stack, list, mouseX, mouseY);
			float quanta = this.menu.quanta.get();
			float rectification = this.menu.rectification.get();
			if (quanta > 0) {
				list.clear();
				list.add(Component.translatable("info.apotheosis.quanta_buff").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.RED));
				list.add(Component.translatable("info.apotheosis.quanta_reduc", f(-quanta + quanta * rectification / 100F)).withStyle(ChatFormatting.DARK_RED));
				list.add(Component.translatable("info.apotheosis.quanta_growth", f(quanta)).withStyle(ChatFormatting.BLUE));
				this.drawOnLeft(stack, list, this.getGuiTop() + 29);
			}
		} else if (this.isHovering(60, 14 + 19 * 3 + 25, 110, 5, mouseX, mouseY)) {
			List<Component> list = Lists.newArrayList();
			stack.pushPose();
			stack.translate(0, 0, 4);
			list.add(Component.literal(arcana() + I18n.get("gui.apotheosis.enchant.arcana.desc")));
			list.add(Component.translatable("gui.apotheosis.enchant.arcana.desc2").withStyle(ChatFormatting.GRAY));
			list.add(Component.translatable("gui.apotheosis.enchant.arcana.desc3").withStyle(ChatFormatting.GRAY));
			if (this.menu.arcana.get() > 0) {
				list.add(Component.literal(""));
				float ench = this.menu.getSlot(0).getItem().getEnchantmentValue() / 2F;
				list.add(Component.literal(I18n.get("gui.apotheosis.enchant.arcana.desc4", f(this.menu.arcana.get() - ench))).withStyle(ChatFormatting.GRAY));
				list.add(Component.translatable("info.apotheosis.ench_bonus", f(ench)).withStyle(ChatFormatting.YELLOW));
				list.add(Component.literal(I18n.get("gui.apotheosis.enchant.arcana.desc5", f(this.menu.arcana.get()))).withStyle(ChatFormatting.GOLD));
			}
			this.renderComponentTooltip(stack, list, mouseX, mouseY);
			stack.popPose();
			if (this.menu.arcana.get() > 0) {
				list.clear();
				Arcana a = Arcana.getForThreshold(this.menu.arcana.get());
				list.add(Component.translatable("info.apotheosis.arcana_bonus").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.DARK_PURPLE));
				if (a != Arcana.EMPTY) list.add(Component.translatable("info.apotheosis.weights_changed").withStyle(ChatFormatting.BLUE));
				int minEnchants = this.menu.arcana.get() > 75F ? 3 : this.menu.arcana.get() > 25F ? 2 : 0;
				if (minEnchants > 0) list.add(Component.translatable("info.apotheosis.min_enchants", minEnchants).withStyle(ChatFormatting.BLUE));

				this.drawOnLeft(stack, list, this.getGuiTop() + 29);
				int offset = 20 + list.size() * this.minecraft.font.lineHeight;
				list.clear();
				list.add(Component.translatable("info.apotheosis.rel_weights").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.YELLOW));
				list.add(Component.translatable("info.apotheosis.weight", I18n.get("rarity.enchantment.common"), a.rarities[0]).withStyle(ChatFormatting.GRAY));
				list.add(Component.translatable("info.apotheosis.weight", I18n.get("rarity.enchantment.uncommon"), a.rarities[1]).withStyle(ChatFormatting.GREEN));
				list.add(Component.translatable("info.apotheosis.weight", I18n.get("rarity.enchantment.rare"), a.rarities[2]).withStyle(ChatFormatting.BLUE));
				list.add(Component.translatable("info.apotheosis.weight", I18n.get("rarity.enchantment.very_rare"), a.rarities[3]).withStyle(ChatFormatting.GOLD));
				this.drawOnLeft(stack, list, this.getGuiTop() + 29 + offset);
			}
		} else if (this.menu.getSlot(0).hasItem() && this.isHovering(145, -15, 27, 15, mouseX, mouseY) && Arrays.stream(this.menu.enchantClue).boxed().map(Enchantment::byId).allMatch(Predicates.notNull())) {
			List<Component> list = Lists.newArrayList();
			list.add(Component.translatable("info.apotheosis.all_available").withStyle(ChatFormatting.BLUE));
			this.renderComponentTooltip(stack, list, mouseX, mouseY);
		}

		ItemStack enchanting = this.menu.getSlot(0).getItem();
		if (!enchanting.isEmpty() && this.menu.costs[2] > 0) {
			for (int j = 0; j < 3; j++) {
				if (this.isHovering(60, 14 + 19 * j, 108, 17, mouseX, mouseY)) {
					List<Component> list = new ArrayList<>();
					int level = this.menu.costs[j];
					list.add(Component.literal(I18n.get("info.apotheosis.ench_at", level)).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GREEN));
					list.add(Component.literal(""));
					int cost = 0;
					for (int i = 0; i <= j; i++) {
						cost += EnchantmentUtils.getExperienceForLevel(level - i) - EnchantmentUtils.getExperienceForLevel(level - i - 1);
					}
					list.add(Component.translatable("info.apotheosis.xp_cost", Component.literal("" + cost).withStyle(ChatFormatting.GREEN), Component.literal("" + EnchantmentUtils.getLevelForExperience(cost)).withStyle(ChatFormatting.GREEN)));
					float quanta = this.menu.quanta.get() / 100F;
					float rectification = this.menu.rectification.get() / 100F;
					int minPow = Math.round(Mth.clamp(level - level * (quanta - quanta * rectification), 1, EnchantingStatManager.getAbsoluteMaxEterna() * 4));
					int maxPow = Math.round(Mth.clamp(level + level * quanta, 1, EnchantingStatManager.getAbsoluteMaxEterna() * 4));
					list.add(Component.translatable("info.apotheosis.power_range", Component.literal("" + minPow).withStyle(ChatFormatting.DARK_RED), Component.literal("" + maxPow).withStyle(ChatFormatting.BLUE)));
					list.add(Component.translatable("info.apotheosis.item_ench", Component.literal("" + enchanting.getEnchantmentValue()).withStyle(ChatFormatting.GREEN)));
					list.add(Component.translatable("info.apotheosis.num_clues", Component.literal("" + (1 + this.menu.clues.get())).withStyle(ChatFormatting.DARK_AQUA)));
					this.drawOnLeft(stack, list, this.getGuiTop() + 29);
					break;
				}
			}
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
		list.forEach(comp -> split.addAll(this.font.getSplitter().splitLines(comp, lambdastupid, comp.getStyle())));

		this.renderComponentTooltip(stack, split, xPos, y, this.font);

		//GuiUtils.drawHoveringText(stack, list, xPos, y, width, height, maxWidth, this.font);
	}

	public void tickBook() {
		ItemStack itemstack = this.menu.getSlot(0).getItem();
		if (!ItemStack.matches(itemstack, this.last)) {
			this.last = itemstack;

			do {
				this.flipT += this.random.nextInt(4) - this.random.nextInt(4);
			} while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
		}

		this.oFlip = this.flip;
		this.oOpen = this.open;
		boolean flag = false;

		for (int i = 0; i < 3; ++i) {
			if (this.menu.costs[i] != 0) {
				flag = true;
			}
		}

		if (flag) {
			this.open += 0.2F;
		} else {
			this.open -= 0.2F;
		}

		this.open = Mth.clamp(this.open, 0.0F, 1.0F);
		float f1 = (this.flipT - this.flip) * 0.4F;

		f1 = Mth.clamp(f1, -0.2F, 0.2F);
		this.flipA += (f1 - this.flipA) * 0.9F;
		this.flip += this.flipA;
	}

	public void acceptClues(int slot, List<EnchantmentInstance> clues, boolean all) {
		this.clues.put(slot, clues);
		this.hasAllClues[slot] = all;
	}

	private static String eterna() {
		return ChatFormatting.GREEN + I18n.get("gui.apotheosis.enchant.eterna") + ChatFormatting.RESET;
	}

	private static String quanta() {
		return ChatFormatting.RED + I18n.get("gui.apotheosis.enchant.quanta") + ChatFormatting.RESET;
	}

	private static String arcana() {
		return ChatFormatting.DARK_PURPLE + I18n.get("gui.apotheosis.enchant.arcana") + ChatFormatting.RESET;
	}

	private static String rectification() {
		return ChatFormatting.YELLOW + I18n.get("gui.apotheosis.enchant.rectification") + ChatFormatting.RESET;
	}

	private static String f(float f) {
		return String.format("%.2f", f);
	}

}