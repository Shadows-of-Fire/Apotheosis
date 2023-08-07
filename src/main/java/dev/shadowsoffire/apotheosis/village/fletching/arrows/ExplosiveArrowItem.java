package dev.shadowsoffire.apotheosis.village.fletching.arrows;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExplosiveArrowItem extends ArrowItem implements IApothArrowItem {

    public ExplosiveArrowItem(Item.Properties props) {
        super(props);
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
        return new ExplosiveArrowEntity(shooter, world);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("info.apotheosis.explosive_arrow").withStyle(ChatFormatting.RED));
    }

    @Override
    public AbstractArrow fromDispenser(Level world, double x, double y, double z) {
        return new ExplosiveArrowEntity(world, x, y, z);
    }

}
