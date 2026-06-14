package dev.shadowsoffire.apotheosis.compat;

import com.google.common.base.Predicates;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.mobs.util.BossStats;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class AdventureServerDataProvider implements IServerDataProvider<EntityAccessor> {

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor access) {
        if (access.getEntity() instanceof LivingEntity living && living.getPersistentData().getBooleanOr(Invader.BOSS_KEY, false)) {
            tag.putBoolean(Invader.BOSS_KEY, true);
            tag.putString(Invader.RARITY_KEY, living.getPersistentData().getStringOr(Invader.RARITY_KEY, ""));
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                AttributeMap map = living.getAttributes();
                ListTag bossAttribs = new ListTag();
                BuiltInRegistries.ATTRIBUTE.listElements().map(map::getInstance).filter(Predicates.notNull()).forEach(inst -> {
                    for (AttributeModifier modif : inst.getModifiers()) {
                        if (modif.id().getPath().startsWith(BossStats.MODIFIER_PREFIX)) {
                            AttributeInstance.Packed packed = inst.pack();
                            bossAttribs.add(AttributeInstance.Packed.CODEC.encodeStart(NbtOps.INSTANCE, packed).getOrThrow());
                            break;
                        }
                    }
                });
                tag.put("apoth.modifiers", bossAttribs);
            }
        }
    }

    @Override
    public Identifier getUid() {
        return Apotheosis.loc("adventure");
    }

}
