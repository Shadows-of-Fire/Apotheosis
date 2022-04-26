package shadows.apotheosis.util;

import com.google.gson.annotations.Expose;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;

public class Weighted implements WeightedEntry {

	@Expose(deserialize = false)
	private Weight _weight;

	protected final int weight;

	public Weighted(int pWeight) {
		this.weight = pWeight;
	}

	@Override
	public Weight getWeight() {
		if (this._weight == null) this._weight = Weight.of(this.weight);
		return this._weight;
	}
}
