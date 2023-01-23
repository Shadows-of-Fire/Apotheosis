package shadows.apotheosis.adventure.affix.socket.gem.bonus;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.socket.gem.GemClass;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.util.StepFunction;

public class AttributeBonus extends GemBonus {

	//Formatter::off
	public static Codec<AttributeBonus> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			GemClass.CODEC.fieldOf("gem_class").forGetter(a -> a.gemClass),
			ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(a -> a.attribute),
			new EnumCodec<>(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
			VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
			.apply(inst, AttributeBonus::new)
		);
	//Formatter::on

	protected final Attribute attribute;
	protected final Operation operation;
	protected final Map<LootRarity, StepFunction> values;

	public AttributeBonus(GemClass gemClass, Attribute attr, Operation op, Map<LootRarity, StepFunction> values) {
		super(Apotheosis.loc("attribute"), gemClass);
		this.attribute = attr;
		this.operation = op;
		this.values = values;
	}

	@Override
	public void addModifiers(ItemStack gem, LootRarity rarity, int facets, BiConsumer<Attribute, AttributeModifier> map) {
		map.accept(this.attribute, read(gem, rarity, facets));
	}

	@Override
	public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity, int facets) {
		return GemItem.toComponent(this.attribute, read(gem, rarity, facets));
	}

	@Override
	public int getMaxFacets(ItemStack gem, LootRarity rarity) {
		return this.values.get(rarity).steps();
	}

	@Override
	public AttributeBonus validate() {
		gemClass.validate();
		Preconditions.checkNotNull(this.attribute, "Invalid AttributeBonus with null attribute");
		Preconditions.checkNotNull(this.operation, "Invalid AttributeBonus with null operation");
		Preconditions.checkNotNull(this.values, "Invalid AttributeBonus with null values");
		return this;
	}

	@Override
	public boolean supports(LootRarity rarity) {
		return this.values.containsKey(rarity);
	}

	public AttributeModifier read(ItemStack gem, LootRarity rarity, int facets) {
		return new AttributeModifier(GemItem.getUUIDs(gem).get(0), "apoth.gem_modifier", this.values.get(rarity).getForStep(facets), this.operation);
	}

}
