package shadows.apotheosis.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import shadows.placebo.json.ItemAdapter;
import shadows.placebo.json.PSerializer;
import shadows.placebo.json.TypeKeyed.TypeKeyedBase;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

/**
 * Util class to contain the full equipment for an entity.
 * @author Shadows
 *
 */
public class GearSet extends TypeKeyedBase<GearSet> implements ILuckyWeighted {

	//Formatter::off
	public static final Codec<GearSet> CODEC = RecordCodecBuilder.create(inst -> 
		inst.group(
			Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
			Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("quality", 0F).forGetter(ILuckyWeighted::getQuality),
			WeightedItemStack.LIST_CODEC.fieldOf("mainhands").forGetter(g -> g.mainhands),
			WeightedItemStack.LIST_CODEC.fieldOf("offhands").forGetter(g -> g.offhands),
			WeightedItemStack.LIST_CODEC.fieldOf("boots").forGetter(g -> g.boots),
			WeightedItemStack.LIST_CODEC.fieldOf("leggings").forGetter(g -> g.leggings),
			WeightedItemStack.LIST_CODEC.fieldOf("chestplates").forGetter(g -> g.chestplates),
			WeightedItemStack.LIST_CODEC.fieldOf("helmets").forGetter(g -> g.helmets),
			Codec.STRING.listOf().fieldOf("tags").forGetter(g -> g.tags))
		.apply(inst, GearSet::new)
	);
	//Formatter::on
	public static final PSerializer<GearSet> SERIALIZER = PSerializer.fromCodec("Gear Set", CODEC);

	protected final int weight;
	protected final float quality;
	protected final List<WeightedItemStack> mainhands;
	protected final List<WeightedItemStack> offhands;
	protected final List<WeightedItemStack> boots;
	protected final List<WeightedItemStack> leggings;
	protected final List<WeightedItemStack> chestplates;
	protected final List<WeightedItemStack> helmets;
	protected final List<String> tags;

	protected transient Map<EquipmentSlot, List<WeightedItemStack>> slotToStacks;

	public GearSet(int weight, float quality, List<WeightedItemStack> mainhands, List<WeightedItemStack> offhands, List<WeightedItemStack> boots, List<WeightedItemStack> leggings, List<WeightedItemStack> chestplates, List<WeightedItemStack> helmets, List<String> tags) {
		this.weight = weight;
		this.quality = quality;
		this.mainhands = mainhands;
		this.offhands = offhands;
		this.boots = boots;
		this.leggings = leggings;
		this.chestplates = chestplates;
		this.helmets = helmets;
		this.tags = tags;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

	/**
	 * Makes the entity wear this armor set.  Returns the entity for convenience.
	 */
	public LivingEntity apply(LivingEntity entity) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			getRandomStack(getPotentials(slot), entity.random).ifPresent(s -> s.apply(entity, slot));
		}
		return entity;
	}

	public List<WeightedItemStack> getPotentials(EquipmentSlot slot) {
		switch (slot) {
		case MAINHAND:
			return this.mainhands;
		case OFFHAND:
			return this.offhands;
		case FEET:
			return this.boots;
		case LEGS:
			return this.leggings;
		case CHEST:
			return this.chestplates;
		case HEAD:
			return this.helmets;
		}
		throw new RuntimeException("invalid slot");
	}

	@Override
	public PSerializer<? extends GearSet> getSerializer() {
		return SERIALIZER;
	}

	/**
	 * Returns a copy of a random itemstack in this list of stacks.
	 */
	public static Optional<WeightedItemStack> getRandomStack(List<WeightedItemStack> stacks, RandomSource random) {
		if (stacks.isEmpty()) return Optional.empty();
		return Optional.of(WeightedRandom.getRandomItem(random, stacks).get());
	}

	public static class WeightedItemStack extends Weighted {

		//Formatter::off
		public static final Codec<WeightedItemStack> CODEC = RecordCodecBuilder.create(inst -> 
			inst.group(
				ItemAdapter.CODEC.fieldOf("stack").forGetter(w -> w.stack),
				Codec.INT.fieldOf("weight").forGetter(w -> w.weight),
				Codec.FLOAT.optionalFieldOf("drop_chance", -1F).forGetter(w -> w.dropChance))
			.apply(inst, WeightedItemStack::new)
		);
		//Formatter::on
		public static final Codec<List<WeightedItemStack>> LIST_CODEC = CODEC.listOf();

		final ItemStack stack;
		final float dropChance;

		public WeightedItemStack(ItemStack stack, int weight, float dropChance) {
			super(weight);
			this.stack = stack;
			this.dropChance = dropChance;
		}

		public ItemStack getStack() {
			return this.stack;
		}

		@Override
		public String toString() {
			return "Stack: " + this.stack.toString() + " @ Weight: " + this.weight;
		}

		public void apply(LivingEntity entity, EquipmentSlot slot) {
			entity.setItemSlot(slot, this.stack.copy());
			if (this.dropChance >= 0 && entity instanceof Mob mob) {
				mob.setDropChance(slot, this.dropChance);
			}
		}
	}

	public static class SetPredicate implements Predicate<GearSet> {

		public static final Codec<SetPredicate> CODEC = ExtraCodecs.stringResolverCodec(s -> s.key, SetPredicate::new);

		protected final String key;
		protected final Predicate<GearSet> internal;

		public SetPredicate(String key) {
			this.key = key;
			if (key.startsWith("#")) {
				String tag = key.substring(1);
				this.internal = t -> t.tags.contains(tag);
			} else {
				ResourceLocation id = new ResourceLocation(key);
				this.internal = t -> t.id.equals(id);
			}
		}

		@Override
		public boolean test(GearSet t) {
			return this.internal.test(t);
		}

		@Override
		public String toString() {
			return "SetPredicate[" + this.key + "]";
		}

	}
}
