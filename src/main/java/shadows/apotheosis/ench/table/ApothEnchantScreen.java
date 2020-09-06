package shadows.apotheosis.ench.table;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import shadows.apotheosis.Apotheosis;

public class ApothEnchantScreen extends ContainerScreen<ApothEnchantContainer> {

	private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation(Apotheosis.MODID, "textures/gui/enchanting_table.png");
	private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
	private static final BookModel MODEL_BOOK = new BookModel();
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

	public ApothEnchantScreen(ApothEnchantContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		this.ySize = 197;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
		this.font.drawString(this.title.getFormattedText(), 12.0F, 5.0F, 4210752);
		this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 7.0F, this.ySize - 96 + 4F, 4210752);
		this.font.drawString(I18n.format("gui.apotheosis.enchant.eterna"), 19, 74, 0x3DB53D);
		this.font.drawString(I18n.format("gui.apotheosis.enchant.quanta"), 19, 84, 0xFC5454);
		this.font.drawString(I18n.format("gui.apotheosis.enchant.arcana"), 19, 94, 0xA800A8);
	}

	@Override
	public void tick() {
		super.tick();
		this.tickBook();
		float current = this.container.eterna.get();
		if (current != eterna) {
			if (current > eterna) eterna += Math.min(current - eterna, Math.max(0.16F, (current - eterna) * 0.1F));
			else eterna = Math.max(eterna - lastEterna * 0.075F, current);
		}
		if (current > 0) lastEterna = current;

		current = this.container.quanta.get();
		if (current != quanta) {
			if (current > quanta) quanta += Math.min(current - quanta, Math.max(0.04F, (current - quanta) * 0.1F));
			else quanta = Math.max(quanta - lastQuanta * 0.075F, current);
		}
		if (current > 0) lastQuanta = current;

		current = this.container.arcana.get();
		if (current != arcana) {
			if (current > arcana) arcana += Math.min(current - arcana, Math.max(0.04F, (current - arcana) * 0.1F));
			else arcana = Math.max(arcana - lastArcana * 0.075F, current);
		}
		if (current > 0) lastArcana = current;
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;

		for (int k = 0; k < 3; ++k) {
			double d0 = p_mouseClicked_1_ - (i + 60);
			double d1 = p_mouseClicked_3_ - (j + 14 + 19 * k);
			if (d0 >= 0.0D && d1 >= 0.0D && d0 < 108.0D && d1 < 19.0D && this.container.enchantItem(this.minecraft.player, k)) {
				this.minecraft.playerController.sendEnchantPacket(this.container.windowId, k);
				return true;
			}
		}

		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		RenderHelper.disableGuiDepthLighting();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
		int xCenter = (this.width - this.xSize) / 2;
		int yCenter = (this.height - this.ySize) / 2;
		this.blit(xCenter, yCenter, 0, 0, this.xSize, this.ySize);
		RenderSystem.matrixMode(5889);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		int k = (int) this.minecraft.getWindow().getGuiScaleFactor();
		RenderSystem.viewport((this.width - 320) / 2 * k, (this.height - 240) / 2 * k, 320 * k, 240 * k);
		RenderSystem.translatef(-0.34F, 0.23F, 0.0F);
		RenderSystem.multMatrix(Matrix4f.perspective(90.0D, 1.3333334F, 9.0F, 80.0F));
		RenderSystem.matrixMode(5888);
		MatrixStack matrixstack = new MatrixStack();
		matrixstack.push();
		MatrixStack.Entry matrixstack$entry = matrixstack.peek();

		matrixstack$entry.getModel().loadIdentity();
		matrixstack$entry.getNormal().loadIdentity();
		matrixstack.translate(0.0D, 5, 1984.0D);
		matrixstack.scale(5.0F, 5.0F, 5.0F);
		matrixstack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
		matrixstack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(20.0F));
		float f1 = MathHelper.lerp(partialTicks, this.oOpen, this.open);

		matrixstack.translate((1.0F - f1) * 0.2F, (1.0F - f1) * 0.1F, (1.0F - f1) * 0.25F);
		float f2 = -(1.0F - f1) * 90.0F - 90.0F;
		matrixstack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(f2));
		matrixstack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180.0F));

		float f3 = MathHelper.lerp(partialTicks, this.oFlip, this.flip) + 0.25F;
		float f4 = MathHelper.lerp(partialTicks, this.oFlip, this.flip) + 0.75F;
		f3 = (f3 - MathHelper.fastFloor(f3)) * 1.6F - 0.3F;
		f4 = (f4 - MathHelper.fastFloor(f4)) * 1.6F - 0.3F;
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

		RenderSystem.enableRescaleNormal();
		MODEL_BOOK.setPageAngles(0.0F, f3, f4, f1);
		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuffer());
		IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(MODEL_BOOK.getLayer(ENCHANTMENT_TABLE_BOOK_TEXTURE));
		MODEL_BOOK.render(matrixstack, ivertexbuilder, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
		irendertypebuffer$impl.draw();
		matrixstack.pop();

		RenderSystem.matrixMode(5889);
		RenderSystem.viewport(0, 0, this.minecraft.getWindow().getFramebufferWidth(), this.minecraft.getWindow().getFramebufferHeight());
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(5888);
		RenderHelper.enableGuiDepthLighting();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		EnchantmentNameParts.getInstance().reseedRandomGenerator(this.container.func_217005_f());
		int lapis = this.container.getLapisAmount();

		for (int slot = 0; slot < 3; ++slot) {
			int j1 = xCenter + 60;
			int k1 = j1 + 20;
			this.setBlitOffset(0);
			this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);

			int level = this.container.enchantLevels[slot];
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (level == 0) {
				this.blit(j1, yCenter + 14 + 19 * slot, 148, 218, 108, 19);
			} else {
				String s = "" + level;
				int nameWidth = 86 - this.font.getStringWidth(s);
				String s1 = EnchantmentNameParts.getInstance().generateNewRandomName(this.font, nameWidth);
				FontRenderer fontrenderer = this.minecraft.getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);
				int j2 = 6839882;
				if ((lapis < slot + 1 || this.minecraft.player.experienceLevel < level) && !this.minecraft.player.abilities.isCreativeMode || this.container.enchantClue[slot] == -1) { // Forge: render buttons as disabled when enchantable but enchantability not met on lower levels
					this.blit(j1, yCenter + 14 + 19 * slot, 148, 218, 108, 19);
					this.blit(j1 + 1, yCenter + 15 + 19 * slot, 16 * slot, 238, 16, 16);
					fontrenderer.drawSplitString(s1, k1, yCenter + 16 + 19 * slot, nameWidth, (j2 & 16711422) >> 1);
					j2 = 4226832;
				} else {
					int k2 = mouseX - (xCenter + 60);
					int l2 = mouseY - (yCenter + 14 + 19 * slot);
					if (k2 >= 0 && l2 >= 0 && k2 < 108 && l2 < 19) {
						this.blit(j1, yCenter + 14 + 19 * slot, 148, 237, 108, 19);
						j2 = 16777088;
					} else {
						this.blit(j1, yCenter + 14 + 19 * slot, 148, 199, 108, 19);
					}

					this.blit(j1 + 1, yCenter + 15 + 19 * slot, 16 * slot, 223, 16, 16);
					fontrenderer.drawSplitString(s1, k1, yCenter + 16 + 19 * slot, nameWidth, j2);
					j2 = 8453920;
				}
				fontrenderer = this.minecraft.fontRenderer;
				fontrenderer.drawStringWithShadow(s, k1 + 86 - fontrenderer.getStringWidth(s), yCenter + 16 + 19 * slot + 7, j2);
			}
		}

		this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
		if (eterna > 0) {
			this.blit(xCenter + 59, yCenter + 75, 0, 197, (int) (eterna / 50 * 110), 5);
		}
		if (quanta > 0) {
			this.blit(xCenter + 59, yCenter + 85, 0, 202, (int) (quanta / 10 * 110), 5);
		}
		if (arcana > 0) {
			this.blit(xCenter + 59, yCenter + 95, 0, 207, (int) (arcana / 10 * 110), 5);
		}
	}

	@Override
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		p_render_3_ = this.minecraft.getRenderPartialTicks();
		this.renderBackground();
		super.render(p_render_1_, p_render_2_, p_render_3_);
		this.renderHoveredToolTip(p_render_1_, p_render_2_);
		boolean creative = this.minecraft.player.abilities.isCreativeMode;
		int lapis = this.container.getLapisAmount();

		for (int j = 0; j < 3; ++j) {
			int level = this.container.enchantLevels[j];
			Enchantment enchantment = Enchantment.getEnchantmentByID(this.container.enchantClue[j]);
			int clue = this.container.worldClue[j];
			int i1 = j + 1;
			if (this.isPointInRegion(60, 14 + 19 * j, 108, 17, p_render_1_, p_render_2_) && level > 0) {
				List<String> list = Lists.newArrayList();
				list.add("" + TextFormatting.GRAY + TextFormatting.ITALIC + I18n.format("container.enchant.clue", enchantment == null ? "" : enchantment.getDisplayName(clue).getFormattedText()));
				if (enchantment == null) {
					Collections.addAll(list, "", TextFormatting.RED + I18n.format("forge.container.enchant.limitedEnchantability"));
				} else if (!creative) {
					list.add("");
					if (this.minecraft.player.experienceLevel < level) {
						list.add(TextFormatting.RED + I18n.format("container.enchant.level.requirement", this.container.enchantLevels[j]));
					} else {
						String s;
						if (i1 == 1) {
							s = I18n.format("container.enchant.lapis.one");
						} else {
							s = I18n.format("container.enchant.lapis.many", i1);
						}

						TextFormatting textformatting = lapis >= i1 ? TextFormatting.GRAY : TextFormatting.RED;
						list.add(textformatting + "" + s);
						if (i1 == 1) {
							s = I18n.format("container.enchant.level.one");
						} else {
							s = I18n.format("container.enchant.level.many", i1);
						}

						list.add(TextFormatting.GRAY + "" + s);
					}
				}
				this.renderTooltip(list, p_render_1_, p_render_2_);
				break;
			}
		}

		if (this.isPointInRegion(60, 14 + 19 * 3 + 5, 110, 5, p_render_1_, p_render_2_))

		{
			List<String> list = Lists.newArrayList();
			list.add(eterna() + I18n.format("gui.apotheosis.enchant.eterna.desc"));
			list.add(I18n.format("gui.apotheosis.enchant.eterna.desc2"));
			list.add("");
			list.add(TextFormatting.GRAY + I18n.format("gui.apotheosis.enchant.eterna.desc3", f(this.container.eterna.get()), 50F));
			this.renderTooltip(list, p_render_1_, p_render_2_);
		}

		if (this.isPointInRegion(60, 14 + 19 * 3 + 15, 110, 5, p_render_1_, p_render_2_)) {
			List<String> list = Lists.newArrayList();
			list.add(quanta() + I18n.format("gui.apotheosis.enchant.quanta.desc"));
			list.add(I18n.format("gui.apotheosis.enchant.quanta.desc2"));
			list.add(I18n.format("gui.apotheosis.enchant.quanta.desc3"));
			list.add("");
			list.add(TextFormatting.GRAY + I18n.format("gui.apotheosis.enchant.quanta.desc4", f(this.container.quanta.get() * 10F)));
			this.renderTooltip(list, p_render_1_, p_render_2_);
		}

		if (this.isPointInRegion(60, 14 + 19 * 3 + 25, 110, 5, p_render_1_, p_render_2_)) {
			List<String> list = Lists.newArrayList();
			list.add(arcana() + I18n.format("gui.apotheosis.enchant.arcana.desc"));
			list.add(I18n.format("gui.apotheosis.enchant.arcana.desc2"));
			list.add(I18n.format("gui.apotheosis.enchant.arcana.desc3"));
			list.add("");
			list.add(TextFormatting.GRAY + I18n.format("gui.apotheosis.enchant.arcana.desc4", f(this.container.arcana.get() * 10F)));
			this.renderTooltip(list, p_render_1_, p_render_2_);
		}

	}

	public void tickBook() {
		ItemStack itemstack = this.container.getSlot(0).getStack();
		if (!ItemStack.areItemStacksEqual(itemstack, this.last)) {
			this.last = itemstack;

			while (true) {
				this.flipT += this.random.nextInt(4) - this.random.nextInt(4);
				if (!(this.flip <= this.flipT + 1.0F) || !(this.flip >= this.flipT - 1.0F)) {
					break;
				}
			}
		}

		++this.ticks;
		this.oFlip = this.flip;
		this.oOpen = this.open;
		boolean flag = false;

		for (int i = 0; i < 3; ++i) {
			if (this.container.enchantLevels[i] != 0) {
				flag = true;
			}
		}

		if (flag) {
			this.open += 0.2F;
		} else {
			this.open -= 0.2F;
		}

		this.open = MathHelper.clamp(this.open, 0.0F, 1.0F);
		float f1 = (this.flipT - this.flip) * 0.4F;
		f1 = MathHelper.clamp(f1, -0.2F, 0.2F);
		this.flipA += (f1 - this.flipA) * 0.9F;
		this.flip += this.flipA;
	}

	private static String eterna() {
		return TextFormatting.GREEN + I18n.format("gui.apotheosis.enchant.eterna") + TextFormatting.RESET;
	}

	private static String quanta() {
		return TextFormatting.RED + I18n.format("gui.apotheosis.enchant.quanta") + TextFormatting.RESET;
	}

	private static String arcana() {
		return TextFormatting.DARK_PURPLE + I18n.format("gui.apotheosis.enchant.arcana") + TextFormatting.RESET;
	}

	private static String f(float f) {
		return String.format("%.2f", f);
	}

}
