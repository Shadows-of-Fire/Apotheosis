package shadows.apotheosis.ench.compat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import shadows.apotheosis.ench.anvil.AnvilTile;
import shadows.apotheosis.util.TOPCompat;

public class EnchTOPPlugin implements TOPCompat.Provider {

	public static void register() {
		TOPCompat.registerProvider(new EnchTOPPlugin());
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World level, BlockState state, IProbeHitData hitData) {
		TileEntity te = level.getBlockEntity(hitData.getPos());
		if (te instanceof AnvilTile) {
			Object2IntMap<Enchantment> enchants = ((AnvilTile) te).getEnchantments();
			for (Object2IntMap.Entry<Enchantment> e : enchants.object2IntEntrySet()) {
				info.text(e.getKey().getFullname(e.getIntValue()));
			}
		}
	}

}