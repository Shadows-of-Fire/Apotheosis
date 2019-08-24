package shadows.village.fletching.arrows;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import shadows.ApotheosisObjects;

public class BroadheadArrowEntity extends ArrowEntity {

	public BroadheadArrowEntity(EntityType<? extends ArrowEntity> t, World world) {
		super(t, world);
	}

	public BroadheadArrowEntity(World world) {
		super(ApotheosisObjects.BH_ARROW_ENTITY, world);
	}

	public BroadheadArrowEntity(LivingEntity shooter, World world) {
		super(world, shooter);
	}

	@Override
	protected ItemStack getArrowStack() {
		return new ItemStack(ApotheosisObjects.BROADHEAD_ARROW);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public EntityType<?> getType() {
		return ApotheosisObjects.BH_ARROW_ENTITY;
	}

	@Override
	public int getColor() {
		return -1;
	}
}