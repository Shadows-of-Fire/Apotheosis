package shadows.apotheosis.adventure.affix.effect;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.util.StepFunction;

public class CleavingAffix extends Affix {

	protected static final StepFunction CHANCE_FUNC = AffixHelper.step(0.3F, 4, 0.05F);
	protected static final StepFunction TARGETS_FUNC = AffixHelper.step(2, 5, 1);
	private static boolean cleaving = false;

	public CleavingAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.HEAVY_WEAPON && rarity.isAtLeast(LootRarity.RARE);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(100 * getChance(rarity, level)), getTargets(rarity, level)).withStyle(ChatFormatting.YELLOW));
	}

	private static float getChance(LootRarity rarity, float level) {
		return (rarity.ordinal() - LootRarity.RARE.ordinal()) * 0.2F + CHANCE_FUNC.get(level);
	}

	private static int getTargets(LootRarity rarity, float level) {
		// We want targets to sort of be separate from chance, so we modulo and double.
		level %= 0.5F;
		level *= 2;
		return (rarity.ordinal() - LootRarity.RARE.ordinal()) * 2 + TARGETS_FUNC.getInt(level);
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
		if (Apotheosis.localAtkStrength >= 0.98 && !cleaving && !user.level.isClientSide) {
			cleaving = true;
			float chance = getChance(rarity, level);
			int targets = getTargets(rarity, level);
			if (user.level.random.nextFloat() < chance && user instanceof Player player) {
				List<Entity> nearby = target.level.getEntities(target, new AABB(target.blockPosition()).inflate(6), cleavePredicate(user, target));
				for (Entity e : nearby) {
					if (targets > 0) {
						user.attackStrengthTicker = 300;
						player.attack(e);
						targets--;
					}
				}
			}
			cleaving = false;
		}
	}

	public static Predicate<Entity> cleavePredicate(Entity user, Entity target) {
		return e -> {
			if ((e instanceof Animal && !(target instanceof Animal)) || (e instanceof AbstractVillager && !(target instanceof AbstractVillager))) return false;
			if (!AdventureConfig.cleaveHitsPlayers && e instanceof Player) return false;
			return e != user && e instanceof LivingEntity;
		};
	}

}
