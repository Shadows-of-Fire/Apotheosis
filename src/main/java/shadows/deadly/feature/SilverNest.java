package shadows.deadly.feature;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import shadows.deadly.config.DeadlyConfig;
import shadows.deadly.util.ChestBuilder;
import shadows.deadly.util.DeadlyConstants;
import shadows.deadly.util.TagBuilder;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.SpawnerBuilder;

/**
 * Generates silverfish nests.  Spawner surrounded in monster egg cobble, with loot below.
 * @author Shadows
 *
 */
public class SilverNest extends WorldFeature {

	private static SpawnerItem defaultSpawner;
	private static SpawnerItem angrySpawner;
	private static SpawnerItem surpriseSpawner;

	public static final List<NestItem> NEST_ITEMS = new ArrayList<>();

	@Override
	public void generate(World world, BlockPos pos) {
		if (world.rand.nextFloat() <= DeadlyConfig.silverfishNestChance) {
			int x = pos.getX() + MathHelper.getInt(world.rand, 6, 9);
			int z = pos.getZ() + MathHelper.getInt(world.rand, 6, 9);
			int y = 50 + MathHelper.getInt(world.rand, -20, 20);
			MutableBlockPos mPos = new MutableBlockPos();
			IntList yValues = new IntArrayList();
			for (byte state = 3; y > 5; y--) {
				if (world.isBlockNormalCube(mPos.setPos(x, y, z), true)) {
					if (state <= 0) {
						if (this.canBePlaced(world, mPos.setPos(x, y + 1, z))) {
							yValues.add(y + 1);
						}
						state = 3;
					}
				} else {
					state--;
				}
			}
			if (yValues.size() > 0) {
				this.place(world, new BlockPos(x, yValues.get(world.rand.nextInt(yValues.size())), z));
			}
		}
	}

	@Override
	public boolean canBePlaced(World world, BlockPos pos) {
		return world.isAirBlock(pos);
	}

	@Override
	public void place(World world, BlockPos pos) {
		for (int x1 = -1; x1 <= 1; x1++) {
			for (int y1 = -2; y1 <= 1; y1++) {
				for (int z1 = -1; z1 <= 1; z1++) {
					if (x1 == 0 || z1 == 0 || y1 == -1 || y1 == 0) if (x1 != 0 || z1 != 0 || y1 < -1 || y1 > 0) {
						PlaceboUtil.setBlockWithMeta(world, pos.add(x1, y1, z1), Blocks.MONSTER_EGG, 1, 2);
					}
				}
			}
		}

		if (world.rand.nextDouble() < DeadlyConfig.nestAngerChance) angrySpawner.place(world, pos);
		else defaultSpawner.place(world, pos);

		NestItem item = WeightedRandom.getRandomItem(world.rand, NEST_ITEMS);
		if (item.type == NestType.SURPRISE) {
			surpriseSpawner.place(world, pos.down());
		} else if (item.type == NestType.CHEST) {
			ChestBuilder.place(world, world.rand, pos.down(), ChestBuilder.SILVER_NEST);
		} else world.setBlockState(pos.down(), item.state, 2);
	}

	public static void init() {
		SpawnerBuilder sb = new SpawnerBuilder();
		sb.setType(EntitySilverfish.class);
		DeadlyConstants.NEST_SPAWNER_STATS.apply(sb);
		defaultSpawner = new SpawnerItem(sb, 0);

		sb = new SpawnerBuilder();
		sb.setType(EntitySilverfish.class);
		DeadlyConstants.NEST_SPAWNER_STATS.apply(sb);
		NBTTagCompound data = TagBuilder.getDefaultTag(EntitySilverfish.class);
		TagBuilder.addPotionEffect(data, MobEffects.POISON, 0);
		TagBuilder.addPotionEffect(data, MobEffects.REGENERATION, 1);
		TagBuilder.addPotionEffect(data, MobEffects.ABSORPTION, 0);
		TagBuilder.addPotionEffect(data, MobEffects.RESISTANCE, 2);
		sb.setSpawnData(data);
		angrySpawner = new SpawnerItem(sb, 0);

		sb = TagBuilder.createTNTSpawner();
		sb.setPlayerRange(3);
		surpriseSpawner = new SpawnerItem(sb, 0);
		for (NestType t : NestType.values()) {
			int weight = DeadlyConfig.config.getInt("Nest Type Weight: " + t.toString(), DeadlyConstants.NESTS, t.defWeight, 0, 50, "The weight for this nest type.  0 to disable.");
			if (weight != 0) NEST_ITEMS.add(new NestItem(t, t.state, weight));
		}
	}

	public static enum NestType {
		REDSTONE(Blocks.REDSTONE_ORE.getDefaultState(), 4),
		LAPIS(Blocks.LAPIS_ORE.getDefaultState(), 2),
		GOLD(Blocks.GOLD_ORE.getDefaultState(), 2),
		EMERALD(Blocks.EMERALD_ORE.getDefaultState(), 1),
		DIAMOND(Blocks.DIAMOND_ORE.getDefaultState(), 1),
		CHEST(Blocks.CHEST.getDefaultState(), 2),
		SURPRISE(null, 1),
		PARTY(Blocks.CAKE.getDefaultState(), 1);

		IBlockState state;
		int defWeight;

		NestType(IBlockState state, int defWeight) {
			this.state = state;
			this.defWeight = defWeight;
		}
	}

	public static class NestItem extends WeightedRandom.Item {

		NestType type;
		IBlockState state;

		public NestItem(NestType type, IBlockState state, int weight) {
			super(weight);
			this.type = type;
			this.state = state;
		}

	}

	@Override
	public boolean isEnabled() {
		return DeadlyConfig.silverfishNestChance > 0;
	}

}