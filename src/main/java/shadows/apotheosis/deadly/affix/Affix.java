package shadows.apotheosis.deadly.affix;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.placebo.config.Configuration;

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
	 * Config for affixes.
	 */
	public static Configuration config;

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
	 * Generates a level for this affix.
	 * @param stack The stack the affix will be applied to.
	 * @param modifier A modifier to be applied to this affix, or null, if no modifier is applied.  The values applied should reflect the modifier.
	 * @return The level of this affix, what the level means is determined by the affix.
	 */
	public abstract float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier);

	/**
	 * Retrieve the modifiers from this affix to be applied to the itemstack.
	 * @param stack The stack the affix is on.
	 * @param level The level of this affix.
	 * @param type The slot type for modifiers being gathered.
	 * @param map The destination for generated attribute modifiers.
	 */
	public void addModifiers(ItemStack stack, float level, EquipmentSlotType type, BiConsumer<Attribute, AttributeModifier> map) {
	}

	/**
	 * Adds all tooltip data from this affix to the given stack's tooltip list.
	 * This consumer will insert tooltips immediately after enchantment tooltips, or after the name if none are present.
	 * @param stack The stack the affix is on.
	 * @param level The level of this affix.
	 * @param tooltips The destination for tooltips.
	 */
	public void addInformation(ItemStack stack, float level, Consumer<ITextComponent> list) {
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", fmt(level)));
	}

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
	 * Calculates the protection value of this affix, with respect to the given damage source.<br>
	 * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}<br>
	 * Ench module overrides with {@link EnchHooks#getDamageAfterMagicAbsorb}<br>
	 * @param level The level of this affix, if applicable.<br>
	 * @param source The damage source to compare against.<br>
	 * @return How many protection points this affix is worth against this source.<br>
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
	 * Called when {@link Item#onItemUse(ItemUseContext)} would be called for an item with this affix.
	 * Return null to not impact the original result type.
	 */
	@Nullable
	public ActionResultType onItemUse(ItemUseContext ctx, float level) {
		return null;
	}

	/**
	 * Called when an arrow that was marked with this affix hits a target.
	 */
	public void onArrowImpact(AbstractArrowEntity arrow, RayTraceResult res, RayTraceResult.Type type, float level) {
	}

	/**
	 * Called when a shield with this affix blocks some amount of damage.
	 * @param entity The blocking entity.
	 * @param stack  The shield itemstack the affix is on .
	 * @param source The damage source being blocked.
	 * @param amount The amount of damage blocked.
	 * @param level  The level of this affix.
	 * @return	     The amount of damage that is *actually* blocked by the shield, after this affix applies.
	 */
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, float level) {
		return amount;
	}

	@Override
	public Affix setRegistryName(ResourceLocation name) {
		if (this.name == null) this.name = name;
		return this;
	}

	public Affix setRegistryName(String name) {
		return this.setRegistryName(GameData.checkPrefix(name, false));
	}

	@Override
	public ResourceLocation getRegistryName() {
		return this.name;
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

	/**
	 * Handles the upgrading of this affix's level, given two levels.
	 * Default logic is (highest level + lowest level / 2)
	 */
	public float upgradeLevel(float curLvl, float newLvl) {
		return Math.min(getMax(), newLvl > curLvl ? newLvl + curLvl / 2 : newLvl / 2 + curLvl);
	}

	/**
	 * Generates a new level, as if the passed level were to be split in two.
	 */
	public float obliterateLevel(float level) {
		return Math.max(getMin(), level / 2);
	}

	public static IFormattableTextComponent loreComponent(String text, Object... args) {
		return new TranslationTextComponent(text, args).mergeStyle(TextFormatting.ITALIC, TextFormatting.DARK_PURPLE);
	}

	public static String fmt(float f) {
		if (f == (long) f) return String.format("%d", (long) f);
		else return String.format("%.2f", f);
	}

	public ITextComponent getDisplayName(float level) {
		return new TranslationTextComponent("affix." + this.getRegistryName() + ".name", fmt(level)).mergeStyle(TextFormatting.GRAY);
	}

}