package shadows.apotheosis.adventure.affix;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.json.NBTAdapter;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;

/**
 * An affix is a construct very similar to an enchantment, providing bonuses to arbitrary items.
 * The Affix's Level is a float from 0 to 1 that defines its relative power level, compared to max.
 * What the level means is up to the individual affix.
 */
public abstract class Affix extends TypeKeyedBase<Affix> {

	//Formatter::off
	protected static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(LootRarity.class, JsonUtil.<LootRarity>makeSerializer((json, type, ctx) -> LootRarity.byId(json.getAsString()), null))
			.registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE)
			.registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE)
			.registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.create();
	//Formatter::on

	protected final AffixType type;

	public Affix(AffixType type) {
		this.type = type;
	}

	/**
	 * Retrieve the modifiers from this affix to be applied to the itemstack.
	 * @param stack The stack the affix is on.
	 * @param level The level of this affix.
	 * @param type The slot type for modifiers being gathered.
	 * @param map The destination for generated attribute modifiers.
	 */
	public void addModifiers(ItemStack stack, LootRarity rarity, float level, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
	}

	/**
	 * Adds all tooltip data from this affix to the given stack's tooltip list.
	 * This consumer will insert tooltips immediately after enchantment tooltips, or after the name if none are present.
	 * @param stack The stack the affix is on.
	 * @param level The level of this affix.
	 * @param tooltips The destination for tooltips.
	 */
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(Component.translatable("affix." + this.getId() + ".desc", fmt(level)).withStyle(ChatFormatting.YELLOW));
	}

	/**
	 * Get a component representing an addition of this affix to the item's name.
	 * @return The name part, prefix or suffix, as requested.
	 */
	public Component getName(ItemStack stack, LootRarity rarity, float level, boolean prefix) {
		if (prefix) return Component.translatable("affix." + this.getId());
		return Component.translatable("affix." + this.getId() + ".suffix");
	}

	/**
	 * Calculates the protection value of this affix, with respect to the given damage source.<br>
	 * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}<br>
	 * Ench module overrides with {@link EnchHooks#getDamageAfterMagicAbsorb}<br>
	 * @param level The level of this affix, if applicable.<br>
	 * @param source The damage source to compare against.<br>
	 * @return How many protection points this affix is worth against this source.<br>
	 */
	public int getDamageProtection(ItemStack stack, LootRarity rarity, float level, DamageSource source) {
		return 0;
	}

	/**
	 * Calculates the additional damage this affix deals.
	 * This damage is dealt as player physical damage, and is not impacted by critical strikes.
	 */
	public float getDamageBonus(ItemStack stack, LootRarity rarity, float level, MobType creatureType) {
		return 0.0F;
	}

	/**
	 * Called when someone attacks an entity with an item containing this affix.
	 * More specifically, this is invoked whenever the user attacks a target, while having an item with this affix in either hand or any armor slot.
	 * @param user The wielder of the weapon.  The weapon stack will be in their main hand.
	 * @param target The target entity being attacked.
	 * @param level The level of this affix, if applicable.
	 */
	public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, @Nullable Entity target) {
	}

	/**
	 * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be
	 * called.
	 */
	public void doPostHurt(ItemStack stack, LootRarity rarity, float level, LivingEntity user, @Nullable Entity attacker) {
	}

	/**
	 * Called when a user fires an arrow from a bow or crossbow with this affix on it.
	 */
	public void onArrowFired(ItemStack stack, LootRarity rarity, float level, LivingEntity user, AbstractArrow arrow) {
	}

	/**
	 * Called when {@link Item#onItemUse(ItemUseContext)} would be called for an item with this affix.
	 * Return null to not impact the original result type.
	 */
	@Nullable
	public InteractionResult onItemUse(ItemStack stack, LootRarity rarity, float level, UseOnContext ctx) {
		return null;
	}

	/**
	 * Called when an arrow that was marked with this affix hits a target.
	 */
	public void onArrowImpact(LootRarity rarity, float level, AbstractArrow arrow, HitResult res, HitResult.Type type) {
	}

	/**
	 * Called when a shield with this affix blocks some amount of damage.
	 * @param entity The blocking entity.
	 * @param source The damage source being blocked.
	 * @param amount The amount of damage blocked.
	 * @param level  The level of this affix.
	 * @return	     The amount of damage that is *actually* blocked by the shield, after this affix applies.
	 */
	public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
		return amount;
	}

	/**
	 * Called when a player with this affix breaks a block.
	 * @param player The breaking player.
	 * @param world  The level the block was broken in.
	 * @param pos    The position of the block.
	 * @param state  The state that was broken.
	 */
	public void onBlockBreak(ItemStack stack, LootRarity rarity, float level, Player player, LevelAccessor world, BlockPos pos, BlockState state) {

	}

	@Override
	public String toString() {
		return String.format("Affix: %s", this.getId());
	}

	public AffixType getType() {
		return this.type;
	}

	public abstract boolean canApplyTo(ItemStack stack, LootRarity rarity);

	public static MutableComponent loreComponent(String text, Object... args) {
		return Component.translatable(text, args).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_PURPLE);
	}

	public static String fmt(float f) {
		if (f == (long) f) return String.format("%d", (long) f);
		else return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(f);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Affix afx && afx.getId().equals(this.getId());
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
}