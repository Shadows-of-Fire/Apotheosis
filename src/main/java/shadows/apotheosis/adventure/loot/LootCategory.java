package shadows.apotheosis.adventure.loot;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.placebo.codec.PlaceboCodecs;

public final class LootCategory {

	private static final Map<String, LootCategory> BY_ID_INTERNAL = new HashMap<>();
	private static final List<LootCategory> VALUES_INTERNAL = new LinkedList<>();

	public static final Map<String, LootCategory> BY_ID = Collections.unmodifiableMap(BY_ID_INTERNAL);
	public static final List<LootCategory> VALUES = Collections.unmodifiableList(VALUES_INTERNAL);
	public static final Codec<LootCategory> CODEC = ExtraCodecs.stringResolverCodec(LootCategory::getName, LootCategory::byId);
	public static final Codec<Set<LootCategory>> SET_CODEC = PlaceboCodecs.setCodec(CODEC);

	public static final LootCategory BOW = register("bow", s -> s.getItem() instanceof BowItem, s -> arr(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
	public static final LootCategory CROSSBOW = register("crossbow", s -> s.getItem() instanceof CrossbowItem, s -> arr(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
	public static final LootCategory PICKAXE = register("pickaxe", s -> s.canPerformAction(ToolActions.PICKAXE_DIG), s -> arr(EquipmentSlot.MAINHAND));
	public static final LootCategory SHOVEL = register("shovel", s -> s.canPerformAction(ToolActions.SHOVEL_DIG), s -> arr(EquipmentSlot.MAINHAND));
	public static final LootCategory HEAVY_WEAPON = register("heavy_weapon", new ShieldBreakerTest(), s -> arr(EquipmentSlot.MAINHAND));
	public static final LootCategory HELMET = register("helmet", armorSlot(EquipmentSlot.HEAD), s -> arr(EquipmentSlot.HEAD));
	public static final LootCategory CHESTPLATE = register("chestplate", armorSlot(EquipmentSlot.CHEST), s -> arr(EquipmentSlot.CHEST));
	public static final LootCategory LEGGINGS = register("leggings", armorSlot(EquipmentSlot.LEGS), s -> arr(EquipmentSlot.LEGS));
	public static final LootCategory BOOTS = register("boots", armorSlot(EquipmentSlot.FEET), s -> arr(EquipmentSlot.FEET));
	public static final LootCategory SHIELD = register("shield", s -> s.canPerformAction(ToolActions.SHIELD_BLOCK), s -> arr(EquipmentSlot.OFFHAND));
	public static final LootCategory TRIDENT = register("trident", s -> s.getItem() instanceof TridentItem, s -> arr(EquipmentSlot.MAINHAND));
	public static final LootCategory SWORD = register("sword", s -> s.canPerformAction(ToolActions.SWORD_DIG) || s.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, s).get(Attributes.ATTACK_DAMAGE).stream().anyMatch(m -> m.getAmount() > 0), s -> arr(EquipmentSlot.MAINHAND));
	public static final LootCategory NONE = register("none", Predicates.alwaysFalse(), s -> new EquipmentSlot[0]);

	private final String name;
	private final Predicate<ItemStack> validator;
	private final Function<ItemStack, EquipmentSlot[]> slotGetter;

	private LootCategory(String name, Predicate<ItemStack> validator, Function<ItemStack, EquipmentSlot[]> slotGetter) {
		this.name = Preconditions.checkNotNull(name);
		this.validator = Preconditions.checkNotNull(validator);
		this.slotGetter = Preconditions.checkNotNull(slotGetter);
	}

	public String getDescId() {
		return "text.apotheosis.category." + this.name;
	}

	public String getDescIdPlural() {
		return this.getDescId() + ".plural";
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Returns the relevant equipment slot for this item.
	 * The passed item should be of the type this category represents.
	 */
	public EquipmentSlot[] getSlots(ItemStack stack) {
		return this.slotGetter.apply(stack);
	}

	public boolean isValid(ItemStack stack) {
		return this.validator.test(stack);
	}

	public boolean isArmor() {
		return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
	}

	public boolean isBreaker() {
		return this == PICKAXE || this == SHOVEL;
	}

	public boolean isRanged() {
		return this == BOW || this == CROSSBOW || this == TRIDENT;
	}

	public boolean isDefensive() {
		return isArmor() || this == SHIELD;
	}

	public boolean isLightWeapon() {
		return this == SWORD || this == TRIDENT;
	}

	public boolean isWeapon() {
		return this == SWORD || this == HEAVY_WEAPON || this == TRIDENT;
	}

	public boolean isWeaponOrShield() {
		return this.isLightWeapon() || this == SHIELD;
	}

	public boolean isNone() {
		return this == NONE;
	}

	@Override
	public String toString() {
		return String.format("LootCategory[%s]", this.name);
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof LootCategory cat && cat.name.equals(this.name);
	}

	/**
	 * Registers a new loot category, adding it to the BY_ID and VALUES collections so that it will be found by the rest of the universe.
	 * @param orderRef An existing LootCategory, to determine where in the order of precedence this new category will go.
	 * @param name The name of this category.  May not be an existing name.
	 * @param validator A predicate that checks if an item stack matches this loot category.
	 * @param slotGetter A function that provides the loot categories that bonuses will be active for, if an item is of this category.
	 * @return A new loot category, which should be stored in a public static final field.
	 */
	public static final LootCategory register(@Nullable LootCategory orderRef, String name, Predicate<ItemStack> validator, Function<ItemStack, EquipmentSlot[]> slotGetter) {
		var cat = new LootCategory(name, validator, slotGetter);
		if (BY_ID_INTERNAL.containsKey(name)) throw new IllegalArgumentException("Cannot register a loot category with a duplicate name.");
		BY_ID_INTERNAL.put(name, cat);

		int idx = VALUES_INTERNAL.size();
		if (orderRef != null) idx = VALUES_INTERNAL.indexOf(orderRef);
		VALUES_INTERNAL.add(idx, cat);

		return cat;
	}

	/**
	 * Looks up a Loot Category by name.
	 * @param name The name of the loot category.
	 * @return The loot category instance, or null, if no loot category has the specified name.
	 */
	@Nullable
	public static LootCategory byId(String name) {
		return BY_ID.get(name);
	}

	/**
	 * Determines the loot category for an item, by iterating all the categories and selecting the first matching one.
	 * @param item The item to find the category for.
	 * @return The first valid loot category, or {@link #NONE} if no categories were valid.
	 */
	public static LootCategory forItem(ItemStack item) {
		LootCategory override = AdventureConfig.TYPE_OVERRIDES.get(ForgeRegistries.ITEMS.getKey(item.getItem()));
		if (override != null) return override;
		for (LootCategory c : VALUES) {
			if (c.isValid(item)) return c;
		}
		return NONE;
	}

	private static EquipmentSlot[] arr(EquipmentSlot... s) {
		return s;
	}

	private static class ShieldBreakerTest implements Predicate<ItemStack> {

		private Zombie attacker, holder;

		@Override
		public boolean test(ItemStack t) {
			try {
				ItemStack shield = new ItemStack(Items.SHIELD);
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				if (attacker == null && server != null) {
					attacker = new Zombie(server.getLevel(Level.OVERWORLD));
					holder = new Zombie(server.getLevel(Level.OVERWORLD));
				}
				if (holder != null) holder.setItemInHand(InteractionHand.OFF_HAND, shield);
				return t.canDisableShield(shield, holder, attacker);
			} catch (Exception ex) {
				return false;
			}
		}

	}

	private static Predicate<ItemStack> armorSlot(EquipmentSlot slot) {
		return (stack) -> stack.getItem() instanceof ArmorItem arm && arm.getSlot() == slot;
	}

	static final LootCategory register(String name, Predicate<ItemStack> validator, Function<ItemStack, EquipmentSlot[]> slotGetter) {
		return register(null, name, validator, slotGetter);
	}
}
