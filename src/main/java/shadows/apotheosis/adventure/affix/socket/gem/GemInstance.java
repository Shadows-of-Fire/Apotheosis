package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

/**
 * A Gem Instance is a live copy of a Gem with all context needed to call Gem methods.<br>
 * This is the Gem counterparty of {@link AffixInstance}.
 * <p>
 * The major difference between them is that most methods do not live on {@link Gem} but rather on {@link GemBonus}.
 *
 * @param gem      The socketed Gem.
 * @param cate     The LootCategory of the item the Gem is socketed into.
 * @param gemStack The itemstack form of the sockted Gem.
 * @param rarity   The rarity of the Gem. Not the rarity of the item the Gem is socketed into.
 */
public record GemInstance(Gem gem, LootCategory cat, ItemStack gemStack, LootRarity rarity) {
    /**
     * Creates a {@link GemInstance} for a socketed gem.
     *
     * @param socketed The item the gem is socketed in.
     * @param gemStack The stack representing the gem.
     */
    public static GemInstance socketed(ItemStack socketed, ItemStack gemStack) {
        Gem gem = GemItem.getGem(gemStack);
        LootRarity rarity = AffixHelper.getRarity(gemStack);

        if (gem != null && rarity != null) {
            rarity = gem.clamp(rarity);
        }

        return new GemInstance(gem, LootCategory.forItem(socketed), gemStack, rarity);
    }

    /**
     * Creates a {@link GemInstance} with {@link LootCategory#NONE}.<br>
     * This instance will be unable to invoke bonus methods, but may be used to easily retrieve the gem properties.
     */
    public static GemInstance unsocketed(ItemStack gemStack) {
        Gem gem = GemItem.getGem(gemStack);
        LootRarity rarity = AffixHelper.getRarity(gemStack);

        if (gem != null && rarity != null) {
            rarity = gem.clamp(rarity);
        }

        return new GemInstance(gem, LootCategory.NONE, gemStack, rarity);
    }

    /**
     * Checks if both the gem and rarity are not null.<br>
     * This should only be used in conjunction with {@link #unsocketed(ItemStack)}.<br>
     * Otherwise, use {@link #isValid()}.
     */
    public boolean isValidUnsocketed() {
        return this.gem != null && this.rarity != null;
    }

    /**
     * Checks if the gem and rarity are not null, and there is a valid bonus for the socketed category.<br>
     * Will always return false if using {@link #unsocketed(ItemStack)}
     */
    public boolean isValid() {
        return this.isValidUnsocketed() && this.gem.getBonus(this.cat).isPresent();
    }

    /**
     * Checks if the rarity of the gem stack is equal to the max rarity of the underlying Gem.
     */
    public boolean isMaxRarity() {
        return this.rarity() == this.gem.getMaxRarity();
    }

    /**
     * @see GemBonus#addModifiers(ItemStack, LootRarity, BiConsumer)
     */
    public void addModifiers(EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> map) {
        for (EquipmentSlot itemSlot : this.cat.getSlots()) {
            if (itemSlot == slot) {
                this.ifPresent(b -> b.addModifiers(this.gemStack, this.rarity, map));
            }
        }
    }

    /**
     * @see GemBonus#getSocketBonusTooltip(ItemStack, LootRarity)
     */
    public Component getSocketBonusTooltip() {
        return this.map(b -> b.getSocketBonusTooltip(this.gemStack, this.rarity)).orElse(Component.literal("Invalid Gem Category"));
    }

    /**
     * @see GemBonus#getDamageProtection(ItemStack, LootRarity, DamageSource)
     */
    public int getDamageProtection(DamageSource source) {
        return this.map(b -> b.getDamageProtection(this.gemStack, this.rarity, source)).orElse(0);
    }

    /**
     * @see GemBonus#getDamageBonus(ItemStack, LootRarity, MobType)
     */
    public float getDamageBonus(MobType creatureType) {
        return this.map(b -> b.getDamageBonus(this.gemStack, this.rarity, creatureType)).orElse(0F);
    }

    /**
     * @see GemBonus#doPostAttack(ItemStack, LootRarity, LivingEntity, Entity)
     */
    public void doPostAttack(LivingEntity user, @Nullable Entity target) {
        this.ifPresent(b -> b.doPostAttack(this.gemStack, this.rarity, user, target));
    }

    /**
     * @see GemBonus#doPostHurt(ItemStack, LootRarity, LivingEntity, Entity)
     */
    public void doPostHurt(LivingEntity user, @Nullable Entity attacker) {
        this.ifPresent(b -> b.doPostHurt(this.gemStack, this.rarity, user, attacker));
    }

    /**
     * @see GemBonus#onArrowFired(ItemStack, LootRarity, LivingEntity, AbstractArrow)
     */
    public void onArrowFired(LivingEntity user, AbstractArrow arrow) {
        this.ifPresent(b -> b.onArrowFired(this.gemStack, this.rarity, user, arrow));
    }

    /**
     * @see GemBonus#onItemUse(ItemStack, LootRarity, UseOnContext)
     */
    @Nullable
    public InteractionResult onItemUse(UseOnContext ctx) {
        return this.map(b -> b.onItemUse(this.gemStack, this.rarity, ctx)).orElse(null);
    }

    /**
     * @see {@link GemBonus#onArrowImpact(AbstractArrow, LootRarity, HitResult, HitResult.Type)}
     */
    public void onArrowImpact(AbstractArrow arrow, ItemStack gem, LootRarity rarity, HitResult res, HitResult.Type type) {
        // TODO: getBonus(arrow).ifPresent(b -> b.onArrowImpact(gem, arrow, res, type));
    }

    /**
     * @see GemBonus#onShieldBlock(ItemStack, LootRarity, LivingEntity, DamageSource, float)
     */
    public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
        return this.map(b -> b.onShieldBlock(this.gemStack, this.rarity, entity, source, amount)).orElse(amount);
    }

    /**
     * @see GemBonus#onBlockBreak(ItemStack, LootRarity, Player, LevelAccessor, BlockPos, BlockState)
     */
    public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        this.ifPresent(b -> b.onBlockBreak(this.gemStack, this.rarity, player, world, pos, state));
    }

    /**
     * @see GemBonus#getDurabilityBonusPercentage(ItemStack, LootRarity, ServerPlayer)
     */
    public float getDurabilityBonusPercentage(ServerPlayer user) {
        return this.map(b -> b.getDurabilityBonusPercentage(this.gemStack, this.rarity, user)).orElse(0F);
    }

    /**
     * @see GemBonus#onHurt(ItemStack, LootRarity, DamageSource, LivingEntity, float)
     */
    public float onHurt(DamageSource src, LivingEntity ent, float amount) {
        return this.map(b -> b.onHurt(this.gemStack, this.rarity, src, ent, amount)).orElse(amount);
    }

    /**
     * @see GemBonus#getEnchantmentLevels(ItemStack, LootRarity, Map)
     */
    public void getEnchantmentLevels(Map<Enchantment, Integer> enchantments) {
        this.ifPresent(b -> b.getEnchantmentLevels(this.gemStack, this.rarity, enchantments));
    }

    /**
     * @see GemBonus#modifyLoot(ItemStack, LootRarity, ObjectArrayList, LootContext)
     */
    public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        this.ifPresent(b -> b.modifyLoot(this.gemStack, this.rarity, loot, ctx));
    }

    private <T> Optional<T> map(Function<GemBonus, T> function) {
        return this.gem.getBonus(this.cat).map(function);
    }

    private void ifPresent(Consumer<GemBonus> function) {
        this.gem.getBonus(this.cat).ifPresent(function);
    }
}
