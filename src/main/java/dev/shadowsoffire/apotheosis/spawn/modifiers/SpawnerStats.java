package dev.shadowsoffire.apotheosis.spawn.modifiers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerBlock;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class SpawnerStats {

    public static final Map<String, SpawnerStat<?>> REGISTRY = new LinkedHashMap<>();

    public static final SpawnerStat<Short> MIN_DELAY = register(new ShortStat("min_delay", s -> s.spawner.minSpawnDelay, (s, v) -> s.spawner.minSpawnDelay = v));

    public static final SpawnerStat<Short> MAX_DELAY = register(new ShortStat("max_delay", s -> s.spawner.maxSpawnDelay, (s, v) -> s.spawner.maxSpawnDelay = v));

    public static final SpawnerStat<Short> SPAWN_COUNT = register(new ShortStat("spawn_count", s -> s.spawner.spawnCount, (s, v) -> s.spawner.spawnCount = v));

    public static final SpawnerStat<Short> MAX_NEARBY_ENTITIES = register(new ShortStat("max_nearby_entities", s -> s.spawner.maxNearbyEntities, (s, v) -> s.spawner.maxNearbyEntities = v));

    public static final SpawnerStat<Short> REQ_PLAYER_RANGE = register(new ShortStat("req_player_range", s -> s.spawner.requiredPlayerRange, (s, v) -> s.spawner.requiredPlayerRange = v));

    public static final SpawnerStat<Short> SPAWN_RANGE = register(new ShortStat("spawn_range", s -> s.spawner.spawnRange, (s, v) -> s.spawner.spawnRange = v));

    public static final SpawnerStat<Boolean> IGNORE_PLAYERS = register(new BoolStat("ignore_players", s -> s.ignoresPlayers, (s, v) -> s.ignoresPlayers = v));

    public static final SpawnerStat<Boolean> IGNORE_CONDITIONS = register(new BoolStat("ignore_conditions", s -> s.ignoresConditions, (s, v) -> s.ignoresConditions = v));

    public static final SpawnerStat<Boolean> REDSTONE_CONTROL = register(new BoolStat("redstone_control", s -> s.redstoneControl, (s, v) -> s.redstoneControl = v));

    public static final SpawnerStat<Boolean> IGNORE_LIGHT = register(new BoolStat("ignore_light", s -> s.ignoresLight, (s, v) -> s.ignoresLight = v));

    public static final SpawnerStat<Boolean> NO_AI = register(new BoolStat("no_ai", s -> s.hasNoAI, (s, v) -> s.hasNoAI = v));

    public static final SpawnerStat<Boolean> SILENT = register(new BoolStat("silent", s -> s.silent, (s, v) -> s.silent = v));

    public static final SpawnerStat<Boolean> BABY = register(new BoolStat("baby", s -> s.baby, (s, v) -> s.baby = v));

    public static void generateTooltip(ApothSpawnerTile tile, Consumer<Component> list) {
        for (SpawnerStat<?> stat : REGISTRY.values()) {
            Component comp = stat.getTooltip(tile);
            if (!comp.getString().isEmpty()) {
                list.accept(comp);
            }
        }
    }

    private static <T extends SpawnerStat<?>> T register(T t) {
        REGISTRY.put(t.getId(), t);
        return t;
    }

    private static abstract class Base<T> implements SpawnerStat<T> {

        protected final String id;
        protected final Function<ApothSpawnerTile, T> getter;
        protected final BiConsumer<ApothSpawnerTile, T> setter;

        private Base(String id, Function<ApothSpawnerTile, T> getter, BiConsumer<ApothSpawnerTile, T> setter) {
            this.id = id;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public T getValue(ApothSpawnerTile spawner) {
            return this.getter.apply(spawner);
        }

    }

    private static class BoolStat extends Base<Boolean> {

        private final Codec<StatModifier<Boolean>> modifierCodec = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.BOOL.fieldOf("value").forGetter(StatModifier::value))
            .apply(inst, value -> new StatModifier<>(this, value, false, true)));

        private BoolStat(String id, Function<ApothSpawnerTile, Boolean> getter, BiConsumer<ApothSpawnerTile, Boolean> setter) {
            super(id, getter, setter);
        }

        @Override
        public Codec<StatModifier<Boolean>> getModifierCodec() {
            return this.modifierCodec;
        }

        @Override
        public Component getTooltip(ApothSpawnerTile spawner) {
            return this.getValue(spawner) ? this.name().withStyle(ChatFormatting.DARK_GREEN) : CommonComponents.EMPTY;
        }

        @Override
        public boolean apply(Boolean value, Boolean min, Boolean max, ApothSpawnerTile spawner) {
            boolean old = this.getter.apply(spawner);
            this.setter.accept(spawner, value);
            return old != this.getter.apply(spawner);
        }

    }

    private static class ShortStat extends Base<Short> {

        public static final Codec<Short> BOUNDS_CODEC = Codec.intRange(-1, Short.MAX_VALUE).xmap(Integer::shortValue, Short::intValue);

        private final Codec<StatModifier<Short>> modifierCodec = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.SHORT.fieldOf("value").forGetter(StatModifier::value),
                BOUNDS_CODEC.fieldOf("min").forGetter(StatModifier::min),
                BOUNDS_CODEC.fieldOf("max").forGetter(StatModifier::max))
            .apply(inst, (value, min, max) -> new StatModifier<>(this, value, min == -1 ? 0 : min, max == -1 ? Short.MAX_VALUE : max)));

        private ShortStat(String id, Function<ApothSpawnerTile, Integer> getter, BiConsumer<ApothSpawnerTile, Short> setter) {
            super(id, tile -> getter.apply(tile).shortValue(), setter);
        }

        @Override
        public Codec<StatModifier<Short>> getModifierCodec() {
            return this.modifierCodec;
        }

        @Override
        public Component getTooltip(ApothSpawnerTile spawner) {
            return ApothSpawnerBlock.concat(this.name(), this.getValue(spawner));
        }

        @Override
        public boolean apply(Short value, Short min, Short max, ApothSpawnerTile spawner) {
            int old = this.getter.apply(spawner);
            this.setter.accept(spawner, (short) Mth.clamp(old + value, min, max));
            return old != this.getter.apply(spawner);
        }

    }

}
