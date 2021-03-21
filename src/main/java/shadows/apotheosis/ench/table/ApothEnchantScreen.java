package shadows.apotheosis.ench.table;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
	protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
		this.font.func_243248_b(stack, this.title, 12.0F, 5.0F, 4210752);
		this.font.func_243248_b(stack, this.playerInventory.getDisplayName(), 7.0F, this.ySize - 96 + 4F, 4210752);
		this.font.drawString(stack, I18n.format("gui.apotheosis.enchant.eterna"), 19, 74, 0x3DB53D);
		this.font.drawString(stack, I18n.format("gui.apotheosis.enchant.quanta"), 19, 84, 0xFC5454);
		this.font.drawString(stack, I18n.format("gui.apotheosis.enchant.arcana"), 19, 94, 0xA800A8);
	}

	@Override
	public void tick() {
		super.tick();
		this.tickBook();
		float current = this.container.eterna.get();
		if (current != this.eterna) {
			if (current > this.eterna) this.eterna += Math.min(current - this.eterna, Math.max(0.16F, (current - this.eterna) * 0.1F));
			else this.eterna = Math.max(this.eterna - this.lastEterna * 0.075F, current);
		}
		if (current > 0) this.lastEterna = current;

		current = this.container.quanta.get();
		if (current != this.quanta) {
			if (current > this.quanta) this.quanta += Math.min(current - this.quanta, Math.max(0.04F, (current - this.quanta) * 0.1F));
			else this.quanta = Math.max(this.quanta - this.lastQuanta * 0.075F, current);
		}
		if (current > 0) this.lastQuanta = current;

		current = this.container.arcana.get();
		if (current != this.arcana) {
			if (current > this.arcana) this.arcana += Math.min(current - this.arcana, Math.max(0.04F, (current - this.arcana) * 0.1F));
			else this.arcana = Math.max(this.arcana - this.lastArcana * 0.075F, current);
		}
		if (current > 0) this.lastArcana = current;
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
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderHelper.setupGuiFlatDiffuseLighting();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
		int xCenter = (this.width - this.xSize) / 2;
		int yCenter = (this.height - this.ySize) / 2;
		this.blit(stack, xCenter, yCenter, 0, 0, this.xSize, this.ySize);
		RenderSystem.matrixMode(5889);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		int k = (int) this.minecraft.getMainWindow().getGuiScaleFactor();
		RenderSystem.viewport((this.width - 320) / 2 * k, (this.height - 240) / 2 * k, 320 * k, 240 * k);
		RenderSystem.translatef(-0.34F, 0.23F, 0.0F);
		RenderSystem.multMatrix(Matrix4f.perspective(90.0D, 1.3333334F, 9.0F, 80.0F));
		RenderSystem.matrixMode(5888);
		stack.push();
		MatrixStack.Entry matrixstack$entry = stack.getLast();

		matrixstack$entry.getMatrix().setIdentity();
		matrixstack$entry.getNormal().setIdentity();
		stack.translate(0.0D, 5, 1984.0D);
		stack.scale(5.0F, 5.0F, 5.0F);
		stack.rotate(Vector3f.ZP.rotationDegrees(180.0F));
		stack.rotate(Vector3f.XP.rotationDegrees(20.0F));
		float f1 = MathHelper.lerp(partialTicks, this.oOpen, this.open);

		stack.translate((1.0F - f1) * 0.2F, (1.0F - f1) * 0.1F, (1.0F - f1) * 0.25F);
		float f2 = -(1.0F - f1) * 90.0F - 90.0F;
		stack.rotate(Vector3f.YP.rotationDegrees(f2));
		stack.rotate(Vector3f.XP.rotationDegrees(180.0F));

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
		MODEL_BOOK.setBookState(0.0F, f3, f4, f1);
		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(MODEL_BOOK.getRenderType(ENCHANTMENT_TABLE_BOOK_TEXTURE));
		MODEL_BOOK.render(stack, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		irendertypebuffer$impl.finish();
		stack.pop();

		RenderSystem.matrixMode(5889);
		RenderSystem.viewport(0, 0, this.minecraft.getMainWindow().getFramebufferWidth(), this.minecraft.getMainWindow().getFramebufferHeight());
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(5888);
		RenderHelper.setupGui3DDiffuseLighting();
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
				this.blit(stack, j1, yCenter + 14 + 19 * slot, 148, 218, 108, 19);
			} else {
				String s = "" + level;
				int width = 86 - this.font.getStringWidth(s);
				ITextProperties itextproperties = EnchantmentNameParts.getInstance().getGalacticEnchantmentName(this.font, width);
				int color = 6839882;
				if ((lapis < slot + 1 || this.minecraft.player.experienceLevel < level) && !this.minecraft.player.abilities.isCreativeMode || this.container.enchantClue[slot] == -1) { // Forge: render buttons as disabled when enchantable but enchantability not met on lower levels
					this.blit(stack, j1, yCenter + 14 + 19 * slot, 148, 218, 108, 19);
					this.blit(stack, j1 + 1, yCenter + 15 + 19 * slot, 16 * slot, 239, 16, 16);
					this.font.func_238418_a_(itextproperties, k1, yCenter + 16 + 19 * slot, width, (color & 16711422) >> 1);
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
					this.font.func_238418_a_(itextproperties, k1, yCenter + 16 + 19 * slot, width, color);
					color = 8453920;
				}

				this.font.drawStringWithShadow(stack, s, k1 + 86 - this.font.getStringWidth(s), yCenter + 16 + 19 * slot + 7, color);
			}
		}

		this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
		if (this.eterna > 0) {
			this.blit(stack, xCenter + 59, yCenter + 75, 0, 197, (int) (this.eterna / this.container.eterna.getMax() * 110), 5);
		}
		if (this.quanta > 0) {
			this.blit(stack, xCenter + 59, yCenter + 85, 0, 202, (int) (this.quanta / 10 * 110), 5);
		}
		if (this.arcana > 0) {
			this.blit(stack, xCenter + 59, yCenter + 95, 0, 207, (int) (this.arcana / 10 * 110), 5);
		}
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		partialTicks = this.minecraft.getRenderPartialTicks();
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(stack, mouseX, mouseY);
		boolean creative = this.minecraft.player.abilities.isCreativeMode;
		int lapis = this.container.getLapisAmount();

		for (int j = 0; j < 3; ++j) {
			int level = this.container.enchantLevels[j];
			Enchantment enchantment = Enchantment.getEnchantmentByID(this.container.enchantClue[j]);
			int clue = this.container.worldClue[j];
			int i1 = j + 1;
			if (this.isPointInRegion(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && level > 0) {
				List<ITextComponent> list = Lists.newArrayList();
				list.add(new TranslationTextComponent("container.enchant.clue", enchantment == null ? "" : enchantment.getDisplayName(clue).getString()).mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
				if (enchantment == null) {
					Collections.addAll(list, new StringTextComponent(""), new TranslationTextComponent("forge.container.enchant.limitedEnchantability").mergeStyle(TextFormatting.RED));
				} else if (!creative) {
					list.add(new StringTextComponent(""));
					if (this.minecraft.player.experienceLevel < level) {
						list.add(new TranslationTextComponent("container.enchant.level.requirement", this.container.enchantLevels[j]).mergeStyle(TextFormatting.RED));
					} else {
						String s;
						if (i1 == 1) {
							s = I18n.format("container.enchant.lapis.one");
						} else {
							s = I18n.format("container.enchant.lapis.many", i1);
						}

						TextFormatting textformatting = lapis >= i1 ? TextFormatting.GRAY : TextFormatting.RED;
						list.add(new StringTextComponent(s).mergeStyle(textformatting));
						if (i1 == 1) {
							s = I18n.format("container.enchant.level.one");
						} else {
							s = I18n.format("container.enchant.level.many", i1);
						}

						list.add(new StringTextComponent(s).mergeStyle(TextFormatting.GRAY));
					}
				}
				this.func_243308_b(stack, list, mouseX, mouseY);
				break;
			}
		}

		if (this.isPointInRegion(60, 14 + 19 * 3 + 5, 110, 5, mouseX, mouseY))

		{
			List<ITextComponent> list = Lists.newArrayList();
			list.add(new StringTextComponent(eterna() + I18n.format("gui.apotheosis.enchant.eterna.desc")));
			list.add(new StringTextComponent(I18n.format("gui.apotheosis.enchant.eterna.desc2")));
			list.add(new StringTextComponent(""));
			list.add(new StringTextComponent(I18n.format("gui.apotheosis.enchant.eterna.desc3", f(this.container.eterna.get()), this.container.eterna.getMax())).mergeStyle(TextFormatting.GRAY));
			this.func_243308_b(stack, list, mouseX, mouseY);
		}

		if (this.isPointInRegion(60, 14 + 19 * 3 + 15, 110, 5, mouseX, mouseY)) {
			List<ITextComponent> list = Lists.newArrayList();
			list.add(new StringTextComponent(quanta() + I18n.format("gui.apotheosis.enchant.quanta.desc")));
			list.add(new StringTextComponent(I18n.format("gui.apotheosis.enchant.quanta.desc2")));
			list.add(new StringTextComponent(I18n.format("gui.apotheosis.enchant.quanta.desc3")));
			list.add(new StringTextComponent(""));
			list.add(new StringTextComponent(I18n.format("gui.apotheosis.enchant.quanta.desc4", f(this.container.quanta.get() * 10F))).mergeStyle(TextFormatting.GRAY));
			this.func_243308_b(stack, list, mouseX, mouseY);
		}

		if (this.isPointInRegion(60, 14 + 19 * 3 + 25, 110, 5, mouseX, mouseY)) {
			List<ITextComponent> list = Lists.newArrayList();
			list.add(new StringTextComponent(arcana() + I18n.format("gui.apotheosis.enchant.arcana.desc")));
			list.add(new StringTextComponent(I18n.format("gui.apotheosis.enchant.arcana.desc2")));
			list.add(new StringTextComponent(I18n.format("gui.apotheosis.enchant.arcana.desc3")));
			list.add(new StringTextComponent(""));
			list.add(new StringTextComponent(I18n.format("gui.apotheosis.enchant.arcana.desc4", f(this.container.arcana.get() * 10F))).mergeStyle(TextFormatting.GRAY));
			this.func_243308_b(stack, list, mouseX, mouseY);
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