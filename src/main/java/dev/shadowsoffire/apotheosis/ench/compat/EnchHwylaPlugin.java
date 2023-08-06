package dev.shadowsoffire.apotheosis.ench.compat;

import java.util.Map;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.anvil.AnvilTile;
import dev.shadowsoffire.apotheosis.ench.anvil.ApothAnvilBlock;
import dev.shadowsoffire.apotheosis.util.CommonTooltipUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class EnchHwylaPlugin implements IWailaPlugin, IBlockComponentProvider, IServerDataProvider<BlockEntity> {

    @Override
    public void register(IWailaCommonRegistration reg) {
        reg.registerBlockDataProvider(this, AnvilTile.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.registerBlockComponent(this, Block.class);
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
        CommonTooltipUtil.appendBlockStats(accessor.getLevel(), accessor.getBlockState(), tooltip::add);
        if (accessor.getBlock() == Blocks.ENCHANTING_TABLE) CommonTooltipUtil.appendTableStats(accessor.getLevel(), accessor.getPosition(), tooltip::add);
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te, boolean something) {
        if (te instanceof AnvilTile) {
            ItemStack stack = new ItemStack(Items.ANVIL);
            EnchantmentHelper.setEnchantments(((AnvilTile) te).getEnchantments(), stack);
            tag.put("enchantments", stack.getEnchantmentTags());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return Apotheosis.loc("ench");
    }

}
