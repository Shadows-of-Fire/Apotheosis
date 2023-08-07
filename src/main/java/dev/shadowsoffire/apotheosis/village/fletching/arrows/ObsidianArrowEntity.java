package dev.shadowsoffire.apotheosis.village.fletching.arrows;

import dev.shadowsoffire.apotheosis.Apoth;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

public class ObsidianArrowEntity extends AbstractArrow {

    public ObsidianArrowEntity(EntityType<? extends AbstractArrow> t, Level world) {
        super(t, world);
    }

    public ObsidianArrowEntity(Level world) {
        super(Apoth.Entities.OBSIDIAN_ARROW.get(), world);
    }

    public ObsidianArrowEntity(LivingEntity shooter, Level world) {
        super(Apoth.Entities.OBSIDIAN_ARROW.get(), shooter, world);
    }

    public ObsidianArrowEntity(Level world, double x, double y, double z) {
        super(Apoth.Entities.OBSIDIAN_ARROW.get(), x, y, z, world);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Apoth.Items.OBSIDIAN_ARROW.get());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void onHitEntity(EntityHitResult res) {
        double base = this.getBaseDamage();
        this.setBaseDamage(base * 1.2F);
        super.onHitEntity(res);
        this.setBaseDamage(base);
    }

}
