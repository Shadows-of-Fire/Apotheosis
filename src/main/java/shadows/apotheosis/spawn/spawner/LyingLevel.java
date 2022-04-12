package shadows.apotheosis.spawn.spawner;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.ITickList;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;

public class LyingLevel implements IServerWorld {

	protected final World wrapped;
	protected int fakeLightLevel;

	public LyingLevel(World wrapped) {
		this.wrapped = wrapped;
	}

	public void setFakeLightLevel(int light) {
		this.fakeLightLevel = light;
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos pPos) {
		return wrapped.getCurrentDifficultyAt(pPos);
	}

	@Override
	public Random getRandom() {
		return wrapped.getRandom();
	}

	@Override
	public void levelEvent(PlayerEntity pPlayer, int pType, BlockPos pPos, int pData) {
		wrapped.levelEvent(pPlayer, pType, pPos, pData);
	}

	@Override
	public List<Entity> getEntities(Entity pEntity, AxisAlignedBB pArea, Predicate<? super Entity> pPredicate) {
		return wrapped.getEntities(pEntity, pArea, pPredicate);
	}

	@Override
	public List<? extends PlayerEntity> players() {
		return wrapped.players();
	}

	@Override
	public int getHeight(Type pHeightmapType, int pX, int pZ) {
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
	public WorldLightManager getLightEngine() {
		return wrapped.getLightEngine();
	}

	@Override
	public TileEntity getBlockEntity(BlockPos pPos) {
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
	public ServerWorld getLevel() {
		return (ServerWorld) this.wrapped;
	}

	@Override
	public float getBrightness(BlockPos pPos) {
		return this.fakeLightLevel;
	}

	@Override
	public int getBrightness(LightType pLightType, BlockPos pBlockPos) {
		return this.fakeLightLevel;
	}

	@Override
	public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
		return this.fakeLightLevel;
	}

	@Override
	public ITickList<Block> getBlockTicks() {
		return wrapped.getBlockTicks();
	}

	@Override
	public ITickList<Fluid> getLiquidTicks() {
		return wrapped.getLiquidTicks();
	}

	@Override
	public IWorldInfo getLevelData() {
		return wrapped.getLevelData();
	}

	@Override
	public AbstractChunkProvider getChunkSource() {
		return wrapped.getChunkSource();
	}

	@Override
	public void playSound(PlayerEntity pPlayer, BlockPos pPos, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
		wrapped.playSound(pPlayer, pPos, pSound, pCategory, pVolume, pPitch);
	}

	@Override
	public void addParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		wrapped.addParticle(pParticleData, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}

	@Override
	public DynamicRegistries registryAccess() {
		return wrapped.registryAccess();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> pClazz, AxisAlignedBB pArea, Predicate<? super T> pFilter) {
		return wrapped.getLoadedEntitiesOfClass(pClazz, pArea, pFilter);
	}

	@Override
	public IChunk getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
		return wrapped.getChunk(pX, pZ, pRequiredStatus, pNonnull);
	}

	@Override
	public Biome getUncachedNoiseBiome(int pX, int pY, int pZ) {
		return wrapped.getUncachedNoiseBiome(pX, pY, pZ);
	}

}
