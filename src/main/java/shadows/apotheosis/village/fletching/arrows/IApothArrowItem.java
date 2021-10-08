package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.world.World;

public interface IApothArrowItem {

	AbstractArrowEntity fromDispenser(World world, double x, double y, double z);

}
