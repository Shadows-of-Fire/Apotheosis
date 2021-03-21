package shadows.apotheosis.util;

import net.minecraft.util.IntArray;
import net.minecraft.util.math.MathHelper;

/**
 * A thing that allows floats to be synced over the container system.
 * Retains up to two decimal places when synced.
 */
public class FloatReferenceHolder {

	boolean updating = false;

	float internal = 0;
	final float min, max;

	IntArray array = new IntArray(3) {
		@Override
		public void set(int index, int value) {
			super.set(index, value);
			if (!FloatReferenceHolder.this.updating) FloatReferenceHolder.this.updateFromArray();
		};
	};

	public FloatReferenceHolder(float def, float min, float max) {
		this.set(def);
		this.min = min;
		this.max = max;
	}

	/**
	 * Returns the internal array object so the container may register it for tracking.
	 */
	public IntArray getArray() {
		return this.array;
	}

	public float get() {
		return this.internal;
	}

	public void set(float f) {
		f = MathHelper.clamp(f, this.min, this.max);
		this.internal = f;
		this.updating = true;
		this.array.set(0, (int) f);
		this.array.set(1, (int) (f * 10) % 10);
		this.array.set(2, (int) (f * 100) % 10);
		this.updating = false;
	}

	private void updateFromArray() {
		this.internal = this.array.get(0) + this.array.get(1) / 10F + this.array.get(2) / 100F;
	}

	public float getMax() {
		return this.max;
	}

	public float getMin() {
		return this.min;
	}

}