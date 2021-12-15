package shadows.apotheosis.ench.compat;

import java.util.List;
import java.util.Map;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.anvil.ApothAnvilBlock;

@WailaPlugin
public class EnchHwylaPlugin implements IWailaPlugin, IComponentProvider, IServerDataProvider<BlockEntity> {

	@Override
	public void register(IRegistrar reg) {
		reg.registerComponentProvider(this, TooltipPosition.BODY, ApothAnvilBlock.class);
		reg.registerBlockDataProvider(this, ApothAnvilBlock.class);
	}

	@Override
	public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config) {
		CompoundTag tag = accessor.getServerData();
		Map<Enchantment, Integer> enchants = EnchantmentHelper.deserializeEnchantments(tag.getList("enchantments", Constants.NBT.TAG_COMPOUND));
		for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
			tooltip.add(e.getKey().getFullname(e.getValue()));
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te) {
		if (te instanceof AnvilTile) {
			ItemStack stack = new ItemStack(Items.ANVIL);
			EnchantmentHelper.setEnchantments(((AnvilTile) te).getEnchantments(), stack);
			tag.put("enchantments", stack.getEnchantmentTags());
		}
	}

}