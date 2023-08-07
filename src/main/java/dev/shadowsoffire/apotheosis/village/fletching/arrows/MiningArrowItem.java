package dev.shadowsoffire.apotheosis.village.fletching.arrows;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

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

public class MiningArrowItem extends ArrowItem implements IApothArrowItem {

    protected final Supplier<Item> breakerItem;
    protected final MiningArrowEntity.Type arrowType;

    public MiningArrowItem(Supplier<Item> breakerItem, MiningArrowEntity.Type arrowType) {
        super(new Item.Properties());
        this.breakerItem = breakerItem;
        this.arrowType = arrowType;
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
        return new MiningArrowEntity(shooter, world, new ItemStack(this.breakerItem.get()), this.arrowType);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("info.apotheosis.mining_arrow." + this.arrowType.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GOLD));
    }

    @Override
    public AbstractArrow fromDispenser(Level world, double x, double y, double z) {
        return new MiningArrowEntity(world, x, y, z, new ItemStack(this.breakerItem.get()), this.arrowType);
    }

}
