package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;
import shadows.apotheosis.Apoth;

public class ObsidianArrowEntity extends AbstractArrow {

	public ObsidianArrowEntity(EntityType<? extends AbstractArrow> t, Level world) {
		super(t, world);
	}

	public ObsidianArrowEntity(Level world) {
		super(Apoth.Entities.OBSIDIAN_ARROW, world);
	}

	public ObsidianArrowEntity(LivingEntity shooter, Level world) {
		super(Apoth.Entities.OBSIDIAN_ARROW, shooter, world);
	}

	public ObsidianArrowEntity(Level world, double x, double y, double z) {
		super(Apoth.Entities.OBSIDIAN_ARROW, x, y, z, world);
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(Apoth.Items.OBSIDIAN_ARROW);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void onHitEntity(EntityHitResult p_213868_1_) {
		this.setBaseDamage(this.getBaseDamage() * 1.2F);
		super.onHitEntity(p_213868_1_);
	}

}