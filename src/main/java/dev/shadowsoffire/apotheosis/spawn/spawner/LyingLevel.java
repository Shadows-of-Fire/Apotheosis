package dev.shadowsoffire.apotheosis.spawn.spawner;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;

public class LyingLevel implements WorldGenLevel {

    protected final ServerLevel wrapped;
    protected int fakeLightLevel;

    public LyingLevel(ServerLevel wrapped) {
        this.wrapped = wrapped;
    }

    public void setFakeLightLevel(int light) {
        this.fakeLightLevel = light;
    }

    @Override
    public long nextSubTickCount() {
        return this.wrapped.nextSubTickCount();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return this.wrapped.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return this.wrapped.getFluidTicks();
    }

    @Override
    public LevelData getLevelData() {
        return this.wrapped.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pPos) {
        return this.wrapped.getCurrentDifficultyAt(pPos);
    }

    @Override
    public MinecraftServer getServer() {
        return this.wrapped.getServer();
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.wrapped.getChunkSource();
    }

    @Override
    public RandomSource getRandom() {
        return this.wrapped.getRandom();
    }

    @Override
    public void playSound(Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch) {
        this.wrapped.playSound(pPlayer, pPos, pSound, pCategory, pVolume, pPitch);
    }

    @Override
    public void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        this.wrapped.addParticle(pParticleData, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
    }

    @Override
    public void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData) {
        this.wrapped.levelEvent(pPlayer, pType, pPos, pData);
    }

    @Override
    public void gameEvent(Entity pEntity, GameEvent pEvent, BlockPos pPos) {
        this.wrapped.gameEvent(pEntity, pEvent, pPos);
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.wrapped.registryAccess();
    }

    @Override
    public List<Entity> getEntities(Entity pEntity, AABB pArea, Predicate<? super Entity> pPredicate) {
        return this.wrapped.getEntities(pEntity, pArea, pPredicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntityTypeTest, AABB pArea, Predicate<? super T> pPredicate) {
        return this.wrapped.getEntities(pEntityTypeTest, pArea, pPredicate);
    }

    @Override
    public List<? extends Player> players() {
        return this.wrapped.players();
    }

    @Override
    public ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
        return this.wrapped.getChunk(pX, pZ, pRequiredStatus, pNonnull);
    }

    @Override
    public int getHeight(Types pHeightmapType, int pX, int pZ) {
        return this.wrapped.getHeight(pHeightmapType, pX, pZ);
    }

    @Override
    public int getSkyDarken() {
        return this.wrapped.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.wrapped.getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
        return this.wrapped.getUncachedNoiseBiome(pX, pY, pZ);
    }

    @Override
    public boolean isClientSide() {
        return this.wrapped.isClientSide(); // Should always be false, but whatever
    }

    @Override
    public int getSeaLevel() {
        return this.wrapped.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return this.wrapped.dimensionType();
    }

    @Override
    public float getShade(Direction pDirection, boolean pShade) {
        return this.wrapped.getShade(pDirection, pShade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.wrapped.getLightEngine();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pPos) {
        return this.wrapped.getBlockEntity(pPos);
    }

    @Override
    public BlockState getBlockState(BlockPos p_45571_) {
        return this.wrapped.getBlockState(p_45571_);
    }

    @Override
    public FluidState getFluidState(BlockPos pPos) {
        return this.wrapped.getFluidState(pPos);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.wrapped.getWorldBorder();
    }

    @Override
    public boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState) {
        return this.wrapped.isStateAtPosition(pPos, pState);
    }

    @Override
    public boolean isFluidAtPosition(BlockPos pPos, Predicate<FluidState> pPredicate) {
        return this.wrapped.isFluidAtPosition(pPos, pPredicate);
    }

    @Override
    public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
        return this.wrapped.setBlock(pPos, pState, pFlags, pRecursionLeft);
    }

    @Override
    public boolean removeBlock(BlockPos pPos, boolean pIsMoving) {
        return this.wrapped.removeBlock(pPos, pIsMoving);
    }

    @Override
    public boolean destroyBlock(BlockPos pPos, boolean pDropBlock, Entity pEntity, int pRecursionLeft) {
        return this.wrapped.destroyBlock(pPos, pDropBlock, pEntity, pRecursionLeft);
    }

    @Override
    public ServerLevel getLevel() {
        return this.wrapped;
    }

    @Override
    public int getBrightness(LightLayer pLightType, BlockPos pBlockPos) {
        return this.fakeLightLevel;
    }

    @Override
    public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
        return this.fakeLightLevel;
    }

    @Override
    public long getSeed() {
        return this.wrapped.getSeed();
    }

    @Override
    public void gameEvent(GameEvent pEvent, Vec3 pPosition, Context pContext) {
        this.wrapped.gameEvent(pEvent, pPosition, pContext);
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return this.wrapped.enabledFeatures();
    }

    @Override
    public boolean ensureCanWrite(BlockPos pPos) {
        return this.wrapped.ensureCanWrite(pPos);
    }

    @Override
    public void setCurrentlyGenerating(Supplier<String> pCurrentlyGenerating) {
        this.wrapped.setCurrentlyGenerating(pCurrentlyGenerating);
    }

    @Override
    public void addFreshEntityWithPassengers(Entity pEntity) {
        this.wrapped.addFreshEntityWithPassengers(pEntity);
    }

    @Override
    public void blockUpdated(BlockPos pPos, Block pBlock) {
        this.wrapped.blockUpdated(pPos, pBlock);
    }

    @Override
    public boolean addFreshEntity(Entity pEntity) {
        return this.wrapped.addFreshEntity(pEntity);
    }

}
