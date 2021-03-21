package shadows.apotheosis.village.fletching.arrows;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MiningArrowItem extends ArrowItem {

	protected final Supplier<Item> breakerItem;
	protected final MiningArrowEntity.Type arrowType;

	public MiningArrowItem(Supplier<Item> breakerItem, MiningArrowEntity.Type arrowType) {
		super(new Item.Properties().group(ItemGroup.COMBAT));
		this.breakerItem = breakerItem;
		this.arrowType = arrowType;
	}

	@Override
	public AbstractArrowEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
		MiningArrowEntity e = new MiningArrowEntity(shooter, world, new ItemStack(this.breakerItem.get()), this.arrowType);
		return e;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("info.apotheosis.mining_arrow." + this.arrowType.name().toLowerCase(Locale.ROOT)).mergeStyle(TextFormatting.GOLD));
	}

}