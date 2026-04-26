package dev.shadowsoffire.apotheosis.data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicTagKey;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import dev.shadowsoffire.placebo.util.data.DynamicTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

/**
 * Generates the gear-set tag JSON files based on the inline {@code .tag(...)} declarations made by
 * {@link GearSetProvider}. Must be registered to datagen <em>after</em> {@link GearSetProvider} so that
 * {@link GearSetProvider#TAG_ASSOCIATIONS} is fully populated by the time this provider runs.
 */
public class GearSetTagsProvider extends DynamicTagProvider<GearSet> {

    public GearSetTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, GearSetRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Gear Set Tags";
    }

    @Override
    protected void addTags() {
        for (Map.Entry<String, List<Identifier>> entry : GearSetProvider.TAG_ASSOCIATIONS.entrySet()) {
            DynamicTagKey<GearSet> key = new DynamicTagKey<>(GearSetRegistry.INSTANCE.getId(), Apotheosis.loc(entry.getKey()));
            TagAppender appender = this.tag(key);
            for (Identifier id : entry.getValue()) {
                appender.add(id);
            }
        }
    }
}
