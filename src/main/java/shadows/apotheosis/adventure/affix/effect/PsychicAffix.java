package shadows.apotheosis.adventure.affix.effect;

import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

/**
 * When blocking an arrow, hurt the shooter.
 */
public class PsychicAffix extends Affix {

	protected final Map<LootRarity, StepFunction> values;

	public PsychicAffix(Map<LootRarity, StepFunction> values) {
		super(AffixType.EFFECT);
		this.values = values;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(Component.translatable("affix." + this.getId() + ".desc", fmt(100 * getTrueLevel(rarity, level))).withStyle(ChatFormatting.YELLOW));
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.SHIELD && this.values.containsKey(rarity);
	}

	@Override
	public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
		if (source.getDirectEntity() instanceof Projectile arrow) {
			Entity owner = arrow.getOwner();
			if (owner instanceof LivingEntity living) {
				living.hurt(new EntityDamageSource("player", entity).setMagic(), amount * getTrueLevel(rarity, level));
			}
		}

		return super.onShieldBlock(stack, rarity, level, entity, source, amount);
	}

	private float getTrueLevel(LootRarity rarity, float level) {
		return this.values.get(rarity).get(level);
	}

	public static Affix read(JsonObject obj) {
		var values = AffixHelper.readValues(GsonHelper.getAsJsonObject(obj, "values"));
		return new PsychicAffix(values);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, func) -> func.write(b));
	}

	public static Affix read(FriendlyByteBuf buf) {
		Map<LootRarity, StepFunction> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> StepFunction.read(b));
		return new PsychicAffix(values);
	}

}