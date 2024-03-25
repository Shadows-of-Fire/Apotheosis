package dev.shadowsoffire.apotheosis.adventure.affix;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.ench.asm.EnchHooks;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.events.GetEnchantmentLevelEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * An affix is a construct very similar to an enchantment, providing bonuses to arbitrary items.
 * The Affix's Level is a float from 0 to 1 that defines its relative power level, compared to max.
 * What the level means is up to the individual affix.
 */
public abstract class Affix implements CodecProvider<Affix> {

    protected final AffixType type;

    public Affix(AffixType type) {
        this.type = type;
    }

    /**
     * Retrieve the modifiers from this affix to be applied to the itemstack.
     *
     * @param stack The stack the affix is on.
     * @param level The level of this affix.
     * @param type  The slot type for modifiers being gathered.
     * @param map   The destination for generated attribute modifiers.
     */
    public void addModifiers(ItemStack stack, LootRarity rarity, float level, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {}

    /**
     * Adds all tooltip data from this affix to the given stack's tooltip list.
     * This consumer will insert tooltips immediately after enchantment tooltips, or after the name if none are present.
     *
     * @param stack    The stack the affix is on.
     * @param level    The level of this affix.
     * @param tooltips The destination for tooltips.
     */
    public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
        list.accept(Component.translatable("affix." + this.getId() + ".desc", fmt(level)));
    }

    /**
     * Get a component representing an addition of this affix to the item's name.
     *
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
     *
     * @param level  The level of this affix, if applicable.<br>
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
     *
     * @param user   The wielder of the weapon. The weapon stack will be in their main hand.
     * @param target The target entity being attacked.
     * @param level  The level of this affix, if applicable.
     */
    public void doPostAttack(ItemStack stack, LootRarity rarity, float level, LivingEntity user, @Nullable Entity target) {}

    /**
     * Whenever an entity that has this affix on one of its associated items is damaged this method will be
     * called.
     */
    public void doPostHurt(ItemStack stack, LootRarity rarity, float level, LivingEntity user, @Nullable Entity attacker) {}

    /**
     * Called when a user fires an arrow from a bow or crossbow with this affix on it.
     */
    public void onArrowFired(ItemStack stack, LootRarity rarity, float level, LivingEntity user, AbstractArrow arrow) {}

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
    public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, float level, HitResult res, HitResult.Type type) {}

    /**
     * Called when a shield with this affix blocks some amount of damage.
     *
     * @param entity The blocking entity.
     * @param source The damage source being blocked.
     * @param amount The amount of damage blocked.
     * @param level  The level of this affix.
     * @return The amount of damage that is *actually* blocked by the shield, after this affix applies.
     */
    public float onShieldBlock(ItemStack stack, LootRarity rarity, float level, LivingEntity entity, DamageSource source, float amount) {
        return amount;
    }

    /**
     * Called when a player with this affix breaks a block.
     *
     * @param player The breaking player.
     * @param world  The level the block was broken in.
     * @param pos    The position of the block.
     * @param state  The state that was broken.
     */
    public void onBlockBreak(ItemStack stack, LootRarity rarity, float level, Player player, LevelAccessor world, BlockPos pos, BlockState state) {

    }

    /**
     * Allows an affix to reduce durability damage to an item.
     *
     * @param stack  The stack with the affix.
     * @param rarity The rarity of the item.
     * @param level  The level of the affix.
     * @param user   The user of the item, if applicable.
     * @return The percentage [0, 1] of durability damage to ignore. This value will be summed with all other affixes that increase it.
     */
    public float getDurabilityBonusPercentage(ItemStack stack, LootRarity rarity, float level, @Nullable ServerPlayer user) {
        return 0;
    }

    /**
     * Fires during the {@link LivingHurtEvent}, and allows for modification of the damage value.<br>
     * If the value is set to zero or below, the event will be cancelled.
     *
     * @param stack  The stack with the affix.
     * @param rarity The rarity of the item.
     * @param level  The level of the affix.
     * @param src    The Damage Source of the attack.
     * @param ent    The entity being attacked.
     * @param amount The amount of damage that is to be taken.
     * @return The amount of damage that will be taken, after modification. This value will propagate to other affixes.
     */
    public float onHurt(ItemStack stack, LootRarity rarity, float level, DamageSource src, LivingEntity ent, float amount) {
        return amount;
    }

    /**
     * Returns true if this affix enables telepathy.
     */
    public boolean enablesTelepathy() {
        return false;
    }

    /**
     * Fires during {@link GetEnchantmentLevelEvent} and allows for increasing enchantment levels.
     *
     * @param stack    The stack with the affix.
     * @param rarity   The rarity of the item.
     * @param level    The level of the affix.
     * @param ench     The enchantment being queried for.
     * @param oldLevel The original level, before modification.
     * @return The bonus level to be added to the current enchantment.
     */
    public void getEnchantmentLevels(ItemStack stack, LootRarity rarity, float level, Map<Enchantment, Integer> enchantments) {}

    /**
     * Fires from {@link LootModifier#apply(ObjectArrayList, LootContext)} when this affix is on the tool given by the context.
     *
     * @param stack  The stack with the affix.
     * @param rarity The rarity of the item.
     * @param level  The level of the affix.
     * @param loot   The generated loot.
     * @param ctx    The loot context.
     */
    public void modifyLoot(ItemStack stack, LootRarity rarity, float level, ObjectArrayList<ItemStack> loot, LootContext ctx) {}

    @Override
    public String toString() {
        return String.format("Affix: %s", this.getId());
    }

    public AffixType getType() {
        return this.type;
    }

    /**
     * Checks if this affix can be applied to an item.
     *
     * @param stack  The item being checked against.
     * @param cat    The LootCategory of the item.
     * @param rarity The rarity of the item.
     * @return If this affix can be applied to the item at the specified rarity.
     */
    public abstract boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity);

    /**
     * Checks if the affix is still on cooldown, if a cooldown was set via {@link #startCooldown(Affix, int, LivingEntity)}
     */
    public static boolean isOnCooldown(ResourceLocation id, int cooldown, LivingEntity entity) {
        long lastApplied = entity.getPersistentData().getLong("apoth.affix_cooldown." + id.toString());
        return lastApplied != 0 && lastApplied + cooldown >= entity.level().getGameTime();
    }

    /**
     * Records the current time as a cooldown tracker. Used in conjunction with {@link #isOnCooldown(Affix, int, LivingEntity)}
     * <p>
     * Use of this method is problematic if the id is not unique for the effect, as is the case with {@link Gem#getId()} and {@link GemBonus#getId()}.
     */
    public static void startCooldown(ResourceLocation id, LivingEntity entity) {
        entity.getPersistentData().putLong("apoth.affix_cooldown." + id.toString(), entity.level().getGameTime());
    }

    public static String fmt(float f) {
        if (f == (long) f) return String.format("%d", (long) f);
        else return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(f);
    }

    public final ResourceLocation getId() {
        return AffixRegistry.INSTANCE.getKey(this);
    }
}
