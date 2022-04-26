package shadows.apotheosis.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.SerializerBuilder;

/**
 * Util class to contain the full equipment for an entity.
 * @author Shadows
 *
 */
public class GearSet extends Weighted implements PlaceboJsonReloadListener.TypeKeyed<GearSet> {

	@Expose(deserialize = false)
	protected ResourceLocation id;
	protected final List<WeightedItemStack> mainhands;
	protected final List<WeightedItemStack> offhands;
	protected final List<WeightedItemStack> boots;
	protected final List<WeightedItemStack> leggings;
	protected final List<WeightedItemStack> chestplates;
	protected final List<WeightedItemStack> helmets;
	protected final List<String> tags = new ArrayList<>();
	private SerializerBuilder<GearSet>.Serializer serializer;

	public GearSet(int weight, List<WeightedItemStack> mainhands, List<WeightedItemStack> offhands, List<WeightedItemStack> boots, List<WeightedItemStack> leggings, List<WeightedItemStack> chestplates, List<WeightedItemStack> helmets) {
		super(weight);
		this.mainhands = mainhands;
		this.offhands = offhands;
		this.boots = boots;
		this.leggings = leggings;
		this.chestplates = chestplates;
		this.helmets = helmets;
	}

	@Override
	public void setId(ResourceLocation id) {
		if (this.id == null) {
			this.id = id;
		} else throw new IllegalStateException("Cannot set the id of this boss item, it is already set!");
	}

	@Override
	public void setSerializer(SerializerBuilder<GearSet>.Serializer serializer) {
		if (this.serializer != null) throw new UnsupportedOperationException();
		this.serializer = serializer;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public SerializerBuilder<GearSet>.Serializer getSerializer() {
		return this.serializer;
	}

	/**
	 * Makes the entity wear this armor set.  Returns the entity for convenience.
	 */
	public LivingEntity apply(LivingEntity entity) {
		entity.setItemSlot(EquipmentSlot.MAINHAND, getRandomStack(this.mainhands, entity.random));
		entity.setItemSlot(EquipmentSlot.OFFHAND, getRandomStack(this.offhands, entity.random));
		entity.setItemSlot(EquipmentSlot.FEET, getRandomStack(this.boots, entity.random));
		entity.setItemSlot(EquipmentSlot.LEGS, getRandomStack(this.leggings, entity.random));
		entity.setItemSlot(EquipmentSlot.CHEST, getRandomStack(this.chestplates, entity.random));
		entity.setItemSlot(EquipmentSlot.HEAD, getRandomStack(this.helmets, entity.random));
		return entity;
	}

	/**
	 * Returns a copy of a random itemstack in this list of stacks.
	 */
	public static ItemStack getRandomStack(List<WeightedItemStack> stacks, Random random) {
		if (stacks.isEmpty()) return ItemStack.EMPTY;
		return WeightedRandom.getRandomItem(random, stacks).get().getStack().copy();
	}

	public static class WeightedItemStack extends Weighted {

		final ItemStack stack;

		public WeightedItemStack(ItemStack stack, int weight) {
			super(weight);
			this.stack = stack;
		}

		public ItemStack getStack() {
			return this.stack;
		}

		@Override
		public String toString() {
			return "Stack: " + this.stack.toString() + " @ Weight: " + this.weight;
		}
	}

	public static class SetPredicate implements Predicate<GearSet> {

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

	}

	public static class SetPredicateAdapter implements JsonDeserializer<SetPredicate>, JsonSerializer<SetPredicate> {

		@Override
		public JsonElement serialize(SetPredicate src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.key);
		}

		@Override
		public SetPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return new SetPredicate(json.getAsString());
		}

	}
}
