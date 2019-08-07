package shadows.potion;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemLuckyFoot extends Item {

	public ItemLuckyFoot() {
		super(new Item.Properties().maxStackSize(1));
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("info.apotheosis.lucky"));
	}
}
