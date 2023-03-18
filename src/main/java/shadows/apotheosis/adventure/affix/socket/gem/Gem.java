package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
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
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.socket.SocketHelper;
import shadows.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.placebo.codec.PlaceboCodecs;
import shadows.placebo.events.GetEnchantmentLevelEvent;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

public class Gem extends TypeKeyedBase<Gem> implements ILuckyWeighted, IDimensional, LootRarity.Clamped, IStaged {

	//Formatter::off
	public static final Codec<Gem> CODEC = RecordCodecBuilder.create(inst -> 
		inst.group(
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
			Codec.FLOAT.fieldOf("quality").forGetter(ILuckyWeighted::getQuality),
			PlaceboCodecs.setCodec(ResourceLocation.CODEC).optionalFieldOf("dimensions", Collections.emptySet()).forGetter(IDimensional::getDimensions),
			LootRarity.CODEC.optionalFieldOf("min_rarity", LootRarity.COMMON).forGetter(LootRarity.Clamped::getMinRarity),
			LootRarity.CODEC.optionalFieldOf("max_rarity", LootRarity.MYTHIC).forGetter(LootRarity.Clamped::getMaxRarity),
			GemManager.gemBonusCodec().listOf().fieldOf("bonuses").forGetter(Gem::getBonuses),
			Codec.BOOL.optionalFieldOf("unique", false).forGetter(Gem::isUnique),
			PlaceboCodecs.setCodec(Codec.STRING).optionalFieldOf("stages").forGetter(gem -> Optional.ofNullable(gem.getStages())))
			.apply(inst, Gem::new)
		);
	
	//Formatter::on

	protected final int weight;
	protected final float quality;
	protected final Set<ResourceLocation> dimensions;
	protected final List<GemBonus> bonuses;
	protected final boolean unique;
	protected final @Nullable Set<String> stages;

	protected transient final Map<LootCategory, GemBonus> bonusMap;
	protected transient final int uuidsNeeded;
	protected transient final LootRarity minRarity, maxRarity;

	public Gem(int weight, float quality, Set<ResourceLocation> dimensions, @Nullable LootRarity minRarity, @Nullable LootRarity maxRarity, List<GemBonus> bonuses, boolean unique, Optional<Set<String>> stages) {
		this.weight = weight;
		this.quality = quality;
		this.dimensions = dimensions;
		this.bonuses = bonuses;
		this.unique = unique;
		this.stages = stages.orElse(null);
		this.bonusMap = bonuses.stream().<Pair<LootCategory, GemBonus>>mapMulti((gemData, mapper) -> {
			for (LootCategory c : gemData.getGemClass().types()) {
				mapper.accept(Pair.of(c, gemData));
			}
		}).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
		this.uuidsNeeded = this.bonuses.stream().mapToInt(GemBonus::getNumberOfUUIDs).max().orElse(0);
		if (!this.bonuses.isEmpty()) {
			this.minRarity = LootRarity.values().stream().filter(bonuses.get(0)::supports).min(LootRarity::compareTo).get();
			this.maxRarity = LootRarity.values().stream().filter(bonuses.get(0)::supports).max(LootRarity::compareTo).get();
		} else {
			this.minRarity = this.maxRarity = LootRarity.COMMON;
		}
	}

	/**
	 * Returns the number of UUIDs that need to be generated for this Gem to operate properly.<br>
	 * This should be equal to the maximum amount of attribute modifiers that need to be generated for proper usage.
	 */
	public int getNumberOfUUIDs() {
		return this.uuidsNeeded;
	}

