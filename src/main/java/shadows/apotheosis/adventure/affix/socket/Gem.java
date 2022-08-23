package shadows.apotheosis.adventure.affix.socket;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import shadows.apotheosis.util.StepFunction;
import shadows.placebo.json.DimWeightedJsonReloadListener.IDimWeighted;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;

public final class Gem extends TypeKeyedBase<Gem> implements IDimWeighted {

	protected int weight;
	protected int variant;
	protected float quality;
	protected Attribute attribute;
	protected Operation operation;
	protected StepFunction value;
	protected Set<ResourceLocation> dimensions;

	Gem() {

	}

	public int getVariant() {
		return this.variant;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public Set<ResourceLocation> getDimensions() {
		return this.dimensions;
	}
}
