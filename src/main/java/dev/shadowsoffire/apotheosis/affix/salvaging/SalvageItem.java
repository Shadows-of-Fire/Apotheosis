package dev.shadowsoffire.apotheosis.affix.salvaging;

import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class SalvageItem extends Item {

    protected final DynamicHolder<LootRarity> rarity;

    public SalvageItem(DynamicHolder<LootRarity> rarity, Properties pProperties) {
        super(pProperties);
        this.rarity = rarity;
    }

    @Override
    public Component getName(ItemStack pStack) {
        if (!this.rarity.isBound()) {
            return super.getName(pStack);
        }
        return Component.translatable(this.getDescriptionId()).withStyle(Style.EMPTY.withColor(this.rarity.get().color()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        if (this.rarity.isBound()) {
            tooltip.accept(Component.translatable("info.apotheosis.rarity_material", this.rarity.get().toComponent()).withStyle(ChatFormatting.GRAY));
        }
    }

}
