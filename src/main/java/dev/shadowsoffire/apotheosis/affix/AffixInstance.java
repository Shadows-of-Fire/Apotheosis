package dev.shadowsoffire.apotheosis.affix;

import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * An Affix Instance is a wrapper around the necessary parameters for all affix methods.
 * Prefer using this over directly invoking methods on {@link Affix}.
 *
 * @param affix  The affix this instance is for.
 * @param level  The level of the affix.
 * @param rarity The rarity of the source item.
 * @param stack  The source item stack.
 */
public record AffixInstance(DynamicHolder<Affix> affix, float level, DynamicHolder<LootRarity> rarity, ItemStack stack) {

    public LootCategory category() {
        return LootCategory.forItem(this.stack);
    }

    public boolean isValid() {
        return this.affix.isBound() && this.rarity.isBound();
    }

    /**
     * Resolves the underlying {@link #rarity}. Throws if unbound.
     */
    public LootRarity getRarity() {
        return this.rarity.get();
    }

    /**
     * Resolves the underlying {@link #affix}. Throws if unbound.
     */
    public Affix getAffix() {
        return this.affix.get();
    }

    /**
     * @see Affix#addModifiers(ItemStack, LootRarity, float, EquipmentSlot, BiConsumer)
     */
    public void addModifiers(StackAttributeModifiersEvent event) {
        this.getAffix().addModifiers(this, event);
    }

    /**
     * @see Affix#getDescription(ItemStack, LootRarity, float)
     */
    public MutableComponent getDescription(AttributeTooltipContext ctx) {
        return this.getAffix().getDescription(this, ctx);
    }

    /**
     * @see Affix#getAugmentingText(ItemStack, LootRarity, float)
     */
    public Component getAugmentingText(AttributeTooltipContext ctx) {
        return this.getAffix().getAugmentingText(this, ctx);
    }

    /**
     * @see Affix#getName(boolean)
     */
    public Component getName(boolean prefix) {
        return this.getAffix().getName(prefix);
    }

    /**
     * @see Affix#getDamageProtection(ItemStack, LootRarity, float, DamageSource)
     */
    public float getDamageProtection(DamageSource source) {
        return this.getAffix().getDamageProtection(this, source);
    }

    /**
     * @see Affix#getDamageBonus(ItemStack, LootRarity, float, MobType)
     */
    public float getDamageBonus(Entity entity) {
        return this.getAffix().getDamageBonus(this, entity);
    }

    /**
     * @see Affix#doPostAttack(ItemStack, LootRarity, float, LivingEntity, Entity)
     */
    public void doPostAttack(LivingEntity user, Entity target) {
        this.getAffix().doPostAttack(this, user, target);
    }

    /**
     * @see Affix#doPostHurt(ItemStack, LootRarity, float, LivingEntity, Entity)
     */
    public void doPostHurt(LivingEntity user, DamageSource source) {
        this.getAffix().doPostHurt(this, user, source);
    }

    /**
     * @see Affix#onProjectileFired(ItemStack, LootRarity, float, LivingEntity, Projectile)
     */
    public void onProjectileFired(LivingEntity user, Projectile proj) {
        this.getAffix().onProjectileFired(this, user, proj);
    }

    /**
     * @see Affix#onItemUse(ItemStack, LootRarity, float, UseOnContext)
     */
    @Nullable
    public InteractionResult onItemUse(UseOnContext ctx) {
        return this.getAffix().onItemUse(this, ctx);
    }

    /**
     * @see Affix#onShieldBlock(ItemStack, LootRarity, float, LivingEntity, DamageSource, float)
     */
    public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
        return this.getAffix().onShieldBlock(this, entity, source, amount);
    }

    /**
     * @see Affix#onBlockBreak(ItemStack, LootRarity, float, Player, LevelAccessor, BlockPos, BlockState)
     */
    public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        this.getAffix().onBlockBreak(this, player, world, pos, state);
    }

    /**
     * @see Affix#getDurabilityBonusPercentage(ItemStack, LootRarity, float, ServerPlayer)
     */
    public float getDurabilityBonusPercentage() {
        return this.getAffix().getDurabilityBonusPercentage(this);
    }

    /**
     * @see Affix#onProjectileImpact(Projectile, LootRarity, float, HitResult, net.minecraft.world.phys.HitResult.Type)
     */
    public void onProjectileImpact(Projectile proj, HitResult res, HitResult.Type type) {
        this.getAffix().onProjectileImpact(this.level, this.getRarity(), proj, res, type);
    }

    /**
     * @see Affix#enablesTelepathy()
     */
    public boolean enablesTelepathy() {
        return this.getAffix().enablesTelepathy();
    }

    /**
     * @see Affix#onHurt(ItemStack, LootRarity, float, DamageSource, LivingEntity, float)
     */
    public float onHurt(DamageSource src, LivingEntity ent, float amount) {
        return this.getAffix().onHurt(this, src, ent, amount);
    }

    /**
     * @see Affix#getEnchantmentLevels(AffixInstance, Map)
     */
    public void getEnchantmentLevels(GetEnchantmentLevelEvent event) {
        this.getAffix().getEnchantmentLevels(this, event);
    }

    /**
     * @see Affix#modifyLoot(AffixInstance, ObjectArrayList, LootContext)
     */
    public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        this.getAffix().modifyLoot(this, loot, ctx);
    }

    /**
     * @see Affix#modifyEntityLoot(AffixInstance, LivingDropsEvent)
     */
    public void modifyEntityLoot(LivingDropsEvent e) {
        this.getAffix().modifyEntityLoot(this, e);
    }

    /**
     * @see Affix#isLevelIndependent(AffixInstance)
     */
    public boolean isLevelIndependent() {
        return this.getAffix().isLevelIndependent(this);
    }

    public AffixInstance withNewLevel(float level) {
        return new AffixInstance(this.affix, Mth.clamp(level, 0, Affix.MAX_LEVEL), this.rarity, this.stack);
    }

    public ResourceLocation makeUniqueId(String salt) {
        return Affix.makeUniqueId(this, salt);
    }

    public ResourceLocation makeUniqueId() {
        return Affix.makeUniqueId(this);
    }
}
