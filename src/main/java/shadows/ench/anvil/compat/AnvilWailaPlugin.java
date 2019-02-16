package shadows.ench.anvil.compat;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import shadows.ApotheosisObjects;
import shadows.ench.anvil.BlockAnvilExt;
import shadows.ench.anvil.TileAnvil;

@WailaPlugin
public class AnvilWailaPlugin implements IWailaPlugin, IWailaDataProvider {

	@Override
	public void register(IWailaRegistrar reg) {
		reg.registerBodyProvider(this, BlockAnvilExt.class);
		reg.registerNBTProvider(this, BlockAnvilExt.class);
	}

	@Override
	public List<String> getWailaBody(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		NBTTagCompound tag = accessor.getNBTData();
		if (tag.getInteger("ub") > 0) tooltip.add(Enchantments.UNBREAKING.getTranslatedName(tag.getInteger("ub")));
		if (tag.getInteger("sp") > 0) tooltip.add(ApotheosisObjects.SPLITTING.getTranslatedName(tag.getInteger("sp")));
		return tooltip;
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
		if (te instanceof TileAnvil) {
			tag.setInteger("ub", ((TileAnvil) te).getUnbreaking());
			tag.setInteger("sp", ((TileAnvil) te).getSplitting());
		}
		return tag;
	}

}
