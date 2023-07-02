package shadows.apotheosis.adventure.affix.socket.gem.bonus;

import java.util.Map;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.effect.DamageReductionAffix.DamageType;
import shadows.apotheosis.adventure.affix.socket.gem.GemClass;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

public class DamageReductionBonus extends GemBonus {

	protected final DamageType type;
	protected final Map<LootRarity, StepFunction> values;

	//Formatter::off
	public static Codec<DamageReductionBonus> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			gemClass(),
			DamageType.CODEC.fieldOf("damage_type").forGetter(a -> a.type),
			VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
			.apply(inst, DamageReductionBonus::new)
		);
	//Formatter::on

	public DamageReductionBonus(GemClass gemClass, DamageType type, Map<LootRarity, StepFunction> values) {
		super(Apotheosis.loc("damage_reduction"), gemClass);
		this.type = type;
		this.values = values;
	}

	@Override
	public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
		float level = this.values.get(rarity).get(0);
		return Component.translatable("affix.apotheosis:damage_reduction.desc", Component.translatable("misc.apotheosis." + this.type.getId()), Affix.fmt(100 * level)).withStyle(ChatFormatting.YELLOW);
	}

	@Override
	public GemBonus validate() {
		Preconditions.checkNotNull(this.type, "Invalid DamageReductionBonus with null type");
		Preconditions.checkNotNull(this.values, "Invalid DamageReductionBonus with null values");
		Preconditions.checkArgument(this.values.entrySet().stream().mapMulti((entry, consumer) -> {
			consumer.accept(entry.getKey());
			consumer.accept(entry.getValue());
		}).allMatch(Objects::nonNull), "Invalid DamageReductionBonus with invalid values");
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
