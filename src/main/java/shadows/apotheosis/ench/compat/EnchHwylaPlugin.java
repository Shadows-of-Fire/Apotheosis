package shadows.apotheosis.ench.compat;

import java.util.List;
import java.util.Map;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.ench.anvil.ApothAnvilBlock;

@WailaPlugin
public class EnchHwylaPlugin implements IWailaPlugin, IComponentProvider, IServerDataProvider<TileEntity> {

	@Override
	public void register(IRegistrar reg) {
		reg.registerComponentProvider(this, TooltipPosition.BODY, ApothAnvilBlock.class);
		reg.registerBlockDataProvider(this, ApothAnvilBlock.class);
	}

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		CompoundNBT tag = accessor.getServerData();
		Map<Enchantment, Integer> enchants = EnchantmentHelper.deserializeEnchantments(tag.getList("enchantments", Constants.NBT.TAG_COMPOUND));
		for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
			tooltip.add(e.getKey().getFullname(e.getValue()));
		}
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
		if (te instanceof AnvilTile) {
			ItemStack stack = new ItemStack(Items.ANVIL);
			EnchantmentHelper.setEnchantments(((AnvilTile) te).getEnchantments(), stack);
			tag.put("enchantments", stack.getEnchantmentTags());
		}
	}

}