package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.annotations.SerializedName;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

public abstract class Gem extends TypeKeyedBase<Gem> implements ILuckyWeighted, IDimensional, LootRarity.Clamped {

	protected final GemVariant variant;
	protected final int weight;
	protected final float quality;
	protected final Set<ResourceLocation> dimensions;
	protected final Set<LootCategory> types;
	@Nullable
	protected LootRarity minRarity;
	@Nullable
	protected LootRarity maxRarity;

	public Gem(GemStub stub) {
		this.variant = stub.variant;
		this.weight = stub.weight;
		this.quality = stub.quality;
		this.dimensions = stub.dimensions;
		this.types = stub.types;
		this.minRarity = stub.minRarity;
		this.maxRarity = stub.maxRarity;
	}

	public GemVariant getVariant() {
		return this.variant;
	}

	/**
	 * Returns the number of UUIDs that need to be generated for this Gem to operate properly.<br>
	 * This should be equal to the maximum amount of attribute modifiers that need to be generated for proper usage.
	 */
	public int getNumberOfUUIDs() {
		return 1;
	}

	@Override
	public LootRarity getMaxRarity() {
		return this.maxRarity;
	}

	@Override
	public LootRarity getMinRarity() {
		return this.minRarity;
	}

	/**
	 * Retrieve the modifiers from this affix to be applied to the itemstack.
	 * @param stack  The stack the affix is on.
	 * @param purity The purity of this gem.
	 * @param type   The slot type for modifiers being gathered.
	 * @param map    The destination for generated attribute modifiers.
	 */
	public void addModifiers(ItemStack stack, LootRarity rarity, int facets, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map, ItemStack gem) {
	}

	/**
	 * Adds all tooltip data from this gem to the gem stack.
	 * @param gem      The gem stack.
	 * @param purity   The purity of this gem.
	 * @param tooltips The destination for tooltips.
	 */
	public void addInformation(ItemStack gem, LootRarity rarity, int facets, Consumer<Component> list) {
		list.accept(Component.translatable("text.apotheosis.facets", 4 + facets).withStyle(Style.EMPTY.withColor(0xAEA2D6)));
		list.accept(CommonComponents.EMPTY);
		Style style = Style.EMPTY.withColor(0x0AFF0A);
		list.accept(Component.translatable("text.apotheosis.socketable_into").withStyle(style));
		if (types != null && !types.isEmpty()) {
			for (LootCategory l : this.types) {
				list.accept(Component.translatable("text.apotheosis.dot_prefix", Component.translatable(l.getDescIdPlural())).withStyle(style));
			}
		} else {
			list.accept(Component.translatable("text.apotheosis.dot_prefix", Component.translatable("text.apotheosis.anything")).withStyle(style));
		}
		list.accept(CommonComponents.EMPTY);
		list.accept(Component.translatable("item.modifiers.socket").withStyle(ChatFormatting.GOLD));
		list.accept(this.getSocketBonusTooltip(ItemStack.EMPTY, gem, rarity, facets));
	}

	/**
	 * Adds the one-line socket bonus tooltip.  This will automatically be called in the correct place.<br>
	 * If you want to override the entire tooltip as shown on the gem item, override {@link Gem#addInformation} 
	 * @param socketed The item the gem is socketed into. May be empty if this is being called from {@link Gem#addInformation}
	 * @param gem      The gem stack.
	 * @param purity   The purity of this gem.
	 * @param tooltips The destination for tooltips.
	 */
	public abstract Component getSocketBonusTooltip(ItemStack socketed, ItemStack gem, LootRarity rarity, int facets);

	/**
	 * Returns the max number of facets available for this gem.<br>
	 * Facets are a user-facing wrapper on the purity (level), because most gems do not change on specify purity percentages.
	 */
	public abstract int getMaxFacets(ItemStack gem, LootRarity rarity);

