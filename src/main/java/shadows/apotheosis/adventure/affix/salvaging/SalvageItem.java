package shadows.apotheosis.adventure.affix.salvaging;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.color.GradientColor;

public class SalvageItem extends Item {

    protected final LootRarity rarity;

    public SalvageItem(LootRarity rarity, Properties pProperties) {
        super(pProperties);
        this.rarity = rarity;
    }

    @Override
    public Component getName(ItemStack pStack) {
        if (this.rarity == LootRarity.ANCIENT) {
            return Component.translatable(this.getDescriptionId(pStack)).withStyle(ChatFormatting.OBFUSCATED).withStyle(s -> s.withColor(GradientColor.RAINBOW));
        }
        return Component.translatable(this.getDescriptionId(pStack)).withStyle(Style.EMPTY.withColor(this.rarity.color()));
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> list, TooltipFlag pIsAdvanced) {
        list.add(Component.translatable("info.apotheosis.rarity_material", this.rarity.toComponent()).withStyle(ChatFormatting.GRAY));
    }

}
