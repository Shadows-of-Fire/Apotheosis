package shadows.apotheosis.adventure.affix.socket.gem.bonus;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.SimpleMapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatRules;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemClass;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.placebo.util.StepFunction;

public abstract class GemBonus {

	public static final SimpleMapCodec<LootRarity, StepFunction> VALUES_CODEC = LootRarity.mapCodec(StepFunction.CODEC);

	protected final ResourceLocation id;
	protected final GemClass gemClass;

	public GemBonus(ResourceLocation id, GemClass gemClass) {
		this.id = id;
		this.gemClass = gemClass;
	}

	/**
	 * Gets the one-line socket bonus tooltip.  This will automatically be called in the correct place.<br>
	 * If you want to override the entire tooltip as shown on the gem item, override {@link Gem#addInformation} 
	 * @param gem      The gem stack.
	 * @param purity   The purity of this gem.
	 * @param tooltips The destination for tooltips.
	 */
	public abstract Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity, int facets);

	/**
	 * Returns the max number of facets available for this gem.<br>
	 * Facets are a user-facing wrapper on the purity (level), because most gems do not change on specify purity percentages.
	 */
	public abstract int getMaxFacets(LootRarity rarity);

	/**
	 * Validates that this gem bonus has been deserialized into a valid state.
	 * If not, throws an error.
	 * @return this
	 * @apiNote Overriders should strongly-type to their class.
	 */
	public abstract GemBonus validate();

	/**
	 * Returns true if this gem bonus is valid at the specified rarity.
	 */
	public abstract boolean supports(LootRarity rarity);

	/**
	 * Returns the number of UUIDs that need to be generated for this Gem to operate properly.<br>
	 * This should be equal to the maximum amount of attribute modifiers that need to be generated for proper usage.
	 */
	public int getNumberOfUUIDs() {
		return 1;
	}

	/**
	 * Retrieve the modifiers from this affix to be applied to the itemstack.
	 * @param stack  The stack the affix is on.
	 * @param purity The purity of this gem.
	 * @param type   The slot type for modifiers being gathered.
	 * @param map    The destination for generated attribute modifiers.
	 */
	public void addModifiers(ItemStack gem, LootRarity rarity, int facets, BiConsumer<Attribute, AttributeModifier> map) {
	}

	/**
	 * Calculates the protection value of this affix, with respect to the given damage source.<br>
	 * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}<br>
	 * Ench module overrides with {@link EnchHooks#getDamageAfterMagicAbsorb}<br>
	 * @param purity The purity of this gem. if applicable.<br>
	 * @param source The damage source to compare against.<br>
	 * @return How many protection points this affix is worth against this source.<br>
	 */
	public int getDamageProtection(ItemStack gem, LootRarity rarity, int facets, DamageSource source) {
		return 0;
	}

	/**
	 * Calculates the additional damage this affix deals.
	 * This damage is dealt as player physical damage, and is not impacted by critical strikes.
	 */
	public float getDamageBonus(ItemStack gem, LootRarity rarity, int facets, MobType creatureType) {
		return 0.0F;
	}

	/**
	 * Called when someone attacks an entity with an item containing this affix.
	 * More specifically, this is invoked whenever the user attacks a target, while having an item with this affix in either hand or any armor slot.
	 * @param user   The wielder of the weapon.  The weapon stack will be in their main hand.
	 * @param target The target entity being attacked.
	 * @param purity The purity of this gem. if applicable.
	 */
	public void doPostAttack(ItemStack gem, LootRarity rarity, int facets, LivingEntity user, @Nullable Entity target) {
	}

	/**
	 * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be
	 * called.
	 */
	public void doPostHurt(ItemStack gem, LootRarity rarity, int facets, LivingEntity user, @Nullable Entity attacker) {
	}

	/**
	 * Called when a user fires an arrow from a bow or crossbow with this affix on it.
	 */
	public void onArrowFired(ItemStack gem, LootRarity rarity, int facets, LivingEntity user, AbstractArrow arrow) {
	}

	/**
	 * Called when {@link Item#onItemUse(ItemUseContext)} would be called for an item with this affix.
	 * Return null to not impact the original result type.
	 */
	@Nullable
	public InteractionResult onItemUse(ItemStack gem, LootRarity rarity, int facets, UseOnContext ctx) {
		return null;
	}

	/**
	 * Called when an arrow that was marked with this affix hits a target.
	 */
	public void onArrowImpact(AbstractArrow arrow, LootRarity rarity, int facets, HitResult res, HitResult.Type type) {
	}

	/**
	 * Called when a shield with this affix blocks some amount of damage.
	 * @param entity The blocking entity.
	 * @param source The damage source being blocked.
	 * @param amount The amount of damage blocked.
	 * @param purity The purity of this gem.
	 * @return	     The amount of damage that is *actually* blocked by the shield, after this affix applies.
	 */
	public float onShieldBlock(ItemStack gem, LootRarity rarity, int facets, LivingEntity entity, DamageSource source, float amount) {
		return amount;
	}

	/**
	 * Called when a player with this affix breaks a block.
	 * @param player The breaking player.
	 * @param world  The level the block was broken in.
	 * @param pos    The position of the block.
	 * @param state  The state that was broken.
	 */
	public void onBlockBreak(ItemStack gem, LootRarity rarity, int facets, Player player, LevelAccessor world, BlockPos pos, BlockState state) {

	}

	/**
	 * Allows an affix to reduce durability damage to an item.
	 * @param stack   The stack with the affix.
	 * @param rarity  The rarity of the item.
	 * @param level   The level of the affix.
	 * @param user    The user of the item, if applicable.
	 * @return        The percentage [0, 1] of durability damage to ignore. This value will be summed with all other affixes that increase it.
	 */
	public float getDurabilityBonusPercentage(ItemStack gem, LootRarity rarity, int facets, ServerPlayer user) {
		return 0;
	}

	/**
	 * Fires during the {@link LivingHurtEvent}, and allows for modification of the damage value.<br>
	 * If the value is set to zero or below, the event will be cancelled.
	 * @param stack   The stack with the affix.
	 * @param rarity  The rarity of the item.
	 * @param level   The level of the affix.
	 * @param src     The Damage Source of the attack.
	 * @param ent     The entity being attacked.
	 * @param amount  The amount of damage that is to be taken.
	 * @return        The amount of damage that will be taken, after modification. This value will propagate to other affixes.
	 */
	public float onHurt(ItemStack gem, LootRarity rarity, int facets, DamageSource src, LivingEntity ent, float amount) {
		return amount;
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public GemClass getGemClass() {
		return this.gemClass;
	}

	protected static <T extends GemBonus> App<RecordCodecBuilder.Mu<T>, GemClass> gemClass() {
		return GemClass.CODEC.fieldOf("gem_class").forGetter(gem -> gem.getGemClass());
	}

}
