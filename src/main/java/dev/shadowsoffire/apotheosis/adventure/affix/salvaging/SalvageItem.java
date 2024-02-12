package dev.shadowsoffire.apotheosis.adventure.affix.salvaging;

import java.util.List;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class SalvageItem extends Item {

    protected final DynamicHolder<LootRarity> rarity;

    public SalvageItem(DynamicHolder<LootRarity> rarity, Properties pProperties) {
        super(pProperties);
        this.rarity = rarity;
    }

    @Override
    public Component getName(ItemStack pStack) {
        if ("ancient".equals(this.rarity.getId().getPath())) {
            return Component.translatable(this.getDescriptionId(pStack)).withStyle(ChatFormatting.OBFUSCATED).withStyle(s -> s.withColor(GradientColor.RAINBOW));
        }
        if (!this.rarity.isBound()) return super.getName(pStack);
        return Component.translatable(this.getDescriptionId(pStack)).withStyle(Style.EMPTY.withColor(this.rarity.get().getColor()));
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> list, TooltipFlag pIsAdvanced) {
        if (this.rarity.isBound()) {
            list.add(Component.translatable("info.apotheosis.rarity_material", this.rarity.get().toComponent()).withStyle(ChatFormatting.GRAY));
        }
    }

}
