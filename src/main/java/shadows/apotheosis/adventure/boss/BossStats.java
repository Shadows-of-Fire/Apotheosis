package shadows.apotheosis.adventure.boss;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import shadows.apotheosis.util.ChancedEffectInstance;
import shadows.placebo.json.RandomAttributeModifier;

public class BossStats {

	@SerializedName("enchant_chance")
	protected float enchantChance;

	@SerializedName("enchantment_levels")
	protected int[] enchLevels;

	protected List<ChancedEffectInstance> effects;

	@SerializedName("attribute_modifiers")
	protected List<RandomAttributeModifier> modifiers;

}
