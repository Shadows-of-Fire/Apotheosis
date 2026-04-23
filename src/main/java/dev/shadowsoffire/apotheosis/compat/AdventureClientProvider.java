package dev.shadowsoffire.apotheosis.compat;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.util.CommonTooltipUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class AdventureClientProvider implements IEntityComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getEntity() instanceof LivingEntity living && accessor.getServerData().getBooleanOr(Invader.BOSS_KEY, false)) {
            ListTag bossAttribs = accessor.getServerData().getListOrEmpty("apoth.modifiers");
            AttributeMap map = living.getAttributes();
            for (Tag t : bossAttribs) {
                CompoundTag tag = (CompoundTag) t;
                AttributeInstance.Packed packed = AttributeInstance.Packed.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
                AttributeInstance inst = map.getInstance(packed.attribute());
                if (inst != null) {
                    inst.apply(packed);
                }
            }
            accessor.getServerData().remove("apoth.modifiers");
            living.getPersistentData().merge(accessor.getServerData());
            CommonTooltipUtil.appendBossData(living.level(), living, tooltip::add);
        }
    }

    @Override
    public Identifier getUid() {
        return Apotheosis.loc("adventure");
    }

}
