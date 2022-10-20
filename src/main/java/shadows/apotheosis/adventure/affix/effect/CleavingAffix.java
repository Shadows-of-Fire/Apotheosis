package shadows.apotheosis.adventure.affix.effect;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.base.Predicate;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

public class CleavingAffix extends Affix {

	protected final Map<LootRarity, CleaveValues> values;

	private static boolean cleaving = false;

	public CleavingAffix(Map<LootRarity, CleaveValues> values) {
		super(AffixType.EFFECT);
		this.values = values;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.HEAVY_WEAPON && this.values.containsKey(rarity);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(Component.translatable("affix." + this.getId() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(100 * getChance(rarity, level)), getTargets(rarity, level)).withStyle(ChatFormatting.YELLOW));
	}

	private float getChance(LootRarity rarity, float level) {
		return this.values.get(rarity).chance.get(level);
	}

	private int getTargets(LootRarity rarity, float level) {
		// We want targets to sort of be separate from chance, so we modulo and double.
		level %= 0.5F;
		level *= 2;
		return (int) this.values.get(rarity).targets.get(level);
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
			if ((target instanceof Enemy && !(e instanceof Enemy))) return false;
			return e != user && e instanceof LivingEntity;
		};
	}

	static class CleaveValues {
		final StepFunction chance;
		final StepFunction targets;

		CleaveValues(StepFunction chance, StepFunction targets) {
			this.chance = chance;
			this.targets = targets;
		}
	}

	public static CleavingAffix read(JsonObject obj) {
		Map<LootRarity, CleaveValues> values = Affix.GSON.fromJson(obj, new TypeToken<Map<LootRarity, CleaveValues>>() {
		}.getType());
		return new CleavingAffix(values);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, pair) -> {
			pair.chance.write(buf);
			pair.targets.write(buf);
		});
	}

	public static CleavingAffix read(FriendlyByteBuf buf) {
		Map<LootRarity, CleaveValues> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> new CleaveValues(StepFunction.read(b), StepFunction.read(b)));
		return new CleavingAffix(values);
	}

}