	/**
	 * Calculates the protection value of this affix, with respect to the given damage source.<br>
	 * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}<br>
	 * Ench module overrides with {@link EnchHooks#getDamageAfterMagicAbsorb}<br>
	 * @param purity The purity of this gem. if applicable.<br>
	 * @param source The damage source to compare against.<br>
	 * @return How many protection points this affix is worth against this source.<br>
	 */
	public int getDamageProtection(ItemStack stack, LootRarity rarity, int facets, DamageSource source, ItemStack gem) {
		return 0;
	}

	/**
	 * Calculates the additional damage this affix deals.
	 * This damage is dealt as player physical damage, and is not impacted by critical strikes.
	 */
	public float getDamageBonus(ItemStack stack, LootRarity rarity, int facets, MobType creatureType, ItemStack gem) {
		return 0.0F;
	}

	/**
	 * Called when someone attacks an entity with an item containing this affix.
	 * More specifically, this is invoked whenever the user attacks a target, while having an item with this affix in either hand or any armor slot.
	 * @param user   The wielder of the weapon.  The weapon stack will be in their main hand.
	 * @param target The target entity being attacked.
	 * @param purity The purity of this gem. if applicable.
	 */
	public void doPostAttack(ItemStack stack, LootRarity rarity, int facets, LivingEntity user, @Nullable Entity target, ItemStack gem) {
	}

	/**
	 * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be
	 * called.
	 */
	public void doPostHurt(ItemStack stack, LootRarity rarity, int facets, LivingEntity user, @Nullable Entity attacker, ItemStack gem) {
	}

	/**
	 * Called when a user fires an arrow from a bow or crossbow with this affix on it.
	 */
	public void onArrowFired(ItemStack stack, LootRarity rarity, int facets, LivingEntity user, AbstractArrow arrow, ItemStack gem) {
	}

	/**
	 * Called when {@link Item#onItemUse(ItemUseContext)} would be called for an item with this affix.
	 * Return null to not impact the original result type.
	 */
	@Nullable
	public InteractionResult onItemUse(ItemStack stack, LootRarity rarity, int facets, UseOnContext ctx, ItemStack gem) {
		return null;
	}

	/**
	 * Called when an arrow that was marked with this affix hits a target.
	 */
	public void onArrowImpact(LootRarity rarity, int facets, AbstractArrow arrow, HitResult res, HitResult.Type type, ItemStack gem) {
	}

	/**
	 * Called when a shield with this affix blocks some amount of damage.
	 * @param entity The blocking entity.
	 * @param source The damage source being blocked.
	 * @param amount The amount of damage blocked.
	 * @param purity The purity of this gem.
	 * @return	     The amount of damage that is *actually* blocked by the shield, after this affix applies.
	 */
	public float onShieldBlock(ItemStack stack, LootRarity rarity, int facets, LivingEntity entity, DamageSource source, float amount, ItemStack gem) {
		return amount;
	}

	/**
	 * Called when a player with this affix breaks a block.
	 * @param player The breaking player.
	 * @param world  The level the block was broken in.
	 * @param pos    The position of the block.
	 * @param state  The state that was broken.
	 */
	public void onBlockBreak(ItemStack stack, LootRarity rarity, int facets, Player player, LevelAccessor world, BlockPos pos, BlockState state, ItemStack gem) {

	}

	@Override
	public String toString() {
		return String.format("Gem: %s", this.getId());
	}

