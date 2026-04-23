package dev.shadowsoffire.apotheosis.advancements.predicates;

import com.mojang.serialization.MapCodec;

import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

/**
 * Checks that the target entity is a {@link Monster}.
 */
public class MonsterPredicate implements EntitySubPredicate {

    public static final MonsterPredicate INSTANCE = new MonsterPredicate();

    public static final MapCodec<MonsterPredicate> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, Vec3 position) {
        return entity instanceof Monster;
    }

}
