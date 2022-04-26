package shadows.apotheosis.deadly.affix.impl.heavy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Targets below a certain percent HP threshold are instantly killed.
 */
public class ExecuteAffix extends RangedAffix {

	private static final DamageSource EXECUTION = new DamageSource("apoth.execute").bypassInvul().bypassMagic();

	public ExecuteAffix(LootRarity rarity, float min, float max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory == LootCategory.HEAVY_WEAPON;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (target instanceof LivingEntity living) {
			if (living.getHealth() / living.getMaxHealth() < level) {
				living.hurt(EXECUTION, Float.MAX_VALUE);
			}
		}
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		return super.generateLevel(stack, rand, modifier);
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", fmt(level * 100)));
	}

	@Override
	public Component getDisplayName(float level) {
		return new TranslatableComponent("affix." + this.getRegistryName() + ".name", fmt(level * 100)).withStyle(ChatFormatting.GRAY);
	}
}