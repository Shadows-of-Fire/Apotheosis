package shadows.apotheosis.adventure.affix.socket.gem.bonus;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.socket.gem.GemClass;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

public class DurabilityBonus extends GemBonus {

	//Formatter::off
	public static Codec<DurabilityBonus> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			gemClass(),
			VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
			.apply(inst, DurabilityBonus::new)
		);
	//Formatter::on

	protected final Map<LootRarity, StepFunction> values;

	public DurabilityBonus(GemClass gemClass, Map<LootRarity, StepFunction> values) {
		super(Apotheosis.loc("durability"), gemClass);
		this.values = values;
	}

	@Override
	public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity, int facets) {
		float level = this.values.get(rarity).getForStep(facets);
		return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(100 * level)).withStyle(ChatFormatting.YELLOW);
	}

	@Override
	public int getMaxFacets(LootRarity rarity) {
		return this.values.get(rarity).steps();
	}

	@Override
	public GemBonus validate() {
		Preconditions.checkNotNull(this.values, "Invalid AttributeBonus with null values");
		return this;
	}

	@Override
	public boolean supports(LootRarity rarity) {
		return this.values.containsKey(rarity);
	}

	@Override
	public int getNumberOfUUIDs() {
		return 0;
	}

	@Override
	public Codec<? extends GemBonus> getCodec() {
		return CODEC;
	}

}
