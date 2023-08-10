package dev.shadowsoffire.apotheosis.adventure.boss;

import java.util.Map;

import com.google.gson.JsonElement;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class BossRegistry extends WeightedDynamicRegistry<ApothBoss> {

    public static final BossRegistry INSTANCE = new BossRegistry();

    public BossRegistry() {
        super(AdventureModule.LOGGER, "bosses", false, false);
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        var map = super.prepare(pResourceManager, pProfiler);
        // The author of Brutal Bosses continues to use my subkey, so, here we go doing stupid shit to work around it.
        map.keySet().removeIf(r -> "brutalbosses".equals(r.getNamespace()));
        return map;
    }

    @Override
    protected void validateItem(ApothBoss item) {
        super.validateItem(item);
        item.validate();
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT, ApothBoss.SERIALIZER);
    }

}
