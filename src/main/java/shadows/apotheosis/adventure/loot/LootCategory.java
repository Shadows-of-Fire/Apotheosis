package shadows.apotheosis.adventure.loot;

import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Predicates;

import net.minecraft.server.MinecraftServer;
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
	NONE(Predicates.alwaysFalse(), s -> new EquipmentSlot[0]),
	BOW(s -> s.getItem() instanceof BowItem, s -> arr(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)),
	CROSSBOW(s -> s.getItem() instanceof CrossbowItem, s -> arr(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)),
	BREAKER(
			s -> s.canPerformAction(ToolActions.PICKAXE_DIG) || s.canPerformAction(ToolActions.SHOVEL_DIG),
			s -> arr(EquipmentSlot.MAINHAND)),
	HEAVY_WEAPON(new ShieldBreakerTest(), s -> arr(EquipmentSlot.MAINHAND)),
	ARMOR(s -> s.getItem() instanceof ArmorItem, s -> arr(((ArmorItem) s.getItem()).getSlot())),
	SHIELD(s -> s.canPerformAction(ToolActions.SHIELD_BLOCK), s -> arr(EquipmentSlot.OFFHAND)),
	TRIDENT(s -> s.getItem() instanceof TridentItem, s -> arr(EquipmentSlot.MAINHAND)),
	SWORD(
			s -> s.canPerformAction(ToolActions.SWORD_DIG) || s.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, s).get(Attributes.ATTACK_DAMAGE).stream().anyMatch(m -> m.getAmount() > 0),
			s -> arr(EquipmentSlot.MAINHAND));

	private final Predicate<ItemStack> validator;
	private final Function<ItemStack, EquipmentSlot[]> slotGetter;

	LootCategory(Predicate<ItemStack> validator, Function<ItemStack, EquipmentSlot[]> slotGetter) {
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

	public boolean isRanged() {
		return this == BOW || this == CROSSBOW || this == TRIDENT;
	}

	public boolean isDefensive() {
		return this == ARMOR || this == SHIELD;
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

	public boolean isMainhand() {
		return this.isWeapon() || this == BREAKER || this == TRIDENT;
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
}
