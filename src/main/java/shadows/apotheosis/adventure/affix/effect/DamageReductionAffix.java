package shadows.apotheosis.adventure.affix.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.util.StepFunction;

public class DamageReductionAffix extends Affix {

	private final DamageType type;
	private final Map<LootRarity, StepFunction> levelFuncs;

	public DamageReductionAffix(DamageType type, Map<LootRarity, StepFunction> levelFuncs) {
		super(AffixType.EFFECT);
		this.type = type;
		this.levelFuncs = levelFuncs;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.ARMOR && this.levelFuncs.containsKey(rarity);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix.apotheosis:damage_reduction.desc", new TranslatableComponent("misc.apotheosis." + this.type.id), fmt(100 * this.getTrueLevel(rarity, level))).withStyle(ChatFormatting.YELLOW));
	}

	public static void onHurt(LivingHurtEvent e) {
		DamageSource src = e.getSource();
		if (src.isBypassInvul() || src.isBypassMagic()) return;
		LivingEntity ent = e.getEntityLiving();
		float amount = e.getAmount();
		for (ItemStack s : ent.getArmorSlots()) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(s);
			for (AffixInstance inst : affixes.values()) {
				if (inst.affix() instanceof DamageReductionAffix dmg && dmg.type.test(src)) {
					amount *= 1 - dmg.getTrueLevel(inst.rarity(), inst.level());
				}
			}
		}
		e.setAmount(amount);
	}

	private float getTrueLevel(LootRarity rarity, float level) {
		return this.levelFuncs.get(rarity).get(level);
	}

	public static enum DamageType implements Predicate<DamageSource> {
		PHYSICAL("physical", d -> !d.isMagic() && !d.isFire() && !d.isExplosion()),
		MAGIC("magic", DamageSource::isMagic),
		FIRE("fire", DamageSource::isFire),
		FALL("fall", DamageSource::isFall),
		EXPLOSION("explosion", DamageSource::isExplosion);

		private final String id;
		private final Predicate<DamageSource> predicate;

		private DamageType(String id, Predicate<DamageSource> predicate) {
			this.id = id;
			this.predicate = predicate;
		}

		public String getId() {
			return this.id;
		}

		@Override
		public boolean test(DamageSource t) {
			return this.predicate.test(t);
		}
	}

	public static class Builder {

		private final DamageType type;
		private final Map<LootRarity, StepFunction> levels = new HashMap<>();

		public Builder(DamageType type) {
			this.type = type;
		}

		public Builder with(LootRarity rarity, StepFunction levelFunc) {
			this.levels.put(rarity, levelFunc);
			return this;
		}

		public DamageReductionAffix build(String id) {
			return (DamageReductionAffix) new DamageReductionAffix(this.type, this.levels).setRegistryName(id);
		}

	}

}
