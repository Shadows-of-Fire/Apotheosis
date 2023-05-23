package shadows.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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
	private static Codec<Pair<StepFunction, StepFunction>> STEP_PAIR_CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			StepFunction.CODEC.fieldOf("duration").forGetter(Pair::getLeft),
			StepFunction.CODEC.fieldOf("amplifier").forGetter(Pair::getRight))
			.apply(inst, Pair::of)
		);
	
	private static MapCodec<Map<LootRarity, Pair<StepFunction, StepFunction>>> VALUES_CODEC = Codec.simpleMap(LootRarity.CODEC, STEP_PAIR_CODEC, Keyable.forStrings(() -> LootRarity.values().stream().map(LootRarity::id)));
	
	public static final Codec<PotionAffix> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
			Target.CODEC.fieldOf("target").forGetter(a -> a.target),
			VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
			Codec.INT.optionalFieldOf("cooldown", 0).forGetter(a -> a.cooldown),
			LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
			.apply(inst, PotionAffix::new)
		);
	//Formatter::on
	public static final PSerializer<PotionAffix> SERIALIZER = PSerializer.fromCodec("Potion Affix", CODEC);

	protected final MobEffect effect;
	protected final Target target;
	protected final Map<LootRarity, Pair<StepFunction, StepFunction>> values;
	protected final int cooldown;
	protected final Set<LootCategory> types;

	protected transient final Map<LootRarity, EffectInst> effects;

	public PotionAffix(MobEffect effect, Target target, Map<LootRarity, Pair<StepFunction, StepFunction>> values, int cooldown, Set<LootCategory> types) {
		super(AffixType.ABILITY);
		this.effect = effect;
		this.target = target;
		this.values = values;
		this.cooldown = cooldown;
		this.types = types;

		var builder = ImmutableMap.<LootRarity, EffectInst>builder();
		values.forEach((rarity, pair) -> {
			builder.put(rarity, new EffectInst(this.effect, pair.getLeft(), pair.getRight()));
		});
		this.effects = builder.build();
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		MobEffectInstance inst = this.effects.get(rarity).build(level);
		if (this.cooldown != 0) {
			Component cd = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(this.cooldown));
			list.accept(Component.translatable("%s %s", this.target.toComponent(toComponent(inst)), cd).withStyle(ChatFormatting.YELLOW));
		} else {
			list.accept(this.target.toComponent(toComponent(inst)).withStyle(ChatFormatting.YELLOW));
		}
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat.isNone()) return false;
		return (this.types.isEmpty() || this.types.contains(cat)) && this.effects.containsKey(rarity);
	};

	@Override
	public void doPostHurt(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity attacker) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.HURT_SELF) applyEffect(user, inst, level);
		else if (this.target == Target.HURT_ATTACKER) {
			if (attacker instanceof LivingEntity tLiving) {
				applyEffect(tLiving, inst, level);
			}
		}
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
		EffectInst inst = this.effects.get(rarity);
		if (this.target == Target.ATTACK_SELF) applyEffect(user, inst, level);
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
	public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, float level, HitResult res, Type type) {
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
		if (this.cooldown != 0) {
			long lastApplied = target.getPersistentData().getLong("apoth.affix_cooldown." + this.getId().toString());
			if (lastApplied != 0 && lastApplied + this.cooldown >= target.level.getGameTime()) return;
		}
		target.addEffect(mei);
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

	public static record EffectInst(MobEffect effect, StepFunction duration, StepFunction amplifier) {

		public MobEffectInstance build(float level) {
			return new MobEffectInstance(this.effect, this.duration.getInt(level), this.amplifier.getInt(level));
		}

		public void write(FriendlyByteBuf buf) {
			this.duration.write(buf);
			this.amplifier.write(buf);
		}

		public static EffectInst read(MobEffect effect, FriendlyByteBuf buf) {
			return new EffectInst(effect, StepFunction.read(buf), StepFunction.read(buf));
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
