package shadows.apotheosis.adventure.affix.effect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
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
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.util.StepFunction;

public class PotionAffix extends Affix {

	protected final Map<LootRarity, EffectInst> effects;
	protected final Set<LootCategory> types;
	protected final Target target;
	protected final int cooldown;

	public PotionAffix(Map<LootRarity, EffectInst> effects, Set<LootCategory> types, Target target, int cooldown) {
		super(AffixType.POTION);
		this.effects = effects;
		this.types = types;
		this.target = target;
		this.cooldown = cooldown;
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
		return (this.types.isEmpty() || this.types.contains(cat)) && this.effects.containsKey(rarity);
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
		if (this.cooldown != 0) {
			long lastApplied = target.getPersistentData().getLong("apoth.affix_cooldown." + this.getId().toString());
			if (lastApplied != 0 && lastApplied + this.cooldown >= target.level.getGameTime()) return;
		}
		target.addEffect(mei);
		target.getPersistentData().putLong("apoth.affix_cooldown." + this.getId().toString(), target.level.getGameTime());
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

	public static PotionAffix read(JsonObject obj) {
		MobEffect effect = JsonUtil.getRegistryObject(obj, "mob_effect", ForgeRegistries.MOB_EFFECTS);
		Target target = Target.valueOf(GsonHelper.getAsString(obj, "target"));
		JsonObject valueMap = GsonHelper.getAsJsonObject(obj, "values");
		Map<LootRarity, EffectInst> effects = new HashMap<>();
		for (String s : valueMap.keySet()) {
			LootRarity rarity = LootRarity.byId(s);
			JsonObject child = valueMap.get(s).getAsJsonObject();
			JsonElement dur = child.get("duration");
			StepFunction duration = dur.isJsonObject() ? GSON.fromJson(dur, StepFunction.class) : StepFunction.constant(dur.getAsInt());
			JsonElement amp = child.get("amplifier");
			StepFunction amplifier = amp.isJsonObject() ? GSON.fromJson(amp, StepFunction.class) : StepFunction.constant(amp.getAsInt());
			effects.put(rarity, new EffectInst(effect, duration, amplifier));
		}
		var types = AffixHelper.readTypes(GsonHelper.getAsJsonArray(obj, "types"));
		int cooldown = GsonHelper.getAsInt(obj, "cooldown", 0);
		return new PotionAffix(effects, types, target, cooldown);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		EffectInst inst = this.effects.values().stream().findFirst().get();
		buf.writeRegistryId(ForgeRegistries.MOB_EFFECTS, inst.effect);
		buf.writeMap(this.effects, (b, key) -> b.writeUtf(key.id()), (b, modif) -> modif.write(b));
		buf.writeByte(this.types.size());
		this.types.forEach(c -> buf.writeEnum(c));
		buf.writeEnum(this.target);
		buf.writeInt(this.cooldown);
	}

	public static PotionAffix read(FriendlyByteBuf buf) {
		MobEffect effect = buf.readRegistryIdSafe(MobEffect.class);
		Map<LootRarity, EffectInst> effects = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> EffectInst.read(effect, b));
		Set<LootCategory> types = new HashSet<>();
		int size = buf.readByte();
		for (int i = 0; i < size; i++) {
			types.add(buf.readEnum(LootCategory.class));
		}
		Target target = buf.readEnum(Target.class);
		int cooldown = buf.readInt();
		return new PotionAffix(effects, types, target, cooldown);
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
			return Component.translatable("affix.apotheosis.target." + this.id, args);
		}
	}

}
