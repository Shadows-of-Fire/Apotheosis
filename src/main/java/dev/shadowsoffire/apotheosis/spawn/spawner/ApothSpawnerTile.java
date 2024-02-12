package dev.shadowsoffire.apotheosis.spawn.spawner;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent.PositionCheck;
import net.minecraftforge.eventbus.api.Event.Result;

public class ApothSpawnerTile extends SpawnerBlockEntity {

    public boolean ignoresPlayers = false;
    public boolean ignoresConditions = false;
    public boolean redstoneControl = false;
    public boolean ignoresLight = false;
    public boolean hasNoAI = false;
    public boolean silent = false;
    public boolean baby = false;

    public ApothSpawnerTile(BlockPos pos, BlockState state) {
        super(pos, state);
        this.spawner = new SpawnerLogicExt();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putBoolean("ignore_players", this.ignoresPlayers);
        tag.putBoolean("ignore_conditions", this.ignoresConditions);
        tag.putBoolean("redstone_control", this.redstoneControl);
        tag.putBoolean("ignore_light", this.ignoresLight);
        tag.putBoolean("no_ai", this.hasNoAI);
        tag.putBoolean("silent", this.silent);
        tag.putBoolean("baby", this.baby);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        this.ignoresPlayers = tag.getBoolean("ignore_players");
        this.ignoresConditions = tag.getBoolean("ignore_conditions");
        this.redstoneControl = tag.getBoolean("redstone_control");
        this.ignoresLight = tag.getBoolean("ignore_light");
        this.hasNoAI = tag.getBoolean("no_ai");
        this.silent = tag.getBoolean("silent");
        this.baby = tag.getBoolean("baby");
        super.load(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    public class SpawnerLogicExt extends BaseSpawner {

        @Override
        public void setEntityId(EntityType<?> type, @Nullable Level level, RandomSource rand, BlockPos pos) {
            this.nextSpawnData = new SpawnData();
            super.setEntityId(type, level, rand, pos);
            this.spawnPotentials = SimpleWeightedRandomList.single(this.nextSpawnData);
            if (level != null) this.delay(level, pos);
        }

        @Override
        public void broadcastEvent(Level level, BlockPos pos, int id) {
            level.blockEvent(pos, Blocks.SPAWNER, id, 0);
        }

        @Override
        public void setNextSpawnData(Level level, BlockPos pos, SpawnData nextSpawnData) {
            super.setNextSpawnData(level, pos, nextSpawnData);

            if (level != null) {
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, 4);
            }
        }

        @Nullable
        @Override
        public BlockEntity getSpawnerBlockEntity() {
            return ApothSpawnerTile.this;
        }

        protected boolean isActivated(Level level, BlockPos pos) {
            boolean hasPlayer = ApothSpawnerTile.this.ignoresPlayers || this.isNearPlayer(level, pos);
            return hasPlayer && (!ApothSpawnerTile.this.redstoneControl || ApothSpawnerTile.this.level.hasNeighborSignal(pos));
        }

        private void delay(Level pLevel, BlockPos pPos) {
            if (this.maxSpawnDelay <= this.minSpawnDelay) {
                this.spawnDelay = this.minSpawnDelay;
            }
            else {
                this.spawnDelay = this.minSpawnDelay + pLevel.random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
            }

            this.spawnPotentials.getRandom(pLevel.random).ifPresent(potential -> {
                this.setNextSpawnData(pLevel, pPos, potential.getData());
            });
            this.broadcastEvent(pLevel, pPos, 1);
        }

        @Override
        public void clientTick(Level pLevel, BlockPos pPos) {
            if (!this.isActivated(pLevel, pPos)) {
                this.oSpin = this.spin;
            }
            else {
                double d0 = pPos.getX() + pLevel.random.nextDouble();
                double d1 = pPos.getY() + pLevel.random.nextDouble();
                double d2 = pPos.getZ() + pLevel.random.nextDouble();
                pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                pLevel.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                }

                this.oSpin = this.spin;
                this.spin = (this.spin + 1000.0F / (this.spawnDelay + 200.0F)) % 360.0D;
            }

        }

        @SuppressWarnings("deprecation")
        @Override
        public void serverTick(ServerLevel level, BlockPos pPos) {
            if (this.isActivated(level, pPos)) {
                if (this.spawnDelay == -1) {
                    this.delay(level, pPos);
                }

                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                }
                else {
                    boolean flag = false;
                    RandomSource rand = level.getRandom();
                    SpawnData spawnData = this.getOrCreateNextSpawnData(level, rand, pPos);

                    for (int i = 0; i < this.spawnCount; ++i) {
                        CompoundTag tag = spawnData.getEntityToSpawn();
                        EntityType<?> entityType = EntityType.by(tag).orElse(null);
                        if (entityType == null) {
                            this.delay(level, pPos);
                            return;
                        }

                        ListTag posList = tag.getList("Pos", 6);
                        int size = posList.size();
                        double x = size >= 1 ? posList.getDouble(0) : pPos.getX() + (rand.nextDouble() - rand.nextDouble()) * this.spawnRange + 0.5D;
                        double y = size >= 2 ? posList.getDouble(1) : (double) (pPos.getY() + rand.nextInt(3) - 1);
                        double z = size >= 3 ? posList.getDouble(2) : pPos.getZ() + (rand.nextDouble() - rand.nextDouble()) * this.spawnRange + 0.5D;
                        if (level.noCollision(entityType.getAABB(x, y, z))) {
                            BlockPos blockpos = BlockPos.containing(x, y, z);

                            // LOGIC CHANGE : Ability to ignore conditions set in the spawner and by the entity.
                            LyingLevel liar = new LyingLevel(level);
                            boolean useLiar = false;
                            if (!ApothSpawnerTile.this.ignoresConditions) {
                                if (ApothSpawnerTile.this.ignoresLight) {
                                    boolean pass = false;
                                    for (int light = 0; light < 16; light++) {
                                        liar.setFakeLightLevel(light);
                                        if (this.checkSpawnRules(spawnData, entityType, liar, blockpos)) {
                                            pass = true;
                                            break;
                                        }
                                    }
                                    if (!pass) continue;
                                    else useLiar = true;
                                }
                                else if (!this.checkSpawnRules(spawnData, entityType, level, blockpos)) continue;
                            }

                            Entity entity = EntityType.loadEntityRecursive(tag, level, freshEntity -> {
                                freshEntity.moveTo(x, y, z, freshEntity.getYRot(), freshEntity.getXRot());
                                return freshEntity;
                            });

                            if (entity == null) {
                                this.delay(level, pPos);
                                return;
                            }

                            // Raise the NoAI Flag and set the apotheosis:movable flag for the main mob and all mob passengers.
                            if (ApothSpawnerTile.this.hasNoAI) {
                                entity.getSelfAndPassengers().filter(Mob.class::isInstance).map(Mob.class::cast).forEach(mob -> {
                                    mob.setNoAi(true);
                                    mob.getPersistentData().putBoolean("apotheosis:movable", true);
                                });
                            }

                            if (ApothSpawnerTile.this.baby && entity instanceof Mob mob) {
                                mob.setBaby(true);
                            }

                            if (ApothSpawnerTile.this.silent) entity.setSilent(true);

                            int nearby = level.getEntitiesOfClass(entity.getClass(), new AABB(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX() + 1, pPos.getY() + 1, pPos.getZ() + 1).inflate(this.spawnRange)).size();
                            if (nearby >= this.maxNearbyEntities) {
                                this.delay(level, pPos);
                                return;
                            }

                            entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), rand.nextFloat() * 360.0F, 0.0F);
                            if (entity instanceof Mob mob) {
                                if (!this.checkSpawnPositionSpawner(mob, useLiar ? liar : level, MobSpawnType.SPAWNER, spawnData, this)) {
                                    continue;
                                }

                                // Forge: Patch in FinalizeSpawn for spawners so it may be fired unconditionally, instead of only when vanilla normally would trigger it.
                                var event = net.minecraftforge.event.ForgeEventFactory.onFinalizeSpawnSpawner(mob, useLiar ? liar : level, level.getCurrentDifficultyAt(entity.blockPosition()), null, tag, this);
                                if (event != null && spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().contains("id", 8)) {
                                    ((Mob) entity).finalizeSpawn(useLiar ? liar : level, event.getDifficulty(), event.getSpawnType(), event.getSpawnData(), event.getSpawnTag());
                                }
                            }

                            if (!level.tryAddFreshEntityWithPassengers(entity)) {
                                this.delay(level, pPos);
                                return;
                            }

                            level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pPos, 0);
                            if (entity instanceof Mob) {
                                ((Mob) entity).spawnAnim();
                            }

                            flag = true;
                        }
                    }

