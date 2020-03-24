package shadows.apotheosis.deadly.loot.affix;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.loot.EquipmentType;
import shadows.apotheosis.deadly.loot.modifiers.AffixModifier;

public abstract class Affix extends WeightedRandom.Item implements IForgeRegistryEntry<Affix> {

	static {
		RegistryBuilder<Affix> build = new RegistryBuilder<>();
		build.setName(new ResourceLocation(Apotheosis.MODID, "affixes"));
		build.setType(Affix.class);
		REGISTRY = (ForgeRegistry<Affix>) build.create();
	}

	/**
	 * The affix registry.
	 */
	public static final ForgeRegistry<Affix> REGISTRY;

	/**
	 * The registry name of this item.
	 */
	protected ResourceLocation name;

	/**
	 * @param weight The weight of this affix, relative to other affixes in the same group.
	 */
	public Affix(int weight) {
		super(weight);
	}

	/**
	 * Apply the modifiers of this affix to the given stack.
	 * @param stack The stack to be modified.
	 * @param AffixModifier A modifier to be applied to this affix, or null, if no modifier is applied.  The values applied should reflect the modifier.
	 * @return The level of this affix.  May return 0 if the level is not applicable.
	 */
	public abstract float apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier);

	/**
	 * Chain the name of this affix to the existing name.  If this is a prefix, it should be applied to the front.
	 * If this is a suffix, it should be applied to the black.
	 * @param name The current name, which may have been modified by other affixes.
	 * @return The new name, consuming the old name in the process.
	 */
	public ITextComponent chainName(ITextComponent name, @Nullable AffixModifier modifier) {
		return new TranslationTextComponent("affix." + this.name + (modifier != null && modifier.editName() ? "." + modifier.getKey() : ""), name);
	}

	/**
	 * Calculates the protection value of this affix, with respect to the given damage source.
	 * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}
	 * @param level The level of this affix, if applicable.
	 * @param source The damage source to compare against.
	 * @return How many protection points this affix is worth against this source.
	 */
	public int getProtectionLevel(float level, DamageSource source) {
		return 0;
	}

	/**
	 * Calculates the additional damage this affix deals.
	 * This damage is dealt as player physical damage, and is not impacted by critical strikes.
	 */
	public float getExtraDamageFor(float level, CreatureAttribute creatureType) {
		return 0.0F;
	}

	/**
	 * Called when someone attacks an entity with an item containing this affix.
	 * More specifically, this is invoked whenever the user attacks a target, while having an item with this affix in either hand or any armor slot.
	 * @param user The wielder of the weapon.  The weapon stack will be in their main hand.
	 * @param target The target entity being attacked.
	 * @param level The level of this affix, if applicable.
	 */
	public void onEntityDamaged(LivingEntity user, @Nullable Entity target, float level) {
	}

	/**
	 * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be
	 * called.
	 */
	public void onUserHurt(LivingEntity user, @Nullable Entity attacker, float level) {
	}

	/**
	 * Called when a user fires an arrow from a bow or crossbow with this affix on it.
	 */
	public void onArrowFired(LivingEntity user, AbstractArrowEntity arrow, ItemStack bow, float level) {

	}

	/**
	 * Called when an arrow that was marked with this affix hits a target.
	 */
	public void onArrowImpact(AbstractArrowEntity arrow, RayTraceResult res, RayTraceResult.Type type, float level) {

	}

	@Override
	public Affix setRegistryName(ResourceLocation name) {
		if (this.name == null) this.name = name;
		return this;
	}

	public Affix setRegistryName(String name) {
		return setRegistryName(GameData.checkPrefix(name, false));
	}

	@Override
	public ResourceLocation getRegistryName() {
		return name;
	}

	@Override
	public Class<Affix> getRegistryType() {
		return Affix.class;
	}

	public static void classload() {
	}

	@Override
	public String toString() {
		return String.format("Affix: %s", this.name);
	}

	public abstract boolean canApply(EquipmentType type);

	/**
	 * The minimum possible value for this affix, after modifiers.
	 */
	public abstract float getMin();

	/**
	 * The maximum possible value for this affix, after modifiers.
	 */
	public abstract float getMax();

}
