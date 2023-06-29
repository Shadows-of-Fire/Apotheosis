package shadows.apotheosis.adventure.affix.socket.gem.bonus;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.socket.gem.GemClass;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.util.StepFunction;

public class MultiAttrBonus extends GemBonus {

	//Formatter::off
	public static Codec<MultiAttrBonus> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			gemClass(),
			ModifierInst.CODEC.listOf().fieldOf("modifiers").forGetter(a -> a.modifiers),
			Codec.STRING.fieldOf("desc").forGetter(a -> a.desc))
			.apply(inst, MultiAttrBonus::new)
		);
	//Formatter::on

	protected final List<ModifierInst> modifiers;
	protected final String desc;

	public MultiAttrBonus(GemClass gemClass, List<ModifierInst> modifiers, String desc) {
		super(Apotheosis.loc("multi_attribute"), gemClass);
		this.modifiers = modifiers;
		this.desc = desc;
	}

	@Override
	public void addModifiers(ItemStack gem, LootRarity rarity, BiConsumer<Attribute, AttributeModifier> map) {
		List<UUID> uuids = GemItem.getUUIDs(gem);
		int i = 0;
		for (ModifierInst modifier : modifiers) {
			map.accept(modifier.attr, modifier.build(uuids.get(i++), rarity));
		}
	}

	@Override
	public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
		Object[] values = new Object[modifiers.size()];
		int i = 0;
		for (ModifierInst modif : modifiers) {
			values[i++] = Affix.fmt(modif.values.get(rarity).get(0));
		}
		return Component.translatable(this.desc, values);
	}

	@Override
	public int getMaxFacets(LootRarity rarity) {
		return this.modifiers.stream().mapToInt(m -> m.values.get(rarity).steps()).max().orElse(0);
	}

	@Override
	public MultiAttrBonus validate() {
		Preconditions.checkNotNull(this.modifiers, "Invalid AttributeBonus with null values");
		return this;
	}

	@Override
	public boolean supports(LootRarity rarity) {
		return this.modifiers.get(0).values.containsKey(rarity);
	}

	@Override
	public int getNumberOfUUIDs() {
		return this.modifiers.size();
	}

	@Override
	public Codec<? extends GemBonus> getCodec() {
		return CODEC;
	}

	protected static record ModifierInst(Attribute attr, Operation op, Map<LootRarity, StepFunction> values) {

		//Formatter::off
		public static Codec<ModifierInst> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
				ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(ModifierInst::attr),
				new EnumCodec<>(Operation.class).fieldOf("operation").forGetter(ModifierInst::op),
				VALUES_CODEC.fieldOf("values").forGetter(ModifierInst::values))
				.apply(inst, ModifierInst::new)
			);
		//Formatter::on

		public AttributeModifier build(UUID id, LootRarity rarity) {
			return new AttributeModifier(id, "apoth.gem_modifier", this.values.get(rarity).get(0), this.op);
		}

	}

}
