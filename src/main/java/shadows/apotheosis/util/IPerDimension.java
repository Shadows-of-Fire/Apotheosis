package shadows.apotheosis.util;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;

public interface IPerDimension {

	Set<ResourceLocation> getDimensions();

	public static Predicate<IPerDimension> matches(ServerLevelAccessor level) {
		return obj -> {
			Set<ResourceLocation> dims = obj.getDimensions();
			return dims == null || dims.isEmpty() || dims.contains(level.getLevel().dimension().location());
		};
	}

}
