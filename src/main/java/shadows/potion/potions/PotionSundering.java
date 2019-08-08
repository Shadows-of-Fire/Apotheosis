package shadows.potion.potions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.potion.PotionModule;

public class PotionSundering extends Effect {

	public PotionSundering() {
		super(EffectType.HARMFUL, 0x989898);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, int x, int y, float z) {
		Minecraft.getInstance().textureManager.bindTexture(PotionModule.POTION_TEX);
		AbstractGui.blit(x + 7, y + 7, 0, 0, 16, 16, 128, 128);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderHUDEffect(EffectInstance effect, AbstractGui gui, int x, int y, float z, float alpha) {
		Minecraft.getInstance().textureManager.bindTexture(PotionModule.POTION_TEX);
		AbstractGui.blit(x + 4, y + 4, 0, 0, 16, 16, 128, 128);
	}

}
