package dev.shadowsoffire.apotheosis.adventure.compat;

import dev.shadowsoffire.apotheosis.util.CommonTooltipUtil;
import dev.shadowsoffire.placebo.compat.TOPCompat;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AdventureTOPPlugin implements TOPCompat.Provider {

    public static void register() {
        TOPCompat.registerProvider(new AdventureTOPPlugin());
    }

    @Override
    public void addProbeEntityInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, Entity entity, IProbeHitEntityData hitData) {
        if (entity instanceof LivingEntity living && living.getPersistentData().getBoolean("apoth.boss")) {
            CommonTooltipUtil.appendBossData(living.level(), living, info::mcText);
        }
    }

}
