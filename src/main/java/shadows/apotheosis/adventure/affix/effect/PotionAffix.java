package shadows.apotheosis.adventure.affix.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

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
import shadows.placebo.util.StepFunction;

public class PotionAffix extends Affix {

	protected final Map<LootRarity, EffectInst> effects;
	protected final @Nullable Predicate<LootCategory> types;
	protected final @Nullable Predicate<ItemStack> items;
	protected final Target target;
	protected final int instantCooldown;

	public PotionAffix(AffixType type, Map<LootRarity, EffectInst> effects, @Nullable Predicate<LootCategory> types, @Nullable Predicate<ItemStack> items, Target target, int instantCooldown) {
		super(type);
		this.effects = effects;
		this.types = types;
		this.items = items;
		this.target = target;
		this.instantCooldown = instantCooldown;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		MobEffectInstance inst = this.effects.get(rarity).build(level);
		list.accept(this.target.toComponent(toComponent(inst)).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == LootCategory.NONE) return false;
		return (this.types == null || this.types.test(cat)) && (this.items == null || this.items.test(stack)) && this.effects.containsKey(rarity);
	};

	@Override
	public void doPostHurt(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity attacker) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.HURT_SELF) user.addEffect(inst.build(level));
		else if (this.target == Target.HURT_ATTACKER) {
			if (attacker instanceof LivingEntity tLiving) {
				applyEffect(tLiving, inst, level);
			}
		}
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.ATTACK_SELF) user.addEffect(inst.build(level));
		else if (this.target == Target.ATTACK_TARGET) {
			if (target instanceof LivingEntity tLiving) {
				applyEffect(tLiving, inst, level);
			}
		}
	}

	@Override
	public void onBlockBreak(ItemStack stack, LootRarity rarity, float level, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.BREAK_SELF) {
			applyEffect(player, inst, level);
		}
	}

	@Override
	public void onArrowImpact(LootRarity rarity, float level, AbstractArrow arrow, HitResult res, Type type) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.ARROW_SELF) {
			if (arrow.getOwner() instanceof LivingEntity owner) {
				applyEffect(owner, inst, level);
			}
		} else if (this.target == Target.ARROW_TARGET) {
			if (type == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
				applyEffect(target, inst, level);
			}
		}
	}

	@Override
	public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.BLOCK_SELF) {
			applyEffect(entity, inst, level);
		} else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
			applyEffect(target, inst, level);
		}
		return amount;
	}

	private void applyEffect(LivingEntity target, EffectInst inst, float level) {
		MobEffectInstance mei = inst.build(level);
		if (mei.getEffect().isInstantenous()) {
			long lastApplied = target.getPersistentData().getLong("apoth.affix_cooldown." + this.getRegistryName().toString());
			if (lastApplied != 0 && lastApplied + 30 >= target.level.getGameTime()) return;
		}
		target.addEffect(mei);
		target.getPersistentData().putLong("apoth.affix_cooldown." + this.getRegistryName().toString(), target.level.getGameTime());
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

	public static record EffectInst(Supplier<MobEffect> effect, StepFunction time, @Nullable StepFunction amp) {

		public MobEffectInstance build(float level) {
			return new MobEffectInstance(this.effect.get(), this.time.getInt(level), this.amp == null ? 0 : this.amp.getInt(level));
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
			return new TranslatableComponent("affix.apotheosis.target." + this.id, args);
		}
	}

	public static class Builder {

		private final Supplier<MobEffect> effect;
		private final Map<LootRarity, EffectInst> effects = new HashMap<>();

		private Predicate<LootCategory> types;
		private Predicate<ItemStack> items;
		private int instantCooldown = 30;

		public Builder(Supplier<MobEffect> effect) {
			this.effect = effect;
		}

		public Builder cooldown(int instantCooldown) {
			this.instantCooldown = instantCooldown;
			return this;
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

		public Builder with(LootRarity rarity, StepFunction timeFunc, @Nullable StepFunction ampFunc) {
			this.effects.put(rarity, new EffectInst(this.effect, timeFunc, ampFunc));
			return this;
		}

		public PotionAffix build(AffixType type, Target target, String id) {
			return (PotionAffix) new PotionAffix(type, this.effects, this.types, this.items, target, this.instantCooldown).setRegistryName(id);
		}

	}

}
