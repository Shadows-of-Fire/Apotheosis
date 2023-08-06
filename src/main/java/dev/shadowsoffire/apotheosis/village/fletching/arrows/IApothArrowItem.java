package dev.shadowsoffire.apotheosis.village.fletching.arrows;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;

public interface IApothArrowItem {

    AbstractArrow fromDispenser(Level world, double x, double y, double z);

}
