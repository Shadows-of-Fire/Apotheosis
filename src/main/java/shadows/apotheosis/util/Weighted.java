package shadows.apotheosis.util;

import com.google.gson.annotations.Expose;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;

public class Weighted implements WeightedEntry {

	@Expose(deserialize = false)
	private Weight _weight;

	public final int weight;

	public Weighted(int pWeight) {
		this.weight = pWeight;
	}

	public Weight getWeight() {
		if (_weight == null) _weight = Weight.of(weight);
		return _weight;
	}
}
