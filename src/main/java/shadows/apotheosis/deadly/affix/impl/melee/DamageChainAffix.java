package shadows.apotheosis.deadly.affix.impl.melee;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Full strength attacks will zap nearby enemies.
 */
public class DamageChainAffix extends RangedAffix {

	public DamageChainAffix(LootRarity rarity, int min, int max, int weight) {
		super(rarity, min, max, weight);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory == LootCategory.SWORD;
	}

	@Override
	public boolean isPrefix() {
		return false;
	}

	@Override
	public void onEntityDamaged(LivingEntity user, Entity target, float level) {
		if (Apotheosis.localAtkStrength >= 0.98) {
			Predicate<Entity> pred = e -> !(e instanceof Player) && e instanceof LivingEntity && ((LivingEntity) e).canAttackType(EntityType.PLAYER);
			List<Entity> nearby = target.level.getEntities(target, new AABB(target.blockPosition()).inflate(6), pred);
			if (!user.level.isClientSide) for (Entity e : nearby) {
				e.hurt(DamageSource.LIGHTNING_BOLT, level);
			}
		}
	}

	@Override
	public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		return Math.round(super.generateLevel(stack, rand, modifier));
	}
}