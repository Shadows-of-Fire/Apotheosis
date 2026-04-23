package dev.shadowsoffire.apotheosis.compat;

import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class AdventureHwylaPlugin implements IWailaPlugin {

    public static final String BOSS_KEY = Invader.BOSS_KEY;

    @Override
    public void register(IWailaCommonRegistration reg) {
        reg.registerEntityDataProvider(new AdventureServerDataProvider(), LivingEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.registerEntityComponent(new AdventureClientProvider(), Entity.class);
    }

}
