package dev.shadowsoffire.apotheosis.advancements.predicates;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Checks that the target entity was spawned by an {@link Invader}.
 */
public class InvaderPredicate implements EntitySubPredicate {

    public static final InvaderPredicate INSTANCE = new InvaderPredicate();

    public static final MapCodec<InvaderPredicate> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, Vec3 position) {
        return entity.getPersistentData().contains(Invader.BOSS_KEY);
    }

}
