package shadows.apotheosis.ench.compat;

import java.util.Map;

import mcp.mobius.waila.api.IComponentProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.anvil.ApothAnvilBlock;
import shadows.apotheosis.util.CommonTooltipUtil;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class EnchHwylaPlugin implements IWailaPlugin, IComponentProvider, IServerDataProvider<BlockEntity> {

	@Override
	public void register(IWailaCommonRegistration reg) {
		reg.registerBlockDataProvider(this, AnvilTile.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration reg) {
		reg.registerComponentProvider(this, TooltipPosition.BODY, Block.class);
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

}