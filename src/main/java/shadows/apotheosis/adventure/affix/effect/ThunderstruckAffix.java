package shadows.apotheosis.adventure.affix.effect;

import java.util.List;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.floats.Float2IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

/**
 * Damage Chain
 */
public class ThunderstruckAffix extends Affix {

	protected static final Float2IntFunction LEVEL_FUNC = AffixHelper.step(2, 6, 1);

	public ThunderstruckAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc", (int) getTrueLevel(rarity, level)).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack).isLightWeapon() && rarity.isAtLeast(LootRarity.RARE);
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
		if (user.level.isClientSide) return;
		if (Apotheosis.localAtkStrength >= 0.98) {
			List<Entity> nearby = target.level.getEntities(target, new AABB(target.blockPosition()).inflate(6), CleavingAffix.cleavePredicate(user, target));
			for (Entity e : nearby) {
				e.hurt(DamageSource.LIGHTNING_BOLT, getTrueLevel(rarity, level));
			}
		}
	}

	private static float getTrueLevel(LootRarity rarity, float level) {
		return (rarity.ordinal() - LootRarity.RARE.ordinal()) * 2 + LEVEL_FUNC.get(level);
	}

}