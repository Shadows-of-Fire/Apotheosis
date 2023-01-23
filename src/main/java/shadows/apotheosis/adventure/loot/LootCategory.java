package shadows.apotheosis.adventure.loot;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.google.gson.annotations.SerializedName;
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

public enum LootCategory {
	@SerializedName("none")
	NONE("none", Predicates.alwaysFalse(), s -> new EquipmentSlot[0]),

	@SerializedName("bow")
	BOW("bow", s -> s.getItem() instanceof BowItem, s -> arr(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)),

	@SerializedName("crossbow")
	CROSSBOW(
			"crossbow",
			s -> s.getItem() instanceof CrossbowItem,
			s -> arr(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)),

	@SerializedName("pickaxe")
	PICKAXE("pickaxe", s -> s.canPerformAction(ToolActions.PICKAXE_DIG), s -> arr(EquipmentSlot.MAINHAND)),

	@SerializedName("shovel")
	SHOVEL("shovel", s -> s.canPerformAction(ToolActions.SHOVEL_DIG), s -> arr(EquipmentSlot.MAINHAND)),

	@SerializedName("heavy_weapon")
	HEAVY_WEAPON("heavy_weapon", new ShieldBreakerTest(), s -> arr(EquipmentSlot.MAINHAND)),

	@SerializedName("helmet")
	HELMET("helmet", armorSlot(EquipmentSlot.HEAD), s -> arr(EquipmentSlot.HEAD)),

	@SerializedName("chestplate")
	CHESTPLATE("chestplate", armorSlot(EquipmentSlot.CHEST), s -> arr(EquipmentSlot.CHEST)),

	@SerializedName("leggings")
	LEGGINGS("leggings", armorSlot(EquipmentSlot.LEGS), s -> arr(EquipmentSlot.LEGS)),

	@SerializedName("boots")
	BOOTS("boots", armorSlot(EquipmentSlot.FEET), s -> arr(EquipmentSlot.FEET)),

	@SerializedName("shield")
	SHIELD("shield", s -> s.canPerformAction(ToolActions.SHIELD_BLOCK), s -> arr(EquipmentSlot.OFFHAND)),

	@SerializedName("trident")
	TRIDENT("trident", s -> s.getItem() instanceof TridentItem, s -> arr(EquipmentSlot.MAINHAND)),

	@SerializedName("sword")
	SWORD(
			"sword",
			s -> s.canPerformAction(ToolActions.SWORD_DIG) || s.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, s).get(Attributes.ATTACK_DAMAGE).stream().anyMatch(m -> m.getAmount() > 0),
			s -> arr(EquipmentSlot.MAINHAND));

	public static final Map<String, LootCategory> BY_ID = Arrays.stream(LootCategory.values()).collect(Collectors.toMap(LootCategory::getName, Function.identity()));
	public static final Codec<LootCategory> CODEC = ExtraCodecs.stringResolverCodec(LootCategory::getName, LootCategory::byId);

	private final String name;
	private final Predicate<ItemStack> validator;
	private final Function<ItemStack, EquipmentSlot[]> slotGetter;

	LootCategory(String name, Predicate<ItemStack> validator, Function<ItemStack, EquipmentSlot[]> slotGetter) {
		this.name = name;
		this.validator = validator;
		this.slotGetter = slotGetter;
	}

	static EquipmentSlot[] arr(EquipmentSlot... s) {
		return s;
	}

	static final LootCategory[] VALUES = values();

	public static LootCategory forItem(ItemStack item) {
		LootCategory override = AdventureConfig.TYPE_OVERRIDES.get(ForgeRegistries.ITEMS.getKey(item.getItem()));
		if (override != null) return override;
		for (LootCategory c : VALUES) {
			if (c.isValid(item)) return c;
		}
		return NONE;
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

	private static class ShieldBreakerTest implements Predicate<ItemStack> {

		@Override
		public boolean test(ItemStack t) {
			try {
				ItemStack shield = new ItemStack(Items.SHIELD);
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				Zombie attacker = server != null ? new Zombie(server.getLevel(Level.OVERWORLD)) : null;
				Zombie holder = server != null ? new Zombie(server.getLevel(Level.OVERWORLD)) : null;
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

	@Nullable
	public static LootCategory byId(String name) {
		return BY_ID.get(name);
	}
}
