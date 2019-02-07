package shadows.potion.potions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.Apotheosis;
import shadows.potion.PotionModule;

public class PotionSundering extends Potion {

	public PotionSundering() {
		super(true, 0x989898);
		setPotionName("effect." + Apotheosis.MODID + ".sundering");
	}

	@Override
	public boolean hasStatusIcon() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		mc.renderEngine.bindTexture(PotionModule.POTION_TEX);
		Gui.drawModalRectWithCustomSizedTexture(x + 7, y + 7, 0, 0, 16, 16, 128, 128);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
		mc.renderEngine.bindTexture(PotionModule.POTION_TEX);
		Gui.drawModalRectWithCustomSizedTexture(x + 4, y + 4, 0, 0, 16, 16, 128, 128);
	}

}