	/**
	 * Retrieve the modifiers from this affix to be applied to the itemstack.
	 * @param socketed  The stack the gem is sockted into.
	 * @param purity    The purity of this gem.
	 * @param type      The slot type for modifiers being gathered.
	 * @param map       The destination for generated attribute modifiers.
	 */
	public void addModifiers(ItemStack socketed, ItemStack gem, LootRarity rarity, int facets, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> map) {
		LootCategory cat = LootCategory.forItem(socketed);
		if (cat.isNone()) {
			AdventureModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getId(), socketed.getHoverName().getString());
			return;
		}
		for (EquipmentSlot itemSlot : cat.getSlots(socketed)) {
			if (itemSlot == slot) {
				getBonus(socketed).ifPresent(b -> b.addModifiers(gem, rarity, facets, map));
			}
		}
	}

	/**
	 * Adds all tooltip data from this gem to the gem stack.
	 * @param gem      The gem stack.
	 * @param purity   The purity of this gem.
	 * @param tooltips The destination for tooltips.
	 */
	public void addInformation(ItemStack gem, LootRarity rarity, int facets, Consumer<Component> list) {
		if (this.isUnique()) list.accept(Component.translatable("text.apotheosis.unique").withStyle(Style.EMPTY.withColor(0xC73912)));
		list.accept(Component.translatable("text.apotheosis.facets", 4 + facets).withStyle(Style.EMPTY.withColor(0xAEA2D6)));
		list.accept(CommonComponents.EMPTY);
		Style style = Style.EMPTY.withColor(0x0AFF0A);
		list.accept(Component.translatable("text.apotheosis.socketable_into").withStyle(style));
		addTypeInfo(list, this.bonusMap.keySet().toArray());
		list.accept(CommonComponents.EMPTY);
		if (this.bonuses.size() == 1) {
			list.accept(Component.translatable("item.modifiers.socket").withStyle(ChatFormatting.GOLD));
			list.accept(this.bonuses.get(0).getSocketBonusTooltip(gem, rarity, facets));
		} else {
			list.accept(Component.translatable("item.modifiers.socket_in").withStyle(ChatFormatting.GOLD));
			for (GemBonus bonus : this.bonuses) {
				Component modifComp = bonus.getSocketBonusTooltip(gem, rarity, facets);
				Component sum = Component.translatable("text.apotheosis.dot_prefix", Component.translatable("%s: %s", Component.translatable("gem_class." + bonus.getGemClass().key()), modifComp)).withStyle(ChatFormatting.GOLD);
				list.accept(sum);
			}
		}
	}

	/**
	 * Gets the one-line socket bonus tooltip.  This will automatically be called in the correct place.<br>
	 * If you want to override the entire tooltip as shown on the gem item, override {@link Gem#addInformation} 
	 * @param socketed The item the gem is socketed into.
	 * @param gem      The gem stack.
	 * @param purity   The purity of this gem.
	 * @param tooltips The destination for tooltips.
	 */
	public Component getSocketBonusTooltip(ItemStack socketed, ItemStack gem, LootRarity rarity, int facets) {
		return getBonus(socketed).map(b -> b.getSocketBonusTooltip(gem, rarity, facets)).orElse(Component.literal("Invalid Gem Category"));
	}

	/**
	 * Returns the max number of facets available for this gem.<br>
	 * Facets are a user-facing wrapper on the purity (level), because most gems do not change on specify purity percentages.
	 */
	public int getMaxFacets(LootRarity rarity) {
		return bonuses.stream().mapToInt(b -> b.getMaxFacets(rarity)).max().orElse(0);
	}

	/**
	 * Checks if this gem can be applied to an item, preventing more than one unique.
	 * @param stack The target item.
	 * @param rarity The rarity of the gem.
	 * @param gem The gem
	 * @return If this gem can be socketed into the item.
	 */
	public boolean canApplyTo(ItemStack stack, ItemStack gem, LootRarity rarity) {
		if (this.isUnique()) {
			List<Gem> gems = SocketHelper.getActiveGems(stack);
			if (gems.contains(this)) return false;
		}
		return isValidIn(stack, gem, rarity);
	}

	/**
	 * Checks if this gem is legally socketed into an item.  Does not validate uniques
	 * @param stack The target item.
	 * @param rarity The rarity of the gem.
	 * @param gem The gem
	 * @return If this gem can be socketed into the item.
	 */
	public boolean isValidIn(ItemStack stack, ItemStack gem, LootRarity rarity) {
		LootCategory cat = LootCategory.forItem(stack);
		return !cat.isNone() && bonusMap.containsKey(cat) && bonusMap.get(cat).supports(rarity);
	}

	/**
	 * Calculates the protection value of this affix, with respect to the given damage source.<br>
	 * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}<br>
	 * Ench module overrides with {@link EnchHooks#getDamageAfterMagicAbsorb}<br>
	 * @param purity The purity of this gem. if applicable.<br>
	 * @param source The damage source to compare against.<br>
	 * @return How many protection points this affix is worth against this source.<br>
	 */
	public int getDamageProtection(ItemStack stack, ItemStack gem, LootRarity rarity, int facets, DamageSource source) {
		return getBonus(stack).map(b -> b.getDamageProtection(gem, rarity, facets, source)).orElse(0);
	}

	/**
	 * Calculates the additional damage this affix deals.
	 * This damage is dealt as player physical damage, and is not impacted by critical strikes.
	 */
	public float getDamageBonus(ItemStack stack, ItemStack gem, LootRarity rarity, int facets, MobType creatureType) {
		return getBonus(stack).map(b -> b.getDamageBonus(gem, rarity, facets, creatureType)).orElse(0F);
	}

	/**
	 * Called when someone attacks an entity with an item containing this affix.
	 * More specifically, this is invoked whenever the user attacks a target, while having an item with this affix in either hand or any armor slot.
	 * @param user   The wielder of the weapon.  The weapon stack will be in their main hand.
	 * @param target The target entity being attacked.
	 * @param purity The purity of this gem. if applicable.
	 */
	public void doPostAttack(ItemStack stack, ItemStack gem, LootRarity rarity, int facets, LivingEntity user, @Nullable Entity target) {
		getBonus(stack).ifPresent(b -> b.doPostAttack(gem, rarity, facets, user, target));
	}

	/**
	 * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be
	 * called.
	 */
	public void doPostHurt(ItemStack stack, ItemStack gem, LootRarity rarity, int facets, LivingEntity user, @Nullable Entity attacker) {
		getBonus(stack).ifPresent(b -> b.doPostHurt(gem, rarity, facets, user, attacker));
	}

	/**
	 * Called when a user fires an arrow from a bow or crossbow with this affix on it.
	 */
	public void onArrowFired(ItemStack stack, ItemStack gem, LootRarity rarity, int facets, LivingEntity user, AbstractArrow arrow) {
		getBonus(stack).ifPresent(b -> b.onArrowFired(gem, rarity, facets, user, arrow));
	}

	/**
	 * Called when {@link Item#onItemUse(ItemUseContext)} would be called for an item with this affix.
	 * Return null to not impact the original result type.
	 */
	@Nullable
	public InteractionResult onItemUse(ItemStack stack, ItemStack gem, LootRarity rarity, int facets, UseOnContext ctx) {
		return getBonus(stack).map(b -> b.onItemUse(gem, rarity, facets, ctx)).orElse(null);
	}

	/**
	 * Called when an arrow that was marked with this affix hits a target.
	 */
	public void onArrowImpact(AbstractArrow arrow, ItemStack gem, LootRarity rarity, int facets, HitResult res, HitResult.Type type) {
		//TODO: getBonus(arrow).ifPresent(b -> b.onArrowImpact(gem, facets, arrow, res, type));
	}

	/**
	 * Called when a shield with this affix blocks some amount of damage.
	 * @param entity The blocking entity.
	 * @param source The damage source being blocked.
	 * @param amount The amount of damage blocked.
	 * @param purity The purity of this gem.
	 * @return	     The amount of damage that is *actually* blocked by the shield, after this affix applies.
	 */
	public float onShieldBlock(ItemStack stack, ItemStack gem, LootRarity rarity, int facets, LivingEntity entity, DamageSource source, float amount) {
		return getBonus(stack).map(b -> b.onShieldBlock(gem, rarity, facets, entity, source, amount)).orElse(amount);
	}

	/**
	 * Called when a player with this affix breaks a block.
	 * @param player The breaking player.
	 * @param world  The level the block was broken in.
	 * @param pos    The position of the block.
	 * @param state  The state that was broken.
	 */
	public void onBlockBreak(ItemStack stack, ItemStack gem, LootRarity rarity, int facets, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		getBonus(stack).ifPresent(b -> b.onBlockBreak(gem, rarity, facets, player, world, pos, state));
	}

	/**
	 * Allows an affix to reduce durability damage to an item.
	 * @param stack   The stack with the affix.
	 * @param rarity  The rarity of the item.
	 * @param level   The level of the affix.
	 * @param user    The user of the item, if applicable.
	 * @return        The percentage [0, 1] of durability damage to ignore. This value will be summed with all other affixes that increase it.
	 */
	public float getDurabilityBonusPercentage(ItemStack socketed, ItemStack gemStack, LootRarity lootRarity, int facets, ServerPlayer user) {
		return getBonus(socketed).map(b -> b.getDurabilityBonusPercentage(gemStack, lootRarity, facets, user)).orElse(0F);
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
	public float onHurt(ItemStack socketed, ItemStack gemStack, LootRarity rarity, int facets, DamageSource src, LivingEntity ent, float amount) {
		return getBonus(socketed).map(b -> b.onHurt(gemStack, rarity, facets, src, ent, amount)).orElse(amount);
	}

	/**
	 * Fires during {@link GetEnchantmentLevelEvent} and allows for increasing enchantment levels.
	 * @param stack   The stack with the affix.
	 * @param rarity  The rarity of the item.
	 * @param level   The level of the affix.
	 * @param ench    The enchantment being queried for.
	 * @return        The bonus level to be added to the current enchantment.
	 */
	public void getEnchantmentLevels(ItemStack socketed, ItemStack gemStack, LootRarity rarity, int facets, Map<Enchantment, Integer> enchantments) {
		getBonus(socketed).ifPresent(b -> b.getEnchantmentLevels(gemStack, rarity, facets, enchantments));
	}

	protected Optional<GemBonus> getBonus(ItemStack stack) {
		return Optional.ofNullable(this.bonusMap.get(LootCategory.forItem(stack)));
	}

	@Override
	public String toString() {
		return String.format("Gem: %s", this.getId());
	}

	public static String fmt(float f) {
		return Affix.fmt(f);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Gem gem && gem.getId().equals(this.getId());
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public Set<ResourceLocation> getDimensions() {
		return this.dimensions;
	}

	@Override
	public LootRarity getMaxRarity() {
		return this.maxRarity;
	}

	@Override
	public LootRarity getMinRarity() {
		return this.minRarity;
	}

	public List<GemBonus> getBonuses() {
		return this.bonuses;
	}

	public boolean isUnique() {
		return this.unique;
	}

	public Gem validate() {
		Preconditions.checkArgument(this.weight >= 0, "Gem " + this.getId() + " has a negative weight");
		Preconditions.checkArgument(this.quality >= 0, "Gem " + this.getId() + " has a negative quality");
		Preconditions.checkNotNull(this.dimensions);
		Preconditions.checkArgument(maxRarity.ordinal() >= minRarity.ordinal());
		LootRarity.values().stream().filter(r -> r.isAtLeast(this.minRarity) && r.isAtMost(this.maxRarity)).forEach(r -> {
			Preconditions.checkArgument(this.bonuses.stream().allMatch(b -> b.supports(r)));
		});
		return this;
	}

	@Override
	public Set<String> getStages() {
		return this.stages;
	}

	public static void addTypeInfo(Consumer<Component> list, Object... types) {
		Arrays.sort(types, (c1, c2) -> ((LootCategory) c1).getName().compareTo(((LootCategory) c2).getName()));
		Style style = Style.EMPTY.withColor(0x0AFF0A);
		if (types.length != LootCategory.BY_ID.size() - 1) {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			while (i < types.length) {
				int rem = Math.min(3, types.length - i);
				Object[] args = new Object[rem];
				for (int r = 0; r < rem; r++) {
					sb.append("%s, ");
					args[r] = Component.translatable(((LootCategory) types[i + r]).getDescIdPlural());
				}
				list.accept(Component.translatable("text.apotheosis.dot_prefix", Component.translatable(sb.substring(0, sb.length() - 2), args)).withStyle(style));
				sb.setLength(0);
				i += rem;
			}
		} else {
			list.accept(Component.translatable("text.apotheosis.dot_prefix", Component.translatable("text.apotheosis.anything")).withStyle(style));
		}
	}

}
