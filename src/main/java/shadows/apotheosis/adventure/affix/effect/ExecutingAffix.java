package shadows.apotheosis.adventure.affix.effect;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.util.StepFunction;

public class ExecutingAffix extends Affix {

	protected static final StepFunction LEVEL_FUNC = AffixHelper.step(0.05F, 5, 0.01F);

	public ExecutingAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.HEAVY_WEAPON && rarity.isAtLeast(LootRarity.EPIC);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(100 * getTrueLevel(rarity, level))).withStyle(ChatFormatting.YELLOW));
	}

	private static float getTrueLevel(LootRarity rarity, float level) {
		return (rarity.ordinal() - LootRarity.EPIC.ordinal()) * 0.05F + LEVEL_FUNC.get(level);
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
		float threshold = getTrueLevel(rarity, level);
		if (target instanceof LivingEntity living && !living.level.isClientSide) {
			if (living.getHealth() / living.getMaxHealth() < threshold) {
				living.die(new EntityDamageSource("apotheosis.execute", user).bypassInvul().bypassArmor().bypassMagic());
			}
		}
	}

}
