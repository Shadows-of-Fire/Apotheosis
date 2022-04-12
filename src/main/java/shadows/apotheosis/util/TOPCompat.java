package shadows.apotheosis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import shadows.placebo.Placebo;

public class TOPCompat {

	public static void register() {
		InterModComms.sendTo("theoneprobe", "getTheOneProbe", GetTheOneProbe::new);
	}

	private static List<Provider> providers = new ArrayList<>();

	public static class GetTheOneProbe implements Function<ITheOneProbe, Void> {

		@Override
		public Void apply(ITheOneProbe probe) {
			probe.registerProvider(new IProbeInfoProvider() {
				@Override
				public String getID() {
					return new ResourceLocation(Placebo.MODID, "plugin").toString();
				}

				@Override
				public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World level, BlockState state, IProbeHitData hitData) {
					providers.forEach(p -> p.addProbeInfo(mode, info, player, level, state, hitData));
				}
			});
			return null;
		}

	}

	public static void registerProvider(Provider p) {
		providers.add(p);
	}

	public static interface Provider {
		public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World level, BlockState state, IProbeHitData hitData);
	}

}