package shadows.apotheosis.deadly.affix.impl.heavy;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import javax.annotation.Nullable;

/**
 * Cleave Affix.  Allows for full strength attacks to trigger a full-strength attack against nearby enemies.
 */
public class CleaveAffix extends RangedAffix {

	private static boolean cleaving = false;

	public CleaveAffix(LootRarity rarity, float min, float max, int weight) { super(rarity, min, max, weight); }

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory == LootCategory.HEAVY_WEAPON;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (Apotheosis.localAtkStrength >= 0.98 && !cleaving && user instanceof Player player) {
			cleaving = true;
			float chance = level % 1;
			int targets = (int) level;
			if (player.level.random.nextFloat() < chance) {
				Predicate<Entity> pred = e -> !(e instanceof Player) && e instanceof LivingEntity && ((LivingEntity) e).canAttackType(EntityType.PLAYER);
				List<Entity> nearby = target.level.getEntities(target, new AABB(target.blockPosition()).inflate(6), pred);
				if (!player.level.isClientSide) for (Entity e : nearby) {
					if (targets > 0) {
						player.attackStrengthTicker = 300;
						player.attack(e);
						targets--;
					}
				}
			}
			cleaving = false;
		}
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		var numTargets = 2 + rand.nextInt(9);
		var cleaveChance = range.getRandomValue(rand);
		return numTargets + cleaveChance;
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
		float chance = level % 1;
		int targets = (int) level;
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", String.format("%.2f", chance * 100), targets));
	}

	@Override
	public Component getDisplayName(float level) {
		float chance = level % 1;
		int targets = (int) level;
		return new TranslatableComponent("affix." + this.getRegistryName() + ".name", String.format("%.2f", chance * 100), targets).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public float upgradeLevel(float curLvl, float newLvl) {
		float curChance = curLvl % 1;
		int curTargets = (int) curLvl;
		float newChance = newLvl % 1;
		int newTargets = (int) newLvl;
		var upgradedChance = Math.min(range.getMax(), super.upgradeLevel(curChance, newChance));
		int upgradedTargets = Math.min(10, (int)super.upgradeLevel(curTargets, newTargets));
		return upgradedTargets + upgradedChance;
	}

}