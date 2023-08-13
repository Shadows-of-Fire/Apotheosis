package shadows.apotheosis.village.fletching.arrows;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.village.VillageModule;

public class ExplosiveArrowEntity extends AbstractArrow {

    public ExplosiveArrowEntity(EntityType<? extends AbstractArrow> t, Level world) {
        super(t, world);
    }

    public ExplosiveArrowEntity(Level world) {
        super(Apoth.Entities.EXPLOSIVE_ARROW.get(), world);
    }

    public ExplosiveArrowEntity(LivingEntity shooter, Level world) {
        super(Apoth.Entities.EXPLOSIVE_ARROW.get(), shooter, world);
    }

    public ExplosiveArrowEntity(Level world, double x, double y, double z) {
        super(Apoth.Entities.EXPLOSIVE_ARROW.get(), x, y, z, world);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Apoth.Items.EXPLOSIVE_ARROW.get());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity living) {
        if (!this.level.isClientSide) {
            this.level.explode(this, living.getX(), living.getY(), living.getZ(), 2, false, VillageModule.expArrowMode);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult res) {
        super.onHitBlock(res);
        Vec3 vec = res.getLocation();
        if (!this.level.isClientSide) {
            this.level.explode(this, vec.x(), vec.y(), vec.z(), 3, false, VillageModule.expArrowMode);
            this.discard();
        }
    }
}