                    if (flag) {
                        this.delay(level, pPos);
                    }

                }
            }
        }

        public boolean checkSpawnPositionSpawner(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, SpawnData spawnData, BaseSpawner spawner) {
            var event = new PositionCheck(mob, level, spawnType, null);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.getResult() == Result.DEFAULT) {
                return ApothSpawnerTile.this.ignoresConditions
                    || spawnData.getCustomSpawnRules().isPresent()
                    || mob.checkSpawnRules(level, MobSpawnType.SPAWNER) && mob.checkSpawnObstruction(level);
            }
            return event.getResult() == Result.ALLOW;
        }

        /**
         * Checks if the requested entity passes spawn rule checks or not.
         */
        private boolean checkSpawnRules(SpawnData spawnData, EntityType<?> entityType, ServerLevelAccessor pServerLevel, BlockPos blockpos) {
            if (spawnData.getCustomSpawnRules().isPresent()) {
                if (!entityType.getCategory().isFriendly() && pServerLevel.getDifficulty() == Difficulty.PEACEFUL) {
                    return false;
                }

                SpawnData.CustomSpawnRules customRules = spawnData.getCustomSpawnRules().get();
                if (ApothSpawnerTile.this.ignoresLight) return true; // All custom spawn rules are light-based, so if we ignore light, we can short-circuit here.
                if (!customRules.blockLightLimit().isValueInRange(pServerLevel.getBrightness(LightLayer.BLOCK, blockpos))
                    || !customRules.skyLightLimit().isValueInRange(pServerLevel.getBrightness(LightLayer.SKY, blockpos))) {
                    return false;
                }
            }
            else if (!SpawnPlacements.checkSpawnRules(entityType, pServerLevel, MobSpawnType.SPAWNER, blockpos, pServerLevel.getRandom())) {
                return false;
            }
            return true;
        }

    }

}
