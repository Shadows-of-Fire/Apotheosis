package shadows.apotheosis.deadly.gen;

import java.util.List;

import net.minecraft.entity.EntityType;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import shadows.apotheosis.util.RandomIntRange;

public class BossItem {

	protected final ResourceLocation id;
	protected final EntityType<?> entity;
	protected final AxisAlignedBB size;
	protected final RandomValueRange health;
	protected final RandomValueRange kbResist;
	protected final RandomValueRange speed;
	protected final RandomValueRange dmg;
	protected final float enchantChance;
	protected final List<ChancedEffectInstance> effects;

	public BossItem(ResourceLocation id, EntityType<?> entity, AxisAlignedBB size, RandomValueRange health, RandomValueRange kbResist, RandomValueRange speed, RandomValueRange dmg, float enchantChance, List<ChancedEffectInstance> effects) {
		this.id = id;
		this.entity = entity;
		this.size = size;
		this.health = health;
		this.kbResist = kbResist;
		this.speed = speed;
		this.dmg = dmg;
		this.enchantChance = enchantChance;
		this.effects = effects;
	}

	public static class Builder {
		protected final EntityType<?> entity;
		protected AxisAlignedBB size = new AxisAlignedBB(0, 0, 0, 1, 2, 1);
		protected RandomValueRange health = new RandomValueRange(4F, 8F);
		protected RandomValueRange kbResist = new RandomValueRange(0.65F, 1F);
		protected RandomValueRange speed = new RandomValueRange(1.10F, 1.4F);
		protected RandomValueRange dmg = new RandomValueRange(2F, 4.5F);
		protected float enchantChance = 0.45F;
		protected List<ChancedEffectInstance> effects;

		/**
		 * Creates a BossItem builder for this entity.
		 */
		public Builder(EntityType<?> entity) {
			this.entity = entity;
		}

		/**
		 * Specifies the size of the contained entity. <br>
		 * Used for worldgen space checks.
		 */
		public Builder withSize(AxisAlignedBB size) {
			this.size = size;
			return this;
		}

		/**
		 * Specifies min/max values for max hp multipliers.
		 */
		public Builder withHealth(float min, float max) {
			this.health = new RandomValueRange(min, max);
			return this;
		}

		/**
		 * Specifies min/max values for knockback resist.
		 */
		public Builder withKbResist(float min, float max) {
			this.kbResist = new RandomValueRange(min, max);
			return this;
		}

		/**
		 * Specifies min/max values for speed multipliers.
		 */
		public Builder withSpeed(float min, float max) {
			this.speed = new RandomValueRange(min, max);
			return this;
		}

		/**
		 * Specifies min/max values for bonus attack damage.
		 */
		public Builder withDamage(float min, float max) {
			this.dmg = new RandomValueRange(min, max);
			return this;
		}

		/**
		 * Specifies the chance that a piece of gear is enchanted.
		 */
		public Builder withEnchantChance(float chance) {
			this.enchantChance = chance;
			return this;
		}

		/**
		 * Specifies possible obtainable enchantments.
		 */
		public Builder withEffects(ChancedEffectInstance... effects) {
			for (ChancedEffectInstance e : effects) {
				this.effects.add(e);
			}
			return this;
		}

		public BossItem build(ResourceLocation id) {
			return new BossItem(id, entity, size, health, kbResist, speed, dmg, enchantChance, effects);
		}
	}

	/**
	 * Represents a potion with a chance to receive this potion.
	 */
	public static class ChancedEffectInstance {
		protected final float chance;
		protected final Effect effect;
		protected final RandomIntRange amp;

		/**
		 * Creates a Chanced Effect Instance.
		 * @param chance The chance this potion is received.
		 * @param effect The effect.
		 * @param amp A random range of possible amplifiers.
		 */
		public ChancedEffectInstance(float chance, Effect effect, RandomIntRange amp) {
			this.chance = chance;
			this.effect = effect;
			this.amp = amp;
		}
	}
}