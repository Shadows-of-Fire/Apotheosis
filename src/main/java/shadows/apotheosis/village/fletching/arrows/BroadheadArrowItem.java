package shadows.apotheosis.village.fletching.arrows;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;

public class BroadheadArrowItem extends ArrowItem {

	public BroadheadArrowItem() {
		super(new Item.Properties().group(Apotheosis.APOTH_GROUP));
	}

	@Override
	public AbstractArrowEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
		BroadheadArrowEntity e = new BroadheadArrowEntity(shooter, world);
		e.addEffect(new EffectInstance(ApotheosisObjects.BLEEDING, 300));
		return e;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("info.apotheosis.broadhead_arrow").mergeStyle(TextFormatting.RED));
	}

}