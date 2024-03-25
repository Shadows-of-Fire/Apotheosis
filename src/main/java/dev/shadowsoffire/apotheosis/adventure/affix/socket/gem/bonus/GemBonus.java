package dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus;

import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.special.AllStatsBonus;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.special.BloodyArrowBonus;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.special.DropTransformBonus;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.special.LeechBlockBonus;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.special.MageSlayerBonus;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.events.GetEnchantmentLevelEvent;
import dev.shadowsoffire.placebo.util.StepFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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

public abstract class GemBonus implements CodecProvider<GemBonus> {

    // TODO: Convert to Registry<Codec<?>> instead of using a raw codec map.
    public static final CodecMap<GemBonus> CODEC = new CodecMap<>("Gem Bonus");
    public static final Codec<Map<LootRarity, StepFunction>> VALUES_CODEC = LootRarity.mapCodec(StepFunction.CODEC);

    protected final ResourceLocation id;
    protected final GemClass gemClass;

    public GemBonus(ResourceLocation id, GemClass gemClass) {
        this.id = id;
        this.gemClass = gemClass;
    }

    /**
     * Validates that this gem bonus has been deserialized into a valid state.
     * If not, throws an error.
     *
     * @return this
     * @apiNote Overriders should strongly-type to their class.
     */
    public abstract GemBonus validate();

    /**
     * Checks if this bonus supports the rarity.
     *
     * @param rarity The rarity being checked.
     * @return True, if this bonus contains values for the specified rarity.
     * @apiNote Other methods in this class will throw an exception if the bonus does not support the rarity.
     */
    public abstract boolean supports(LootRarity rarity);

    /**
     * Returns the number of UUIDs that need to be generated for this Gem to operate properly.<br>
     * This should be equal to the maximum amount of attribute modifiers that need to be generated for proper usage.
     */
    public abstract int getNumberOfUUIDs();

