package shadows.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.mixin.LivingEntityInvoker;
import shadows.placebo.util.StepFunction;

public class ExecutingAffix extends Affix {

	protected final Map<LootRarity, StepFunction> values;

	public ExecutingAffix(Map<LootRarity, StepFunction> values) {
		super(AffixType.ABILITY);
		this.values = values;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.HEAVY_WEAPON && values.containsKey(rarity);
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(Component.translatable("affix." + this.getId() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(100 * getTrueLevel(rarity, level))).withStyle(ChatFormatting.YELLOW));
	}

	private float getTrueLevel(LootRarity rarity, float level) {
		return this.values.get(rarity).get(level);
	}

	@Override
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, Entity target) {
		float threshold = getTrueLevel(rarity, level);
		if (Apotheosis.localAtkStrength >= 0.98 && target instanceof LivingEntity living && !living.level.isClientSide) {
			if (living.getHealth() / living.getMaxHealth() < threshold) {
				DamageSource src = new EntityDamageSource("apotheosis.execute", user).bypassArmor().bypassMagic();
				if (!((LivingEntityInvoker) living).callCheckTotemDeathProtection(src)) {
					SoundEvent soundevent = ((LivingEntityInvoker) living).callGetDeathSound();
					if (soundevent != null) {
						living.playSound(soundevent, ((LivingEntityInvoker) living).callGetSoundVolume(), living.getVoicePitch());
					}

					living.setHealth(0);
					living.die(src);
				}
			}
		}
	}

	public static Affix read(JsonObject obj) {
		var values = AffixHelper.readValues(GsonHelper.getAsJsonObject(obj, "values"));
		return new ExecutingAffix(values);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, func) -> func.write(b));
	}

	public static Affix read(FriendlyByteBuf buf) {
		Map<LootRarity, StepFunction> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> StepFunction.read(b));
		return new ExecutingAffix(values);
	}

}
