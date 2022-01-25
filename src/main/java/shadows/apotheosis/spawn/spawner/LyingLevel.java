package shadows.apotheosis.spawn.spawner;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
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
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.LevelTickAccess;

public class LyingLevel implements ServerLevelAccessor {

	protected final Level wrapped;
	protected int fakeLightLevel;

	public LyingLevel(Level wrapped) {
		this.wrapped = wrapped;
	}

	public void setFakeLightLevel(int light) {
		this.fakeLightLevel = light;
	}

	@Override
	public long nextSubTickCount() {
		return wrapped.nextSubTickCount();
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return wrapped.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return wrapped.getFluidTicks();
	}

	@Override
	public LevelData getLevelData() {
		return wrapped.getLevelData();
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos pPos) {
		return wrapped.getCurrentDifficultyAt(pPos);
	}

	@Override
	public MinecraftServer getServer() {
		return wrapped.getServer();
	}

	@Override
	public ChunkSource getChunkSource() {
		return wrapped.getChunkSource();
	}

	@Override
	public Random getRandom() {
		return wrapped.getRandom();
	}

	@Override
	public void playSound(Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch) {
		wrapped.playSound(pPlayer, pPos, pSound, pCategory, pVolume, pPitch);
	}

	@Override
	public void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		wrapped.addParticle(pParticleData, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}

	@Override
	public void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData) {
		wrapped.levelEvent(pPlayer, pType, pPos, pData);
	}

	@Override
	public void gameEvent(Entity pEntity, GameEvent pEvent, BlockPos pPos) {
		wrapped.gameEvent(pEntity, pEvent, pPos);
	}

	@Override
	public RegistryAccess registryAccess() {
		return wrapped.registryAccess();
	}

	@Override
	public List<Entity> getEntities(Entity pEntity, AABB pArea, Predicate<? super Entity> pPredicate) {
		return wrapped.getEntities(pEntity, pArea, pPredicate);
	}

	@Override
	public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntityTypeTest, AABB pArea, Predicate<? super T> pPredicate) {
		return wrapped.getEntities(pEntityTypeTest, pArea, pPredicate);
	}

	@Override
	public List<? extends Player> players() {
		return wrapped.players();
	}

	@Override
	public ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
		return wrapped.getChunk(pX, pZ, pRequiredStatus, pNonnull);
	}

	@Override
	public int getHeight(Types pHeightmapType, int pX, int pZ) {
		return wrapped.getHeight(pHeightmapType, pX, pZ);
	}

	@Override
	public int getSkyDarken() {
		return wrapped.getSkyDarken();
	}

	@Override
	public BiomeManager getBiomeManager() {
		return wrapped.getBiomeManager();
	}

	@Override
	public Biome getUncachedNoiseBiome(int pX, int pY, int pZ) {
		return wrapped.getUncachedNoiseBiome(pX, pY, pZ);
	}

	@Override
	public boolean isClientSide() {
		return wrapped.isClientSide(); //Should always be false, but whatever
	}

	@Override
	public int getSeaLevel() {
		return wrapped.getSeaLevel();
	}

	@Override
	public DimensionType dimensionType() {
		return wrapped.dimensionType();
	}

	@Override
	public float getShade(Direction pDirection, boolean pShade) {
		return wrapped.getShade(pDirection, pShade);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return wrapped.getLightEngine();
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pPos) {
		return wrapped.getBlockEntity(pPos);
	}

	@Override
	public BlockState getBlockState(BlockPos p_45571_) {
		return wrapped.getBlockState(p_45571_);
	}

	@Override
	public FluidState getFluidState(BlockPos pPos) {
		return wrapped.getFluidState(pPos);
	}

	@Override
	public WorldBorder getWorldBorder() {
		return wrapped.getWorldBorder();
	}

	@Override
	public boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState) {
		return wrapped.isStateAtPosition(pPos, pState);
	}

	@Override
	public boolean isFluidAtPosition(BlockPos pPos, Predicate<FluidState> pPredicate) {
		return wrapped.isFluidAtPosition(pPos, pPredicate);
	}

	@Override
	public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
		return wrapped.setBlock(pPos, pState, pFlags, pRecursionLeft);
	}

	@Override
	public boolean removeBlock(BlockPos pPos, boolean pIsMoving) {
		return wrapped.removeBlock(pPos, pIsMoving);
	}

	@Override
	public boolean destroyBlock(BlockPos pPos, boolean pDropBlock, Entity pEntity, int pRecursionLeft) {
		return wrapped.destroyBlock(pPos, pDropBlock, pEntity, pRecursionLeft);
	}

	@Override
	public ServerLevel getLevel() {
		return (ServerLevel) this.wrapped;
	}

	@Override
	public float getBrightness(BlockPos pPos) {
		return this.fakeLightLevel;
	}

	@Override
	public int getBrightness(LightLayer pLightType, BlockPos pBlockPos) {
		return this.fakeLightLevel;
	}

}
