package shadows.deadly.feature;
/*
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import shadows.deadly.DeadlyWorld;
import shadows.deadly.util.ChestBuilder;

public class RogueChest implements WorldFeature {
	/// The weights for each nest type.
	public static final int[] weights = WorldGenerator.getWeights(Properties.CHESTS, DeadlyWorld.CHESTS);
	/// The total weight for nest types.
	public static final int totalWeight = WorldGenerator.getTotalWeight(RogueChest.weights);

	/// The chance for this to appear in any given chunk. Determined by the properties file.
	public final double frequency;

	public RogueChest(double freq) {
		this.frequency = freq;
	}

	/// Attempts to generate this feature in the given chunk. Block position is given.
	@Override
	public void generate(World world, Random random, int x, int z) {
		if (this.frequency <= random.nextDouble()) return;
		x += random.nextInt(16);
		z += random.nextInt(16);
		int y = random.nextInt(50) + 11;
		for (byte state = 0; y > 5; y--) {
			if (world.isBlockNormalCubeDefault(x, y, z, true)) {
				if (state == 0) {
					if (this.canBePlaced(world, random, x, y + 1, z)) {
						this.place(world, random, x, y + 1, z);
						return;
					}
					state = -1;
				}
			} else {
				state = 0;
			}
		}
	}

	/// Returns true if this feature can be placed at the location.
	@Override
	public boolean canBePlaced(World world, Random random, int x, int y, int z) {
		return world.isAirBlock(x, y, z) && world.isAirBlock(x, y + 1, z);
	}

	/// Places this feature at the location.
	@Override
	public void place(World world, Random random, int x, int y, int z) {
		String type = WorldGenerator.choose(random, RogueChest.totalWeight, DeadlyWorld.CHESTS, RogueChest.weights);
		if (type == "valuable") {
			ChestBuilder.place(world, random, x, y, z, ChestBuilder.CHEST_VALUABLE);
		} else if (type == "trapped") {
			ChestBuilder.placeTrapped(world, random, x, y, z, ChestBuilder.getRogueChestByHeight(y));
			this.trapChest(world, random, x, y, z);
		} else {
			ChestBuilder.place(world, random, x, y, z, ChestBuilder.getRogueChestByHeight(y));
		}

		if (type == "normal") {
			// Do nothing
		} else if (type == "mine") {
			if (world.isBlockNormalCubeDefault(x, y - 2, z, false)) {
				Mine.item.place(world, random, x, y - 1, z);
			}
		} else if (type == "indie") {
			byte dir = (byte) random.nextInt(4);
			for (byte i = 4; i-- > 0;) {
				if (this.indieTrapChest(world, random, x, y, z, dir)) {
					break;
				}
				dir = (byte) Direction.rotateRight[dir];
			}
		}
	}

	/// Traps the chest at the given position.
	private void trapChest(World world, Random random, int x, int y, int z) {
		byte[] nextDir = { 4, 0, 1, 2, 3 };
		byte dir = (byte) random.nextInt(5);
		int X, Y, Z;
		y--;
		for (byte d = 5; d-- > 0;) {
			X = x;
			Y = y;
			Z = z;
			if (dir == 4) {
				Y--;
			} else {
				X += Direction.offsetX[dir];
				Z += Direction.offsetZ[dir];
			}
			if (this.canPlaceTNT(world, X, Y, Z)) {
				world.setBlock(X, Y, Z, Blocks.tnt, 0, 2);
				byte[] nextOffDir = dir == 4 ? new byte[] { 4, 0, 1, 2, 3 } : new byte[] { 3, 0, 1, 2 };
				byte offDir = (byte) random.nextInt(nextOffDir.length);
				int xi, yi, zi;
				for (byte i = (byte) nextOffDir.length; i-- > 0;) {
					xi = X;
					yi = Y;
					zi = Z;
					if (offDir == 4 || dir == Direction.rotateOpposite[offDir]) {
						Y--;
					} else {
						X += Direction.offsetX[offDir];
						Z += Direction.offsetZ[offDir];
					}
					if (this.canPlaceTNT(world, X, Y, Z)) {
						world.setBlock(X, Y, Z, Blocks.tnt, 0, 2);
						if (random.nextInt(3) != 0) {
							this.hideTNTnearChest(world, random, x, y, z);
							return;
						}
						offDir = (byte) random.nextInt(nextOffDir.length);
					}
					offDir = nextOffDir[offDir];
				}
			}
			dir = nextDir[dir];
		}
	}

	/// Generates an "indie" trap in the direction. Returns true if it was successful.
	private boolean indieTrapChest(World world, Random random, int x, int y, int z, byte dir) {
		if (!this.canPlaceTNT(world, x += Direction.offsetX[dir] << 1, y, z += Direction.offsetZ[dir] << 1) || world.getBlock(x, y, z).hasTileEntity(world.getBlockMetadata(x, y, z)) || !this.canPlaceTNT(world, x += Direction.offsetX[dir], y, z += Direction.offsetZ[dir])) return false;
		dir = (byte) Direction.rotateOpposite[dir];
		byte[] nextDir = { 4, 0, 1, 2, 3 };
		byte offDir = (byte) random.nextInt(5);
		int X, Y, Z;
		for (byte d = 5; d-- > 0;) {
			X = x;
			Y = y;
			Z = z;
			if (offDir == 4) {
				Y--;
			} else if (offDir == dir) {
				Y++;
			} else {
				X += Direction.offsetX[offDir];
				Z += Direction.offsetZ[offDir];
			}
			if (this.canPlaceTNT(world, X, Y, Z)) {
				world.setBlock(X, Y, Z, Blocks.tnt, 0, 2);
				world.setBlock(x, y, z, Blocks.tnt, 0, 2);
				world.setBlock(x + Direction.offsetX[dir], y, z + Direction.offsetZ[dir], Blocks.unpowered_comparator, dir, 2);
				this.hideTNTnearChest(world, random, x + 3 * Direction.offsetX[dir], y, z + 3 * Direction.offsetZ[dir]);
				return true;
			}
			offDir = nextDir[offDir];
		}
		return false;
	}

	/// Places hidden TNT near the given location.
	private void hideTNTnearChest(World world, Random random, int x, int y, int z) {
		int X, Y, Z;
		for (byte i = 4; i-- > 0;) {
			X = x + (random.nextInt(3) - 1 << 1);
			Y = y + (Math.min(1, random.nextInt(3)) << 1);
			Z = z + (random.nextInt(3) - 1 << 1);
			if (world.isBlockNormalCubeDefault(X, Y, Z, false) && this.canPlaceTNT(world, X, Y, Z)) {
				world.setBlock(X, Y, Z, Blocks.tnt, 0, 2);
			}
		}
	}

	/// Returns true if all surrounding blocks are solid.
	private boolean canPlaceTNT(World world, int x, int y, int z) {
		return world.isBlockNormalCubeDefault(x - 1, y, z, false) && world.isBlockNormalCubeDefault(x + 1, y, z, false) && world.isBlockNormalCubeDefault(x, y - 1, z, false) && world.isBlockNormalCubeDefault(x, y + 1, z, false) && world.isBlockNormalCubeDefault(x, y, z - 1, false) && world.isBlockNormalCubeDefault(x, y, z + 1, false);
	}
}*/