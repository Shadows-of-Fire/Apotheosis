package shadows.apotheosis.adventure.affix.effect;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.util.StepFunction;

/**
 * When blocking an explosion, gain great power.
 */
public class CatalyzingAffix extends Affix {

	protected static final StepFunction LEVEL_FUNC = AffixHelper.step(200, 400, 1);

	public CatalyzingAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc").withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.SHIELD && rarity.isAtLeast(LootRarity.EPIC);
	}

	@Override
	public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
		if (source.isExplosion()) {
			int time = getTrueLevel(rarity, level) + (int) (amount * 4);
			int modifier = 1 + (int) (amount / 12);
			entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time, modifier));
		}

		return super.onShieldBlock(stack, rarity, level, entity, source, amount);
	}

	private static int getTrueLevel(LootRarity rarity, float level) {
		return (rarity.ordinal() - LootRarity.EPIC.ordinal()) * 200 + LEVEL_FUNC.getInt(level);
	}

}