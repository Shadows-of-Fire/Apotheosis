package shadows.apotheosis.village.fletching.arrows;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.apotheosis.Apotheosis;

public class ObsidianArrowItem extends ArrowItem implements IApothArrowItem {

	public ObsidianArrowItem() {
		super(new Item.Properties().tab(Apotheosis.APOTH_GROUP));
	}

	@Override
	public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
		return new ObsidianArrowEntity(shooter, world);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(new TranslatableComponent("info.apotheosis.obsidian_arrow").withStyle(ChatFormatting.BLUE));
	}

	@Override
	public AbstractArrow fromDispenser(Level world, double x, double y, double z) {
		AbstractArrow e = new ObsidianArrowEntity(world, x, y, z);
		e.pickup = AbstractArrow.Pickup.ALLOWED;
		return e;
	}

}