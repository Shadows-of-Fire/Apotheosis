package shadows.apotheosis.util;

import java.util.Random;

/**
 * A class that helps to generate random values within a range.
 */
public class IntValueRange {

	public static final IntValueRange ZERO = new IntValueRange(0, 1) {
		@Override
		public int getRandomValue(Random rand) {
			return 0;
		}
	};

	private final int min;
	private final int max;

	/**
	 * Creates a range.
	 * @param min Min value, inclusive.
	 * @param max Max value, exclusive.
	 */
	public IntValueRange(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}

	public int getRandomValue(Random rand) {
		return this.min + rand.nextInt(this.max - this.min);
	}
}
