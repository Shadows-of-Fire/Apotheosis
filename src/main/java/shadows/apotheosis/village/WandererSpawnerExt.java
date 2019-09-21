package shadows.apotheosis.village;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.passive.horse.TraderLlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WanderingTraderSpawner;
import net.minecraft.world.storage.WorldInfo;

public class WandererSpawnerExt extends WanderingTraderSpawner {

	public static int defaultDelay = 24000;
	public static int defaultChance = 10;

	protected final Random random = new Random();
	protected final ServerWorld world;
	protected int delay;
	protected int spawnDelay;
	protected int spawnChance;

	public WandererSpawnerExt(ServerWorld world) {
		super(world);
		this.world = world;
		this.delay = 1200;
		WorldInfo worldinfo = world.getWorldInfo();
		this.spawnDelay = worldinfo.getWanderingTraderSpawnDelay();
		this.spawnChance = worldinfo.getWanderingTraderSpawnChance();
		if (this.spawnDelay == 0 && this.spawnChance == 0) {
			this.spawnDelay = defaultDelay;
			worldinfo.setWanderingTraderSpawnDelay(this.spawnDelay);
			this.spawnChance = defaultChance;
			worldinfo.setWanderingTraderSpawnChance(this.spawnChance);
		}
	}

	@Override
	public void tick() {
		if (--this.delay <= 0) {
			this.delay = 1200;
			WorldInfo worldinfo = this.world.getWorldInfo();
			this.spawnDelay -= 1200;
			worldinfo.setWanderingTraderSpawnDelay(this.spawnDelay);
			if (this.spawnDelay <= 0) {
				this.spawnDelay = defaultDelay;
				if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
					int i = this.spawnChance;
					this.spawnChance = MathHelper.clamp(this.spawnChance + defaultChance, defaultChance, 100);
					worldinfo.setWanderingTraderSpawnChance(this.spawnChance);
					if (this.random.nextInt(100) <= i) {
						if (this.spawnTrader()) {
							this.spawnChance = defaultChance;
						}
					}
				}
			}
		}
	}

	protected boolean spawnTrader() {
		PlayerEntity player = this.world.getRandomPlayer();
		if (player == null) return false;

		BlockPos pos = player.getPosition();
		PointOfInterestManager poiMgr = this.world.func_217443_B();
		Optional<BlockPos> optional = poiMgr.func_219127_a(PointOfInterestType.MEETING.func_221045_c(), Predicates.alwaysTrue(), pos, 48, PointOfInterestManager.Status.ANY);
		BlockPos poiPos = optional.orElse(pos);
		BlockPos spawnPos = this.findSpawnPoint(poiPos, 48);
		if (spawnPos != null) {
			if (this.world.getBiome(spawnPos) == Biomes.THE_VOID) return false;

			WanderingTraderEntity wanderingtraderentity = EntityType.WANDERING_TRADER.spawn(this.world, null, null, null, spawnPos, SpawnReason.EVENT, false, false);
			if (wanderingtraderentity != null) {
				for (int j = 0; j < 2; ++j) {
					this.spawnLlamas(wanderingtraderentity, 4);
				}

				this.world.getWorldInfo().setWanderingTraderId(wanderingtraderentity.getUniqueID());
				wanderingtraderentity.func_213728_s(48000);
				wanderingtraderentity.func_213726_g(poiPos);
				wanderingtraderentity.setHomePosAndDistance(poiPos, 16);
				return true;
			}
		}

		return false;
	}

	protected void spawnLlamas(WanderingTraderEntity p_221243_1_, int p_221243_2_) {
		BlockPos blockpos = this.findSpawnPoint(new BlockPos(p_221243_1_), p_221243_2_);
		if (blockpos != null) {
			TraderLlamaEntity traderllamaentity = EntityType.TRADER_LLAMA.spawn(this.world, (CompoundNBT) null, (ITextComponent) null, (PlayerEntity) null, blockpos, SpawnReason.EVENT, false, false);
			if (traderllamaentity != null) {
				traderllamaentity.setLeashHolder(p_221243_1_, true);
			}
		}
	}

	static AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 2, 1);

	@Nullable
	protected BlockPos findSpawnPoint(BlockPos pos, int radius) {
		for (int i = 0; i < 10; ++i) {
			int j = pos.getX() + this.random.nextInt(radius * 2) - radius;
			int k = pos.getZ() + this.random.nextInt(radius * 2) - radius;
			int l = this.world.getHeight(Heightmap.Type.WORLD_SURFACE, j, k);
			BlockPos spawnPos = new BlockPos(j, l, k);
			if (world.getBlockState(spawnPos).getBlock().canSpawnInBlock() || world.areCollisionShapesEmpty(aabb.offset(spawnPos))) {
				return spawnPos;
			}
		}
		return null;
	}

}
