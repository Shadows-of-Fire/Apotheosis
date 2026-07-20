package dev.shadowsoffire.apotheosis.mobs.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class SpawnCooldownSavedData extends SavedData {

    public Object2IntMap<ResourceLocation> bossCooldowns = new Object2IntOpenHashMap<>();

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        for (Object2IntMap.Entry<ResourceLocation> e : this.bossCooldowns.object2IntEntrySet()) {
            tag.putInt(e.getKey().toString(), e.getIntValue());
        }
        return tag;
    }

    public void tick(ResourceLocation level) {
        int remaining = this.bossCooldowns.getInt(level);
        if (remaining > 0) {
            this.bossCooldowns.put(level, remaining - 1);
            this.setDirty();
        }
    }

    public boolean isOnCooldown(Level level) {
        return this.isOnCooldown(level.dimension().location());
    }

    public boolean isOnCooldown(ResourceLocation level) {
        return this.bossCooldowns.getInt(level) > 0;
    }

    public void startCooldown(Level level, int timer) {
        this.startCooldown(level.dimension().location(), timer);
    }

    public void startCooldown(ResourceLocation level, int timer) {
        this.bossCooldowns.put(level, timer);
        this.setDirty();
    }

    public static SpawnCooldownSavedData loadTimes(CompoundTag tag, HolderLookup.Provider registries) {
        var data = new SpawnCooldownSavedData();

        data.bossCooldowns.clear();
        for (String s : tag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id != null) {
                int val = tag.getInt(s);
                data.bossCooldowns.put(id, val);
            }
        }

        return data;
    }

}
