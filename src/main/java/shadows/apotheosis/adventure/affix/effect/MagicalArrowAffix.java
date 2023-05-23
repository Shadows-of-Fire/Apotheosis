package shadows.apotheosis.adventure.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.PSerializer;

public class MagicalArrowAffix extends Affix {

	//Formatter::off
	public static final Codec<MagicalArrowAffix> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			LootRarity.CODEC.fieldOf("min_rarity").forGetter(a -> a.minRarity))
			.apply(inst, MagicalArrowAffix::new)
		);
	//Formatter::on
	public static final PSerializer<MagicalArrowAffix> SERIALIZER = PSerializer.fromCodec("Magical Arrow Affix", CODEC);

	protected LootRarity minRarity;

	public MagicalArrowAffix(LootRarity minRarity) {
		super(AffixType.ABILITY);
		this.minRarity = minRarity;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack).isRanged() && rarity.isAtLeast(minRarity);
	}

	// EventPriority.HIGH
	public void onHurt(LivingHurtEvent e) {
		if (e.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
			if (AffixHelper.getAffixes(arrow).containsKey(this)) {
				e.getSource().setMagic();
			}
		}
	}

	@Override
	public PSerializer<? extends Affix> getSerializer() {
		return SERIALIZER;
	}

}
