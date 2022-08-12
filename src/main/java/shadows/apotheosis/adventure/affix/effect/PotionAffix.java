package shadows.apotheosis.adventure.affix.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.floats.Float2IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public class PotionAffix extends Affix {

	protected final Map<LootRarity, EffectInst> effects;
	protected final @Nullable Predicate<LootCategory> types;
	protected final @Nullable Predicate<ItemStack> items;
	protected final Target target;

	public PotionAffix(AffixType type, Map<LootRarity, EffectInst> modifiers, @Nullable Predicate<LootCategory> types, @Nullable Predicate<ItemStack> items, Target target) {
		super(type);
		this.effects = modifiers;
		this.types = types;
		this.items = items;
		this.target = target;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		MobEffectInstance inst = this.effects.get(rarity).build(level);
		list.accept(this.target.toComponent(toComponent(inst)).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) return false;
		return (types == null || types.test(cat)) && (items == null || items.test(stack)) && effects.containsKey(rarity);
	};

	@Override
	public void doPostHurt(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity attacker) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.HURT_SELF) user.addEffect(inst.build(level));
		else if (this.target == Target.HURT_ATTACKER) {
			if (attacker instanceof LivingEntity tLiving) {
				tLiving.addEffect(inst.build(level));
			}
		}
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.ATTACK_SELF) user.addEffect(inst.build(level));
		else if (this.target == Target.ATTACK_TARGET) {
			if (target instanceof LivingEntity tLiving) {
				tLiving.addEffect(inst.build(level));
			}
		}
	}

	@Override
	public void onBlockBreak(ItemStack stack, LootRarity rarity, float level, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.BREAK_SELF) {
			player.addEffect(inst.build(level));
		}
	}

	@Override
	public void onArrowImpact(LootRarity rarity, float level, AbstractArrow arrow, HitResult res, Type type) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.ARROW_SELF) {
			if (arrow.getOwner() instanceof LivingEntity owner) {
				owner.addEffect(inst.build(level));
			}
		} else if (this.target == Target.ARROW_TARGET) {
			if (type == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
				target.addEffect(inst.build(level));
			}
		}
	}

	@Override
	public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.BLOCK_SELF) {
			entity.addEffect(inst.build(level));
		} else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
			target.addEffect(inst.build(level));
		}
		return amount;
	}

	public static Component toComponent(MobEffectInstance inst) {
		MutableComponent mutablecomponent = new TranslatableComponent(inst.getDescriptionId());
		MobEffect mobeffect = inst.getEffect();

		if (inst.getAmplifier() > 0) {
			mutablecomponent = new TranslatableComponent("potion.withAmplifier", mutablecomponent, new TranslatableComponent("potion.potency." + inst.getAmplifier()));
		}

		if (inst.getDuration() > 20) {
			mutablecomponent = new TranslatableComponent("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(inst, 1));
		}

		return mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting());
	}

	public static record EffectInst(Supplier<MobEffect> effect, Float2IntFunction time, @Nullable Float2IntFunction amp) {

		public MobEffectInstance build(float level) {
			return new MobEffectInstance(effect.get(), time.get(level), amp == null ? 0 : amp.get(level));
		}
	}

	/**
	 * This enum is used to specify when a potion is applied.
	 * The naming scheme is "<event>_<target>", so attack_self applies to yourself when you attack.
	 */
	public static enum Target {
		ATTACK_SELF("attack_self"),
		ATTACK_TARGET("attack_target"),
		HURT_SELF("hurt_self"),
		HURT_ATTACKER("hurt_attacker"),
		BREAK_SELF("break_self"),
		ARROW_SELF("arrow_self"),
		ARROW_TARGET("arrow_target"),
		BLOCK_SELF("block_self"),
		BLOCK_ATTACKER("block_attacker");

		private final String id;

		Target(String id) {
			this.id = id;
		}

		public MutableComponent toComponent(Object... args) {
			return new TranslatableComponent("affix.apotheosis.target." + id, args);
		}
	}

	public static class Builder {

		private final Supplier<MobEffect> effect;
		private final Map<LootRarity, EffectInst> effects = new HashMap<>();

		private Predicate<LootCategory> types;
		private Predicate<ItemStack> items;

		public Builder(Supplier<MobEffect> effect) {
			this.effect = effect;
		}

		public Builder types(Predicate<LootCategory> types) {
			this.types = types;
			return this;
		}

		/**
		 * Limits the items this affix can apply to.
		 * Importantly, these are checked after types, so if types are filtered
		 * then it is guaranteed that any checked item is of a valid type.
		 */
		public Builder items(Predicate<ItemStack> items) {
			this.items = items;
			return this;
		}

		public Builder with(LootRarity rarity, Float2IntFunction timeFunc, @Nullable Float2IntFunction ampFunc) {
			this.effects.put(rarity, new EffectInst(effect, timeFunc, ampFunc));
			return this;
		}

		public PotionAffix build(AffixType type, Target target, String id) {
			return (PotionAffix) new PotionAffix(type, effects, types, items, target).setRegistryName(id);
		}

	}

}
