package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GemCaseBlockItem extends BlockItem {

    private final int maxCount;

    public GemCaseBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
        this.maxCount = block instanceof GemCaseBlock gcb ? gcb.maxCount : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.accept(Apotheosis.lang("tooltip", "gem_case.capacity", GemCaseBlock.format(this.maxCount)).withStyle(ChatFormatting.GOLD));
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null && data.contains("gems")) {
            int gems = data.getUnsafe().getCompoundOrEmpty("gems").size();
            if (gems > 0) {
                tooltip.accept(Apotheosis.lang("tooltip", "gem_case.unique_gems", gems).withStyle(ChatFormatting.GRAY));
            }
        }

    }
}
