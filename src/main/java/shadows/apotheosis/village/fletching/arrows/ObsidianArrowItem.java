package shadows.apotheosis.village.fletching.arrows;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.apotheosis.Apotheosis;

public class ObsidianArrowItem extends ArrowItem implements IApothArrowItem {

	public ObsidianArrowItem() {
		super(new Item.Properties().tab(Apotheosis.APOTH_GROUP));
	}

	@Override
	public AbstractArrowEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
		return new ObsidianArrowEntity(shooter, world);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("info.apotheosis.obsidian_arrow").withStyle(TextFormatting.BLUE));
	}

	@Override
	public AbstractArrowEntity fromDispenser(World world, double x, double y, double z) {
		AbstractArrowEntity e = new ObsidianArrowEntity(world, x, y, z);
		e.pickup = AbstractArrowEntity.PickupStatus.ALLOWED;
		return e;
	}

}