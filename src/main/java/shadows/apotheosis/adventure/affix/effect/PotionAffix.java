package shadows.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
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
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.json.PSerializer;
import shadows.placebo.util.StepFunction;

public class PotionAffix extends Affix {

	//Formatter::off
	public static final Codec<PotionAffix> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
			Target.CODEC.fieldOf("target").forGetter(a -> a.target),
			LootRarity.mapCodec(EffectData.CODEC).fieldOf("values").forGetter(a -> a.values),
			Codec.INT.optionalFieldOf("cooldown", 0).forGetter(a -> a.cooldown),
			LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types),
			Codec.BOOL.optionalFieldOf("stack_on_reapply", false).forGetter(a -> a.stackOnReapply))
			.apply(inst, PotionAffix::new)
		);
	//Formatter::on
	public static final PSerializer<PotionAffix> SERIALIZER = PSerializer.fromCodec("Potion Affix", CODEC);

	protected final MobEffect effect;
	protected final Target target;
	protected final Map<LootRarity, EffectData> values;
	@Deprecated(forRemoval = true, since = "6.3.0")
	protected final int cooldown;
	protected final Set<LootCategory> types;
	protected final boolean stackOnReapply;

	public PotionAffix(MobEffect effect, Target target, Map<LootRarity, EffectData> values, int cooldown, Set<LootCategory> types, boolean stackOnReapply) {
		super(AffixType.ABILITY);
		this.effect = effect;
		this.target = target;
		this.values = values;
		this.cooldown = cooldown;
		this.types = types;
		this.stackOnReapply = stackOnReapply;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		MobEffectInstance inst = this.values.get(rarity).build(this.effect, level);
		MutableComponent comp = this.target.toComponent(toComponent(inst)).withStyle(ChatFormatting.YELLOW);
		int cooldown = this.getCooldown(rarity);
		if (cooldown != 0) {
			Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cooldown));
			comp = comp.append(" ").append(cd);
		}
		if (this.stackOnReapply) {
			comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
		}
		list.accept(comp);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
		return (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
	};

	@Override
	public void doPostHurt(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity attacker) {
		if (this.target == Target.HURT_SELF) applyEffect(user, rarity, level);
		else if (this.target == Target.HURT_ATTACKER) {
			if (attacker instanceof LivingEntity tLiving) {
				applyEffect(tLiving, rarity, level);
			}
		}
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
		if (this.target == Target.ATTACK_SELF) applyEffect(user, rarity, level);
		else if (this.target == Target.ATTACK_TARGET) {
			if (target instanceof LivingEntity tLiving) {
				applyEffect(tLiving, rarity, level);
			}
		}
	}

	@Override
	public void onBlockBreak(ItemStack stack, LootRarity rarity, float level, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		if (this.target == Target.BREAK_SELF) {
			applyEffect(player, rarity, level);
		}
	}

	@Override
	public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, float level, HitResult res, Type type) {
		if (this.target == Target.ARROW_SELF) {
			if (arrow.getOwner() instanceof LivingEntity owner) {
				applyEffect(owner, rarity, level);
			}
		} else if (this.target == Target.ARROW_TARGET) {
			if (type == Type.ENTITY && ((EntityHitResult) res).getEntity() instanceof LivingEntity target) {
				applyEffect(target, rarity, level);
			}
		}
	}

	@Override
	public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
		if (this.target == Target.BLOCK_SELF) {
			applyEffect(entity, rarity, level);
		} else if (this.target == Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
			applyEffect(target, rarity, level);
		}
		return amount;
	}

	protected int getCooldown(LootRarity rarity) {
		EffectData data = this.values.get(rarity);
		if (data.cooldown != -1) return data.cooldown;
		return this.cooldown;
	}

	private void applyEffect(LivingEntity target, LootRarity rarity, float level) {
		int cooldown = this.getCooldown(rarity);
		if (cooldown != 0) {
			long lastApplied = target.getPersistentData().getLong("apoth.affix_cooldown." + this.getId().toString());
			if (lastApplied != 0 && lastApplied + cooldown >= target.level.getGameTime()) return;
		}
		EffectData data = this.values.get(rarity);
		var inst = target.getEffect(this.effect);
		if (this.stackOnReapply && inst != null) {
			if (inst != null) {
				var newInst = new MobEffectInstance(this.effect, (int) Math.max(inst.getDuration(), data.duration.get(level)), (int) (inst.getAmplifier() + 1 + data.amplifier.get(level)));
				target.addEffect(newInst);
			}
		} else {
			target.addEffect(data.build(this.effect, level));
		}
		target.getPersistentData().putLong("apoth.affix_cooldown." + this.getId().toString(), target.level.getGameTime());
	}

	@Override
	public PSerializer<? extends Affix> getSerializer() {
		return SERIALIZER;
	}

	public static Component toComponent(MobEffectInstance inst) {
		MutableComponent mutablecomponent = Component.translatable(inst.getDescriptionId());
		MobEffect mobeffect = inst.getEffect();

		if (inst.getAmplifier() > 0) {
			mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + inst.getAmplifier()));
		}

		if (inst.getDuration() > 20) {
			mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(inst, 1));
		}

		return mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting());
	}

	public static record EffectData(StepFunction duration, StepFunction amplifier, int cooldown) {

		//Formatter::off
		private static Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
				StepFunction.CODEC.fieldOf("duration").forGetter(EffectData::duration),
				StepFunction.CODEC.fieldOf("amplifier").forGetter(EffectData::amplifier),
				Codec.INT.optionalFieldOf("cooldown", -1).forGetter(EffectData::cooldown))
				.apply(inst, EffectData::new)
			);
		//Formatter::on

		public MobEffectInstance build(MobEffect effect, float level) {
			return new MobEffectInstance(effect, this.duration.getInt(level), this.amplifier.getInt(level));
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

		public static final Codec<Target> CODEC = new EnumCodec<>(Target.class);

		private final String id;

		Target(String id) {
			this.id = id;
		}

		public MutableComponent toComponent(Object... args) {
			return Component.translatable("affix.apotheosis.target." + this.id, args);
		}
	}

}