    /**
     * Gets the one-line socket bonus tooltip.
     *
     * @param gem    The gem stack.
     * @param rarity The rarity of the gem.
     */
    public abstract Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity);

    /**
     * Retrieve the modifiers from this bonus to be applied to the socketed stack.<br>
     * This method will be called once for each slot based on the category this bonus is for.
     * <p>
     * For modifiers created here, they should use the UUIDs from {@link GemItem.getUUIDs(gem)}
     *
     * @param gem    The gem stack.
     * @param rarity The rarity of the gem.
     * @param map    The destination for generated attribute modifiers.
     */
    public void addModifiers(ItemStack gem, LootRarity rarity, BiConsumer<Attribute, AttributeModifier> map) {}

    /**
     * Calculates the protection value of this bonus, with respect to the given damage source.
     *
     * @param gem    The gem stack.
     * @param rarity The rarity of the gem.
     * @param source The damage source to compare against.
     * @return How many protection points this affix is worth against this source.
     */
    public int getDamageProtection(ItemStack gem, LootRarity rarity, DamageSource source) {
        return 0;
    }

    /**
     * Calculates the additional damage this bonus provides.
     * This damage is dealt as player physical damage.
     *
     * @param gem    The gem stack.
     * @param rarity The rarity of the gem.
     * @param type   The type of the mob.
     */
    public float getDamageBonus(ItemStack gem, LootRarity rarity, MobType type) {
        return 0.0F;
    }

    /**
     * Called when someone attacks an entity with an item that has this bonus.<br>
     * Specifically, this is invoked whenever the user attacks a target, while having an item with this bonus in either hand or any armor slot.
     *
     * @param gem    The gem stack.
     * @param rarity The rarity of the gem.
     * @param user   The wielder of the weapon. The weapon stack will be in their main hand.
     * @param target The target entity being attacked.
     */
    public void doPostAttack(ItemStack gem, LootRarity rarity, LivingEntity user, @Nullable Entity target) {}

    /**
     * Called when an entity that has this bonus on one of its armor items is damaged.
     *
     * @param gem      The gem stack.
     * @param rarity   The rarity of the gem.
     * @param user     The entity wearing an itme with this bonus.
     * @param attacker The entity attacking the user.
     */
    public void doPostHurt(ItemStack gem, LootRarity rarity, LivingEntity user, @Nullable Entity attacker) {}

    /**
     * Called when a user fires an arrow from a bow or crossbow with this affix on it.
     */
    public void onArrowFired(ItemStack gem, LootRarity rarity, LivingEntity user, AbstractArrow arrow) {}

    /**
     * Called when {@link Item#onItemUse(ItemUseContext)} would be called for an item with this affix.
     * Return null to not impact the original result type.
     */
    @Nullable
    public InteractionResult onItemUse(ItemStack gem, LootRarity rarity, UseOnContext ctx) {
        return null;
    }

    /**
     * Called when an arrow that was marked with this affix hits a target.
     */
    public void onArrowImpact(ItemStack gemStack, LootRarity rarity, AbstractArrow arrow, HitResult res, HitResult.Type type) {}

    /**
     * Called when a shield with this affix blocks some amount of damage.
     *
     * @param entity The blocking entity.
     * @param source The damage source being blocked.
     * @param amount The amount of damage blocked.
     * @param purity The purity of this gem.
     * @return The amount of damage that is *actually* blocked by the shield, after this affix applies.
     */
    public float onShieldBlock(ItemStack gem, LootRarity rarity, LivingEntity entity, DamageSource source, float amount) {
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
    public void onBlockBreak(ItemStack gem, LootRarity rarity, Player player, LevelAccessor world, BlockPos pos, BlockState state) {

    }

    /**
     * Allows an affix to reduce durability damage to an item.
     *
     * @param gem    The stack representing this gem.
     * @param rarity The rarity of the item.
     * @param level  The level of the affix.
     * @param user   The user of the item, if applicable.
     * @return The percentage [0, 1] of durability damage to ignore. This value will be summed with all other affixes that increase it.
     */
    public float getDurabilityBonusPercentage(ItemStack gem, LootRarity rarity, @Nullable ServerPlayer user) {
        return 0;
    }

    /**
     * Fires during the {@link LivingHurtEvent}, and allows for modification of the damage value.<br>
     * If the value is set to zero or below, the event will be cancelled.
     *
     * @param gem    The stack representing this gem.
     * @param rarity The rarity of the item.
     * @param level  The level of the affix.
     * @param src    The Damage Source of the attack.
     * @param user   The entity being attacked.
     * @param amount The amount of damage that is to be taken.
     * @return The amount of damage that will be taken, after modification. This value will propagate to other bonuses.
     */
    public float onHurt(ItemStack gem, LootRarity rarity, DamageSource src, LivingEntity user, float amount) {
        return amount;
    }

    /**
     * Fires during {@link GetEnchantmentLevelEvent} and allows for increasing enchantment levels.
     *
     * @param gem    The stack representing this gem.
     * @param rarity The rarity of the item.
     * @param level  The level of the affix.
     * @param ench   The enchantment being queried for.
     * @return The bonus level to be added to the current enchantment.
     */
    public void getEnchantmentLevels(ItemStack gem, LootRarity rarity, Map<Enchantment, Integer> enchantments) {}

    /**
     * Fires from {@link LootModifier#apply(ObjectArrayList, LootContext)} when this bonus is active on the tool given by the context.
     *
     * @param gem    The gem itemstack.
     * @param rarity The rarity of the gem.
     * @param loot   The generated loot.
     * @param ctx    The loot context.
     */
    public void modifyLoot(ItemStack gem, LootRarity rarity, ObjectArrayList<ItemStack> loot, LootContext ctx) {}

    public ResourceLocation getId() {
        return this.id;
    }

    public GemClass getGemClass() {
        return this.gemClass;
    }

    /**
     * Generates an ID for use with {@link Affix#isOnCooldown} / {@link Affix#startCooldown}
     */
    protected final ResourceLocation getCooldownId(ItemStack gemStack) {
        ResourceLocation gemId = GemItem.getGem(gemStack).getId();
        return new ResourceLocation(gemId.getNamespace(), gemId.getPath() + "/" + this.getId().toLanguageKey());
    }

    protected static <T extends GemBonus> App<RecordCodecBuilder.Mu<T>, GemClass> gemClass() {
        return GemClass.CODEC.fieldOf("gem_class").forGetter(GemBonus::getGemClass);
    }

    public static void initCodecs() {
        register("attribute", AttributeBonus.CODEC);
        register("multi_attribute", MultiAttrBonus.CODEC);
        register("durability", DurabilityBonus.CODEC);
        register("damage_reduction", DamageReductionBonus.CODEC);
        register("enchantment", EnchantmentBonus.CODEC);
        register("bloody_arrow", BloodyArrowBonus.CODEC);
        register("leech_block", LeechBlockBonus.CODEC);
        register("all_stats", AllStatsBonus.CODEC);
        register("drop_transform", DropTransformBonus.CODEC);
        register("mageslayer", MageSlayerBonus.CODEC);
        register("mob_effect", PotionBonus.CODEC);
    }

    private static void register(String id, Codec<? extends GemBonus> codec) {
        CODEC.register(Apotheosis.loc(id), codec);
    }

}
