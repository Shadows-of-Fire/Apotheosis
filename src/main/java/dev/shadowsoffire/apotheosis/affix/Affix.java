package dev.shadowsoffire.apotheosis.affix;

import javax.annotation.Nullable;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * An affix is a construct very similar to an enchantment, providing bonuses to arbitrary items.
 * The Affix's Level is a float from 0 to 1 that defines its relative power level, compared to max.
 * What the level means is up to the individual affix.
 */
public abstract class Affix implements CodecProvider<Affix>, Weighted {

    public static final float MAX_LEVEL = 2.0F;
    public static final float STANDARD_MAX_LEVEL = 1.0F;

    protected final AffixDefinition definition;

    protected Affix(AffixDefinition definition) {
        this.definition = definition;
    }

    /**
     * Checks if this affix can be applied to an item.
     *
     * @param stack  The item being checked against.
     * @param cat    The category of the target item.
     * @param rarity The rarity of the target item.
     * @return If this affix can be applied to the item at the specified rarity.
     */
    public abstract boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity);

    /**
     * Gets the one-line description for this affix, to be added to the item stack's tooltip.
     * <p>
     * Description tooltips are added immediately before enchantment tooltips, or after the name if none are present.
     * If you do not want to show a specific affix description, return {@link Component#empty()}.
     *
     * @param inst The affix instance.
     */
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", fmt(inst.level()));
    }

    /**
     * Get a component representing an addition of this affix to the item's name.
     *
     * @return The name part, prefix or suffix, as requested.
     */
    public Component getName(boolean prefix) {
        if (prefix) {
            return Component.translatable("affix." + this.id());
        }
        return Component.translatable("affix." + this.id() + ".suffix");
    }

    /**
     * Returns a component containing the text shown in the Augmenting Table.
     * <p>
     * This text should show the current affix power, as well as the min/max power bounds, displaying to the user what the full range is.
     *
     * @param inst The affix instance.
     */
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        return this.getDescription(inst, ctx);
    }

    /**
     * Returns true if this affix is level-independent, meaning that its power does not change with the level of the affix.
     */
    public boolean isLevelIndependent(AffixInstance inst) {
        return false;
    }

    /**
     * Adds any attribute modifiers supplied by this affix to the passed consumer.
     * <p>
     * Attribute modifiers must have unique IDs per-slot, since the same affix may be applicable to multiple items.
     *
     * @param inst  The affix instance.
     * @param event The attribute modifiers event.
     */
    public void addModifiers(AffixInstance inst, StackAttributeModifiersEvent event) {}

    /**
     * Calculates the protection value of this affix, with respect to the given damage source.<br>
     * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}<br>
     * Ench module overrides with {@link EnchHooks#getDamageAfterMagicAbsorb}<br>
     *
     * @param inst   The affix instance.
     * @param source The damage source to compare against.
     * @return How many protection points this affix is worth against this source.
     */
    public float getDamageProtection(AffixInstance inst, DamageSource source) {
        return 0;
    }

    /**
     * Calculates the additional damage this affix deals.
     * This damage is dealt as player physical damage, and is not impacted by critical strikes.
     *
     * @param inst   The affix instance.
     * @param entity The target entity.
     */
    public float getDamageBonus(AffixInstance inst, Entity entity) {
        return 0.0F;
    }

    /**
     * Called when someone attacks an entity with an item containing this affix.
     * More specifically, this is invoked whenever the user attacks a target, while having an item with this affix in either hand or any armor slot.
     *
     * @param inst   The affix instance.
     * @param user   The wielder of the weapon. The weapon stack will be in their main hand.
     * @param target The target entity being attacked.
     */
    public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {}

    /**
     * Whenever an entity that has this affix on one of its associated items is damaged this method will be
     * called.
     */
    public void doPostHurt(AffixInstance inst, LivingEntity user, DamageSource source) {}

    /**
     * Called when a user fires an projectile from a weapon with this affix on it.
     */
    public void onProjectileFired(AffixInstance inst, LivingEntity user, Projectile projectile) {}

    /**
     * Called when {@link Item#useOn(ItemUseContext)} would be called for an item with this affix.
     * Return null to not impact the original result type.
     */
    @Nullable
    public InteractionResult onItemUse(AffixInstance inst, UseOnContext ctx) {
        return null;
    }

    /**
     * Called when an projectile that was marked with this affix hits a target.
     */
    public void onProjectileImpact(float level, LootRarity rarity, Projectile proj, HitResult res, HitResult.Type type) {}

    /**
     * Called when a shield with this affix blocks some amount of damage.
     *
     * @param entity The blocking entity.
     * @param source The damage source being blocked.
     * @param amount The amount of damage blocked.
     * @param level  The level of this affix.
     * @return The amount of damage that is *actually* blocked by the shield, after this affix applies.
     */
    public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
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
    public void onBlockBreak(AffixInstance inst, Player player, LevelAccessor world, BlockPos pos, BlockState state) {

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
    public float getDurabilityBonusPercentage(AffixInstance inst) {
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
    public float onHurt(AffixInstance inst, DamageSource src, LivingEntity ent, float amount) {
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
     * @param inst  The affix instance.
     * @param event The GetEnchantmentLevelEvent, which allows for modification of the levels.
     */
    public void getEnchantmentLevels(AffixInstance inst, GetEnchantmentLevelEvent event) {}

    /**
     * Fires from {@link LootModifier#apply(ObjectArrayList, LootContext)} when this affix is on the tool given by the context.
     *
     * @param stack  The stack with the affix.
     * @param rarity The rarity of the item.
     * @param level  The level of the affix.
     * @param loot   The generated loot.
     * @param ctx    The loot context.
     */
    public void modifyLoot(AffixInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {}

    /**
     * Generic {@link LivingDropsEvent} handler which allows affixes to execute custom functionality.
     */
    public void modifyEntityLoot(AffixInstance inst, LivingDropsEvent event) {}

    /**
     * An affix is compatible with another affix if they aren't the name affix, and neither is exclusive with the other.
     */
    public boolean isCompatibleWith(Affix affix) {
        return this != affix && !this.definition().exclusiveSet().contains(AffixRegistry.INSTANCE.holder(affix)) && !affix.definition().exclusiveSet().contains(AffixRegistry.INSTANCE.holder(this));
    }

    public boolean isCompatibleWith(ItemAffixes affixes) {
        return affixes.liveAffixes().allMatch(this::isCompatibleWith);
    }

    @Override
    public String toString() {
        return String.format("Affix: %s", this.id());
    }

    public final AffixDefinition definition() {
        return this.definition;
    }

    @Override
    public final TieredWeights weights() {
        return this.definition.weights();
    }

    public final ResourceLocation id() {
        return AffixRegistry.INSTANCE.getKey(this);
    }

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
     * Use of this method is problematic if the id is not unique for the effect, as is the case with {@link Gem#getId()} and {@link GemBonus#getTypeKey()}.
     */
    public static void startCooldown(ResourceLocation id, LivingEntity entity) {
        entity.getPersistentData().putLong("apoth.affix_cooldown." + id.toString(), entity.level().getGameTime());
    }

    public static String fmt(float f) {
        if (f == (long) f) {
            return String.format("%d", (long) f);
        }
        else {
            return IAttributeExtension.FORMAT.format(f);
        }
    }

    public static MutableComponent valueBounds(Component min, Component max) {
        return CommonComponents.space().append(Component.translatable("misc.apotheosis.affix_bounds", min, max).withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * Generates a deterministic {@link ResourceLocation} that is unique for a given socketed gem instance.
     * <p>
     * Can be used to generate attribute modifiers, track cooldowns, and other things that need to be unique per-gem-in-slot.
     *
     * @param inst The owning gem instance for the bonus
     * @param salt A salt value, which can be used if the bonus needs multiple modifiers.
     */
    static ResourceLocation makeUniqueId(AffixInstance inst, String salt) {
        ResourceLocation key = inst.affix().getId();
        LootCategory cat = LootCategory.forItem(inst.stack());
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath() + "_modifier_" + cat.getSlots().id().toShortLanguageKey() + "_" + salt);
    }

    /**
     * Calls {@link #makeUniqueId(GemInstance, String)} with an empty salt value.
     */
    static ResourceLocation makeUniqueId(AffixInstance inst) {
        return makeUniqueId(inst, "");
    }

    protected static <T extends Affix> App<RecordCodecBuilder.Mu<T>, AffixDefinition> affixDef() {
        return AffixDefinition.CODEC.fieldOf("definition").forGetter(Affix::definition);
    }
}
