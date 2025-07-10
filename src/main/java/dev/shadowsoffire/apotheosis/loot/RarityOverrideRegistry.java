package dev.shadowsoffire.apotheosis.loot;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apoth.BuiltInRegs;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.resources.ResourceLocation;

public class RarityOverrideRegistry extends DynamicRegistry<RarityOverride> {

    public static final RarityOverrideRegistry INSTANCE = new RarityOverrideRegistry();

    protected Map<LootCategory, RarityOverride> byCategory = new HashMap<>();

    public RarityOverrideRegistry() {
        super(Apotheosis.LOGGER, "rarity_override", true, false);
    }

    @Nullable
    public RarityOverride getOverride(LootCategory category) {
        return this.byCategory.get(category);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("rarity_override"), RarityOverride.CODEC);
    }

    @Override
    protected void validateItem(ResourceLocation key, RarityOverride value) {
        String path = key.getPath().replace('/', ':');
        ResourceLocation cat = ResourceLocation.tryParse(path);
        Preconditions.checkNotNull(cat, "Invalid category path: " + path);
        LootCategory category = BuiltInRegs.LOOT_CATEGORY.get(cat);
        Preconditions.checkArgument(!category.isNone(), "Category not found: " + cat);
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
            ResourceLocation cat = ResourceLocation.tryParse(path);
            LootCategory category = BuiltInRegs.LOOT_CATEGORY.get(cat);
            RarityOverride old = this.byCategory.put(category, value);
            if (old != null) {
                this.logger.warn("Duplicate rarity override for category {}: Old: {}, New: {}", path, this.getKey(old), key);
            }
        });
    }

}
