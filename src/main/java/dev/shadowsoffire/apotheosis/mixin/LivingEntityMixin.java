package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Used to make the glowing effect on mobs use their name color.
     */
    @Override
    public int getTeamColor() {
        int color = super.getTeamColor();
        if (color == 16777215) {
            Component name = this.getCustomName();
            if (name != null && name.getStyle().getColor() != null) color = name.getStyle().getColor().getValue();
        }
        return color;
    }

}
