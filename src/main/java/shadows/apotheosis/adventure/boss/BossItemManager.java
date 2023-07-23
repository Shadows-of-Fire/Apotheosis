package shadows.apotheosis.adventure.boss;

import java.util.Map;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.placebo.json.WeightedJsonReloadListener;

public class BossItemManager extends WeightedJsonReloadListener<BossItem> {

    public static final BossItemManager INSTANCE = new BossItemManager();

    public BossItemManager() {
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
    protected void validateItem(BossItem item) {
        super.validateItem(item);
        item.validate();
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT, BossItem.SERIALIZER);
    }

}
