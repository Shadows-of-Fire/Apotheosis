package dev.shadowsoffire.apotheosis.loot;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apoth.BuiltInRegs;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;
import net.minecraft.resources.Identifier;

public class RarityOverrideRegistry extends DynamicRegistry<RarityOverride> {

    public static final RarityOverrideRegistry INSTANCE = new RarityOverrideRegistry();

    protected Map<LootCategory, RarityOverride> byCategory = new HashMap<>();

    public RarityOverrideRegistry() {
        super(Apotheosis.LOGGER, Apotheosis.loc("rarity_override"), RegistrySerializer.synced(RarityOverride.CODEC));
    }

    @Nullable
    public RarityOverride getOverride(LootCategory category) {
        return this.byCategory.get(category);
    }

    @Override
    protected void validateItem(Identifier key, RarityOverride value) {
        String path = key.getPath().replace('/', ':');
        Identifier cat = Identifier.tryParse(path);
        Preconditions.checkNotNull(cat, "Invalid category path: " + path);
        LootCategory category = BuiltInRegs.LOOT_CATEGORY.getValue(cat);
        Preconditions.checkArgument(category != null && !category.isNone(), "Category not found: " + cat);
        Preconditions.checkArgument(value.category() == category, "Category mismatch: " + value.category() + " != " + category);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.byCategory = new HashMap<>();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        this.registry.forEach((key, value) -> {
            String path = key.getPath().replace('/', ':');
            Identifier cat = Identifier.tryParse(path);
            LootCategory category = BuiltInRegs.LOOT_CATEGORY.getValue(cat);
            RarityOverride old = this.byCategory.put(category, value);
            if (old != null) {
                this.logger.warn("Duplicate rarity override for category {}: Old: {}, New: {}", path, this.getKey(old), key);
            }
        });
    }

}