	public boolean canApplyTo(ItemStack stack, LootRarity rarity, ItemStack gem) {
		LootCategory cat = LootCategory.forItem(stack);
		return !cat.isNone() && this.types.isEmpty() || this.types.contains(cat);
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

	/**
	 * Bouncer class holding all the base gem data.
	 * Useful for automatic de/serialization.
	 * I would make it a record, but GSON has the big stupid.
	 */
	public static class GemStub {
		protected GemVariant variant;
		protected int weight;
		protected float quality;
		protected Set<ResourceLocation> dimensions = Collections.emptySet();
		protected Set<LootCategory> types = Collections.emptySet();
		@Nullable
		@SerializedName("min_rarity")
		protected LootRarity minRarity;
		@Nullable
		@SerializedName("max_rarity")
		protected LootRarity maxRarity;

		public static void write(FriendlyByteBuf buf, Gem gem) {
			buf.writeEnum(gem.variant);
			buf.writeShort(gem.weight);
			buf.writeFloat(gem.quality);
			// Dimensions do not need to be synced
			buf.writeByte(gem.types.size());
			gem.types.forEach(c -> buf.writeEnum(c));
			// Min/Max rarities also do not need to be synced, they're only used at generation time which is SS-only.
		}

		public static GemStub read(FriendlyByteBuf buf) {
			GemStub stub = new GemStub();
			stub.variant = buf.readEnum(GemVariant.class);
			stub.weight = buf.readShort();
			stub.quality = buf.readFloat();
			stub.dimensions = Collections.emptySet();
			int size = buf.readByte();
			stub.types = new HashSet<>(size);
			for (int i = 0; i < size; i++) {
				stub.types.add(buf.readEnum(LootCategory.class));
			}
			size = buf.readByte();
			return stub;
		}

		public GemStub validate() {
			Preconditions.checkNotNull(this.variant);
			Preconditions.checkArgument(this.weight >= 0);
			Preconditions.checkArgument(this.quality >= 0);
			Preconditions.checkArgument(maxRarity.ordinal() >= minRarity.ordinal());
			return this;
		}
	}

	public static enum GemVariant {
		@SerializedName("parity")
		PARITY("parity", 0),
		@SerializedName("arcane")
		ARCANE("arcane", 1),
		@SerializedName("splendor")
		SPLENDOR("splendor", 2),
		@SerializedName("breach")
		BREACH("breach", 3),
		@SerializedName("guardian")
		GUARDIAN("guardian", 4),
		@SerializedName("chaotic")
		CHAOTIC("chaotic", 5),
		@SerializedName("necrotic")
		NECROTIC("necrotic", 6),
		@SerializedName("mirror")
		MIRROR("mirror", 7),
		@SerializedName("geometric")
		GEOMETRIC("geometric", 8),
		@SerializedName("valence")
		VALENCE("valence", 9),
		@SerializedName("endersurge")
		ENDERSURGE("endersurge", 10);

		public static final Map<String, GemVariant> BY_ID = Arrays.stream(GemVariant.values()).collect(Collectors.toMap(GemVariant::key, Function.identity()));

		private final String key;
		private final int id;

		private GemVariant(String key, int id) {
			this.key = key;
			this.id = id;
		}

		public String key() {
			return this.key;
		}

		public int id() {
			return this.id;
		}
	}

	/**
	 * A Gem Class is the set of types of items it may be applied to.
	 * This comes in the form of a named group of LootCategories.
	 */
	public static class GemClass {

		private final String key;
		private final Set<LootCategory> types;

		public GemClass(String key, Set<LootCategory> types) {
			this.key = key;
			this.types = types;
		}

		public String key() {
			return this.key;
		}

		public Set<LootCategory> types() {
			return this.types;
		}

		public GemClass validate() {
			Preconditions.checkNotNull(this.key, "Invalid GemClass with null key");
			Preconditions.checkArgument(this.types != null && !this.types.isEmpty(), "Invalid GemClass with null or empty types");
			return this;
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(this.key);
			buf.writeByte(this.types.size());
			types.forEach(c -> buf.writeEnum(c));
		}

		public static GemClass read(FriendlyByteBuf buf) {
			String key = buf.readUtf();
			int size = buf.readByte();
			List<LootCategory> list = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				list.add(buf.readEnum(LootCategory.class));
			}
			return new GemClass(key, ImmutableSet.copyOf(list));
		}
	}
}
