package shadows.apotheosis.adventure.affix.effect;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

/**
 * When blocking an arrow, hurt the shooter.
 */
public class PsychicAffix extends Affix {

	protected static final Float2FloatFunction LEVEL_FUNC = AffixHelper.step(0.2F, 40, 0.01F);

	public PsychicAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc", fmt(100 * getTrueLevel(rarity, level))).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.SHIELD;
	}

	@Override
	public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
		if (source.getDirectEntity() instanceof Projectile arrow) {
			Entity owner = arrow.getOwner();
			if (owner instanceof LivingEntity living) {
				living.hurt(new EntityDamageSource("player", entity).setMagic(), amount * getTrueLevel(rarity, level));
			}
		}

		return super.onShieldBlock(stack, rarity, level, entity, source, amount);
	}

	private static float getTrueLevel(LootRarity rarity, float level) {
		return (rarity.ordinal() - LootRarity.RARE.ordinal()) * 0.125F + LEVEL_FUNC.get(level);
	}

}