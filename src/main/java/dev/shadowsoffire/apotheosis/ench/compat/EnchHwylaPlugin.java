package dev.shadowsoffire.apotheosis.ench.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.Ench;
import dev.shadowsoffire.apotheosis.ench.anvil.AnvilTile;
import dev.shadowsoffire.apotheosis.ench.anvil.ApothAnvilBlock;
import dev.shadowsoffire.apotheosis.ench.objects.FilteringShelfBlock.FilteringShelfTile;
import dev.shadowsoffire.apotheosis.util.CommonTooltipUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

@WailaPlugin
public class EnchHwylaPlugin implements IWailaPlugin, IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void register(IWailaCommonRegistration reg) {
        if (Apotheosis.enableEnch) reg.registerBlockDataProvider(this, AnvilTile.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        if (Apotheosis.enableEnch) reg.registerBlockComponent(this, Block.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof ApothAnvilBlock) {
            CompoundTag tag = accessor.getServerData();
            Map<Enchantment, Integer> enchants = EnchantmentHelper.deserializeEnchantments(tag.getList("enchantments", Tag.TAG_COMPOUND));
            for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
                tooltip.add(e.getKey().getFullname(e.getValue()));
            }
        }
        CommonTooltipUtil.appendBlockStats(accessor.getLevel(), accessor.getBlockState(), accessor.getPosition(), tooltip::add);
        if (accessor.getBlock() == Blocks.ENCHANTING_TABLE) {
            CommonTooltipUtil.appendTableStats(accessor.getLevel(), accessor.getPosition(), tooltip::add);
            tooltip.remove(Identifiers.MC_TOTAL_ENCHANTMENT_POWER);
        }

        if (accessor.getBlock() == Ench.Blocks.FILTERING_SHELF.get()) this.handleFilteringShelf(tooltip, accessor);
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor access) {
        if (access.getBlockEntity() instanceof AnvilTile te) {
            ItemStack stack = new ItemStack(Items.ANVIL);
            EnchantmentHelper.setEnchantments(te.getEnchantments(), stack);
            tag.put("enchantments", stack.getEnchantmentTags());
        }
    }

    @Override
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        if (accessor.getBlock() == Ench.Blocks.FILTERING_SHELF.get()) {
            return IElementHelper.get().item(accessor.getPickedResult()); // Need to override the book icon back to the shelf when Jade triggers vanilla integration.
        }
        return currentIcon;
    }

    @Override
    public ResourceLocation getUid() {
        return Apotheosis.loc("ench");
    }

    @Override
    public int getDefaultPriority() {
        return 1150; // Magic number which puts us after item display.
    }

    public void handleFilteringShelf(ITooltip tooltip, BlockAccessor accessor) {
        tooltip.remove(Identifiers.MC_ENCHANTMENT_POWER);
        tooltip.remove(Identifiers.MC_CHISELED_BOOKSHELF);
        tooltip.remove(Identifiers.UNIVERSAL_ITEM_STORAGE);

        if (accessor.showDetails()) {
            return;
        }

        if (accessor.getBlockEntity() instanceof FilteringShelfTile tile) {
            Optional<Vec2> optional = ChiseledBookShelfBlock.getRelativeHitCoordinatesForBlockFace(accessor.getHitResult(), accessor.getBlockState().getValue(HorizontalDirectionalBlock.FACING));
            if (optional.isEmpty()) {
                return;
            }
            int slot = ChiseledBookShelfBlock.getHitSlot(optional.get());
            ItemStack stack = tile.getItem(slot);
            if (stack.isEmpty()) return;
            tooltip.add(CommonComponents.EMPTY);
            IElementHelper helper = tooltip.getElementHelper();
            List<IElement> elements = new ArrayList<>();
            elements.add(helper.smallItem(stack).clearCachedMessage());
            elements.add(helper
                .text(
                    Component.literal(" ").append(Component.literal(IDisplayHelper.get().humanReadableNumber(stack.getCount(), "", false)).append("× ").append(stack.getHoverName())))
                .message(null));
            tooltip.add(elements);

            if (stack.getTag() != null && stack.getTag().contains(EnchantedBookItem.TAG_STORED_ENCHANTMENTS)) {
                List<Component> list = new ArrayList<>();
                ItemStack.appendEnchantmentNames(list, EnchantedBookItem.getEnchantments(stack));
                for (Component c : list)
                    tooltip.add(Component.literal(" - ").append(c));
            }
        }
    }

}
