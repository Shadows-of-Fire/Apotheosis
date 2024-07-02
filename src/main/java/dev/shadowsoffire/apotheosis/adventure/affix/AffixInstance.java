package dev.shadowsoffire.apotheosis.adventure.affix;

import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;

/**
 * An Affix Instance is a wrapper around the necessary parameters for all affix methods.<br>
 * Prefer using this over directly invoking methods on {@link Affix}.
 */
public record AffixInstance(DynamicHolder<? extends Affix> affix, ItemStack stack, DynamicHolder<LootRarity> rarity, float level) {

    public boolean isValid() {
        return this.affix.isBound() && this.rarity.isBound();
    }

    private Affix afx() {
        return this.affix.get();
    }

    private LootRarity rty() {
        return this.rarity.get();
    }

    /**
     * @see Affix#addModifiers(ItemStack, LootRarity, float, EquipmentSlot, BiConsumer)
     */
    public void addModifiers(EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
        this.afx().addModifiers(this.stack, this.rty(), this.level, type, map);
    }

    /**
     * @see Affix#getDescription(ItemStack, LootRarity, float)
     */
    public MutableComponent getDescription() {
        return this.afx().getDescription(this.stack, this.rty(), this.level);
    }

    /**
     * @see Affix#getAugmentingText(ItemStack, LootRarity, float)
     */
    public Component getAugmentingText() {
        return this.afx().getAugmentingText(this.stack, this.rty(), this.level);
    }

    /**
     * @see Affix#getName(boolean)
     */
    public Component getName(boolean prefix) {
        return this.afx().getName(prefix);
    }

    /**
     * @see Affix#getDamageProtection(ItemStack, LootRarity, float, DamageSource)
     */
    public int getDamageProtection(DamageSource source) {
        return this.afx().getDamageProtection(this.stack, this.rty(), this.level, source);
    }

    /**
     * @see Affix#getDamageBonus(ItemStack, LootRarity, float, MobType)
     */
    public float getDamageBonus(MobType creatureType) {
        return this.afx().getDamageBonus(this.stack, this.rty(), this.level, creatureType);
    }

    /**
     * @see Affix#doPostAttack(ItemStack, LootRarity, float, LivingEntity, Entity)
     */
    public void doPostAttack(LivingEntity user, @Nullable Entity target) {
        this.afx().doPostAttack(this.stack, this.rty(), this.level, user, target);
    }

    /**
     * @see Affix#doPostHurt(ItemStack, LootRarity, float, LivingEntity, Entity)
     */
    public void doPostHurt(LivingEntity user, @Nullable Entity attacker) {
        this.afx().doPostHurt(this.stack, this.rty(), this.level, user, attacker);
    }

    /**
     * @see Affix#onArrowFired(ItemStack, LootRarity, float, LivingEntity, AbstractArrow)
     */
    public void onArrowFired(LivingEntity user, AbstractArrow arrow) {
        this.afx().onArrowFired(this.stack, this.rty(), this.level, user, arrow);
    }

    /**
     * @see Affix#onItemUse(ItemStack, LootRarity, float, UseOnContext)
     */
    @Nullable
    public InteractionResult onItemUse(UseOnContext ctx) {
        return this.afx().onItemUse(this.stack, this.rty(), this.level, ctx);
    }

    /**
     * @see Affix#onShieldBlock(ItemStack, LootRarity, float, LivingEntity, DamageSource, float)
     */
    public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
        return this.afx().onShieldBlock(this.stack, this.rty(), this.level, entity, source, amount);
    }

    /**
     * @see Affix#onBlockBreak(ItemStack, LootRarity, float, Player, LevelAccessor, BlockPos, BlockState)
     */
    public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        this.afx().onBlockBreak(this.stack, this.rty(), this.level, player, world, pos, state);
    }

    /**
     * @see Affix#getDurabilityBonusPercentage(ItemStack, LootRarity, float, ServerPlayer)
     */
    public float getDurabilityBonusPercentage(@Nullable ServerPlayer user) {
        return this.afx().getDurabilityBonusPercentage(this.stack, this.rty(), this.level, user);
    }

    /**
     * @see Affix#onArrowImpact(AbstractArrow, LootRarity, float, HitResult, net.minecraft.world.phys.HitResult.Type)
     */
    public void onArrowImpact(AbstractArrow arrow, HitResult res, HitResult.Type type) {
        this.afx().onArrowImpact(arrow, this.rty(), this.level, res, type);
    }

    /**
     * @see Affix#enablesTelepathy()
     */
    public boolean enablesTelepathy() {
        return this.afx().enablesTelepathy();
    }

    /**
     * @see Affix#onHurt(ItemStack, LootRarity, float, DamageSource, LivingEntity, float)
     */
    public float onHurt(DamageSource src, LivingEntity ent, float amount) {
        return this.afx().onHurt(this.stack, this.rty(), this.level, src, ent, amount);
    }

    /**
     * @see Affix#getEnchantmentLevels(ItemStack, LootRarity, float, Map)
     */
    public void getEnchantmentLevels(Map<Enchantment, Integer> enchantments) {
        this.afx().getEnchantmentLevels(this.stack, this.rty(), this.level, enchantments);
    }

    /**
     * @see Affix#modifyLoot(ItemStack, LootRarity, float, ObjectArrayList, LootContext)
     */
    public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        this.afx().modifyLoot(this.stack, this.rty(), this.level, loot, ctx);
    }

    public AffixInstance withNewLevel(float level) {
        return new AffixInstance(this.affix, this.stack, this.rarity, Mth.clamp(level, 0, 1));
    }
}
