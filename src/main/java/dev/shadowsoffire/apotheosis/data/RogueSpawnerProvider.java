package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawner;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawnerRegistry;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.EntityType;

public class RogueSpawnerProvider extends DynamicRegistryProvider<RogueSpawner> {

    public RogueSpawnerProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, RogueSpawnerRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Rogue Spawners";
    }

    @Override
    public void generate() {
        this.add("brutal/zombie", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_BRUTAL)
            .weight(30)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 200)
                .stat(SpawnerStats.MAX_DELAY, 400)
                .stat(SpawnerStats.SPAWN_COUNT, 3)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 6)
                .stat(SpawnerStats.SPAWN_RANGE, 5)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 12))
            .spawnData(10, brutalZombie()));

        this.add("brutal/husk", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_BRUTAL)
            .weight(20)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 200)
                .stat(SpawnerStats.MAX_DELAY, 400)
                .stat(SpawnerStats.SPAWN_COUNT, 3)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 6)
                .stat(SpawnerStats.SPAWN_RANGE, 5)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 12))
            .spawnData(10, brutalHusk()));

        this.add("brutal/pillager", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_BRUTAL)
            .weight(20)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 200)
                .stat(SpawnerStats.MAX_DELAY, 400)
                .stat(SpawnerStats.SPAWN_COUNT, 2)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 4)
                .stat(SpawnerStats.SPAWN_RANGE, 5)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 12))
            .spawnData(10, brutalPillager()));

        this.add("brutal/rotating", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_BRUTAL)
            .weight(10)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 180)
                .stat(SpawnerStats.MAX_DELAY, 400)
                .stat(SpawnerStats.SPAWN_COUNT, 3)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 7)
                .stat(SpawnerStats.SPAWN_RANGE, 5)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 12))
            .spawnData(30, brutalZombie())
            .spawnData(20, brutalHusk())
            .spawnData(10, brutalPillager()));

        this.add("swarm/spider", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_SWARM)
            .weight(30)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 50)
                .stat(SpawnerStats.MAX_DELAY, 125)
                .stat(SpawnerStats.SPAWN_COUNT, 2)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 10)
                .stat(SpawnerStats.SPAWN_RANGE, 7)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 14))
            .spawnData(10, mobWithSwiftness(EntityType.SPIDER, 0)));

        this.add("swarm/cave_spider", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_SWARM)
            .weight(20)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 50)
                .stat(SpawnerStats.MAX_DELAY, 125)
                .stat(SpawnerStats.SPAWN_COUNT, 2)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 10)
                .stat(SpawnerStats.SPAWN_RANGE, 7)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 14))
            .spawnData(10, mobWithSwiftness(EntityType.CAVE_SPIDER, 0)));

        this.add("swarm/silverfish", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_SWARM)
            .weight(20)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 50)
                .stat(SpawnerStats.MAX_DELAY, 125)
                .stat(SpawnerStats.SPAWN_COUNT, 2)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 10)
                .stat(SpawnerStats.SPAWN_RANGE, 7)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 14))
            .spawnData(10, mobWithSwiftness(EntityType.SILVERFISH, 0)));

        this.add("swarm/fast_cave_spider", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_SWARM)
            .weight(10)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 50)
                .stat(SpawnerStats.MAX_DELAY, 125)
                .stat(SpawnerStats.SPAWN_COUNT, 2)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 10)
                .stat(SpawnerStats.SPAWN_RANGE, 7)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 14))
            .spawnData(10, mobWithSwiftness(EntityType.CAVE_SPIDER, 2)));

        this.add("swarm/baby_zombie", b -> b
            .lootTable(Apoth.LootTables.SPAWNER_SWARM)
            .weight(10)
            .stats(c -> c
                .stat(SpawnerStats.MIN_DELAY, 50)
                .stat(SpawnerStats.MAX_DELAY, 125)
                .stat(SpawnerStats.SPAWN_COUNT, 2)
                .stat(SpawnerStats.MAX_NEARBY_ENTITIES, 10)
                .stat(SpawnerStats.SPAWN_RANGE, 7)
                .stat(SpawnerStats.REQ_PLAYER_RANGE, 14)
                .stat(SpawnerStats.YOUTHFUL, true))
            .spawnData(10, mobWithSwiftness(EntityType.ZOMBIE, 1)));
    }

    private void add(String path, UnaryOperator<RogueSpawner.Builder> builder) {
        this.add(Apotheosis.loc(path), builder.apply(RogueSpawner.builder()).build());
    }

    private static CompoundTag brutalHusk() {
        String rawNbt = """
            {
                "Health": 30.0,
                "attributes": [{
                        "base": 0.23,
                        "modifiers": [{
                            "amount": 0.15,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_spd"
                        }],
                        "id": "minecraft:generic.movement_speed"
                    },
                    {
                        "base": 3.0,
                        "modifiers": [{
                            "amount": 0.5,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_dmg"
                        }],
                        "id": "minecraft:generic.attack_damage"
                    },
                    {
                        "base": 20.0,
                        "modifiers": [{
                            "amount": 0.5,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_hp"
                        }],
                        "id": "minecraft:generic.max_health"
                    }
                ],
                "id": "husk",
                "active_effects": [{
                    "visible": false,
                    "show_particles": false,
                    "duration": 10000000,
                    "id": "minecraft:fire_resistance",
                    "amplifier": 0
                }]
            }
            """;
        return parse(rawNbt);
    }

    private static CompoundTag brutalZombie() {
        String rawNbt = """
            {
                "Health": 30.0,
                "attributes": [{
                        "base": 0.23,
                        "modifiers": [{
                            "amount": 0.15,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_spd"
                        }],
                        "id": "minecraft:generic.movement_speed"
                    },
                    {
                        "base": 3.0,
                        "modifiers": [{
                            "amount": 0.5,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_dmg"
                        }],
                        "id": "minecraft:generic.attack_damage"
                    },
                    {
                        "base": 20.0,
                        "modifiers": [{
                            "amount": 0.5,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_hp"
                        }],
                        "id": "minecraft:generic.max_health"
                    }
                ],
                "id": "zombie",
                "active_effects": [{
                    "visible": false,
                    "show_particles": false,
                    "duration": 10000000,
                    "id": "minecraft:fire_resistance",
                    "amplifier": 0
                }]
            }
            """;
        return parse(rawNbt);
    }

    private static CompoundTag brutalPillager() {
        String rawNbt = """
            {
                "Health": 36.0,
                "attributes": [{
                        "base": 0.35,
                        "modifiers": [{
                            "amount": 0.15,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_spd"
                        }],
                        "id": "minecraft:generic.movement_speed"
                    },
                    {
                        "base": 1.0,
                        "modifiers": [{
                            "amount": 0.3,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_dmg"
                        }],
                        "id": "apothic_attributes:projectile_damage"
                    },
                    {
                        "base": 24.0,
                        "modifiers": [{
                            "amount": 0.5,
                            "operation": "add_multiplied_total",
                            "id": "placebo:brutal_max_hp"
                        }],
                        "id": "minecraft:generic.max_health"
                    }
                ],
                "id": "pillager",
                "active_effects": [{
                    "visible": false,
                    "show_particles": false,
                    "duration": 10000000,
                    "id": "minecraft:fire_resistance",
                    "amplifier": 0
                }],
                "HandItems": [{
                        "id": "minecraft:crossbow",
                        "Count": 1
                    },
                    {}
                ]
            }
            """;
        return parse(rawNbt);
    }

    private static CompoundTag mobWithSwiftness(EntityType<?> type, int amplifier) {
        String rawNbt = """
            {
                "id": "%s",
                "active_effects": [{
                    "visible": false,
                    "show_particles": false,
                    "duration": 10000000,
                    "id": "minecraft:speed",
                    "amplifier": %d
                }]
            }
            """.formatted(BuiltInRegistries.ENTITY_TYPE.getKey(type), amplifier);
        return parse(rawNbt);
    }

    private static CompoundTag parse(String str) {
        try {
            return TagParser.parseCompoundFully(str);
        }
        catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
