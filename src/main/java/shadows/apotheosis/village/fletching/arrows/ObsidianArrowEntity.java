package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import shadows.apotheosis.ApotheosisObjects;

public class ObsidianArrowEntity extends AbstractArrowEntity {

	public ObsidianArrowEntity(EntityType<? extends AbstractArrowEntity> t, World world) {
		super(t, world);
	}

	public ObsidianArrowEntity(World world) {
		super(ApotheosisObjects.OB_ARROW_ENTITY, world);
	}

	public ObsidianArrowEntity(LivingEntity shooter, World world) {
		super(ApotheosisObjects.OB_ARROW_ENTITY, shooter, world);
	}

	public ObsidianArrowEntity(World world, double x, double y, double z) {
		super(ApotheosisObjects.OB_ARROW_ENTITY, x, y, z, world);
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(ApotheosisObjects.OBSIDIAN_ARROW);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void onHitEntity(EntityRayTraceResult p_213868_1_) {
		this.setBaseDamage(this.getBaseDamage() * 1.2F);
		super.onHitEntity(p_213868_1_);
	}

}