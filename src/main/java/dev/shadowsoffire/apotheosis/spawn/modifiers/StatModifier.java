package dev.shadowsoffire.apotheosis.spawn.modifiers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apotheosis.spawn.SpawnerModule;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

public record StatModifier<T>(SpawnerStat<T> stat, T value, T min, T max) {

    public boolean apply(ApothSpawnerTile tile) {
        return this.stat.apply(this.value, this.min, this.max, tile);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.stat.getId(), 32);
        buf.writeNbt((CompoundTag) this.stat.getModifierCodec().encodeStart(NbtOps.INSTANCE, this).getOrThrow(false, SpawnerModule.LOG::error));
    }

    public static StatModifier<?> read(FriendlyByteBuf buf) {
        SpawnerStat<?> stat = SpawnerStats.REGISTRY.get(buf.readUtf(32));
        return stat.getModifierCodec().decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(false, SpawnerModule.LOG::error).getFirst();
    }

    public static StatModifier<?> parse(JsonObject obj) {
        SpawnerStat<?> stat = SpawnerStats.REGISTRY.get(obj.get("id").getAsString());
        if (stat == null) throw new JsonParseException("Failed to parse a stat modifier - missing or invalid ID");
        return stat.getModifierCodec().decode(JsonOps.INSTANCE, obj).getOrThrow(false, SpawnerModule.LOG::error).getFirst();
    }

}
