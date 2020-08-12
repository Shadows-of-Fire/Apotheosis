package shadows.apotheosis.ench.compat;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.anvil.ApothAnvilBlock;
import shadows.apotheosis.ench.anvil.AnvilTile;

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
		if (tag.getInt("ub") > 0) tooltip.add(Enchantments.UNBREAKING.getDisplayName(tag.getInt("ub")));
		if (tag.getInt("sp") > 0) tooltip.add(ApotheosisObjects.SPLITTING.getDisplayName(tag.getInt("sp")));
	}

	@Override
	public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
		if (te instanceof AnvilTile) {
			tag.putInt("ub", ((AnvilTile) te).getUnbreaking());
			tag.putInt("sp", ((AnvilTile) te).getSplitting());
		}
	}

}