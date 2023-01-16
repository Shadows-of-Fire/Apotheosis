package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

public final class MultiStatGem extends Gem {

	protected final List<MultiStatGem.Data> data;
	protected transient final Map<LootCategory, MultiStatGem.Data> dataExploded;

	public MultiStatGem(GemStub stub, List<MultiStatGem.Data> data) {
		super(stub);
		this.data = data;
		Collections.sort(data, (d1, d2) -> d1.gemClass.key().compareTo(d2.gemClass.key()));
		this.dataExploded = data.stream().<Pair<LootCategory, Data>>mapMulti((gemData, mapper) -> {
			for (LootCategory c : gemData.gemClass.types()) {
				mapper.accept(Pair.of(c, gemData));
			}
		}).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
	}

	@Override
	public void addInformation(ItemStack gem, LootRarity rarity, int facets, Consumer<Component> list) {
		list.accept(Component.translatable("text.apotheosis.facets", 4 + facets).withStyle(Style.EMPTY.withColor(0xAEA2D6)));
		list.accept(CommonComponents.EMPTY);
		Style style = Style.EMPTY.withColor(0x0AFF0A);
		list.accept(Component.translatable("text.apotheosis.socketable_into").withStyle(style));
		addTypeInfo(list, this.dataExploded.keySet().toArray());
		list.accept(CommonComponents.EMPTY);
		list.accept(Component.translatable("item.modifiers.socket_in").withStyle(ChatFormatting.GOLD));
		for (MultiStatGem.Data d : data) {
			Component modifComp = GemItem.toComponent(d.attribute, read(gem, rarity, facets, d));
			Component sum = Component.translatable("text.apotheosis.dot_prefix", Component.translatable("%s: %s", Component.translatable("gem_class." + d.gemClass.key()), modifComp)).withStyle(ChatFormatting.GOLD);
			list.accept(sum);
		}
	}

	public boolean canApplyTo(ItemStack stack, LootRarity rarity, ItemStack gem) {
		LootCategory cat = LootCategory.forItem(stack);
		return this.types.isEmpty() || this.types.contains(cat);
	}

	@Override
	public Component getSocketBonusTooltip(ItemStack socketed, ItemStack gem, LootRarity rarity, int facets) {
		Data d = dataExploded.get(LootCategory.forItem(socketed));
		if (d == null) return Component.literal("Null Error");
		return GemItem.toComponent(d.attribute, read(gem, rarity, facets, d));
	}

	@Override
	public int getMaxFacets(ItemStack gem, LootRarity rarity) {
		return data.get(0).values.get(rarity).steps();
	}

	@Override
	public void addModifiers(ItemStack stack, LootRarity rarity, int facets, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map, ItemStack gem) {
		LootCategory cat = LootCategory.forItem(stack);
		Data d = dataExploded.get(cat);
		for (EquipmentSlot s : cat.getSlots(stack)) {
			if (s == type) {
				map.accept(d.attribute, read(gem, rarity, facets, d));
			}
		}
	}

	public static AttributeModifier read(ItemStack gem, LootRarity rarity, int facets, Data d) {
		return new AttributeModifier(GemItem.getUUIDs(gem).get(0), "apoth.gem_modifier", d.values.get(rarity).getForStep(facets), d.operation);
	}

	public static MultiStatGem read(JsonObject obj) {
		GemStub stub = GemManager.GSON.fromJson(obj, GemStub.class);
		List<Data> data = GemManager.GSON.fromJson(obj.get("values"), new TypeToken<List<Data>>() {
		}.getType());

		Data prev = null, cur = null;
		for (int i = 0; i < data.size(); i++) {
			cur = data.get(i).validate();
			if (prev != null) {
				for (LootRarity r : prev.values.keySet()) {
					Preconditions.checkArgument(prev.values.get(r).steps() == cur.values.get(r).steps(), "MultiStatGem: Mismatched max number of steps at rarity " + r.id());
				}
			}
			prev = data.get(i);
		}

		return new MultiStatGem(stub, data);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		GemStub.write(buf, this);
		buf.writeByte(this.data.size());
		this.data.forEach(d -> d.write(buf));
	}

	public static MultiStatGem read(FriendlyByteBuf buf) {
		GemStub stub = GemStub.read(buf);
		int size = buf.readByte();
		List<Data> data = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			data.add(Data.read(buf));
		}
		return new MultiStatGem(stub, data);
	}

	protected static class Data {
		@SerializedName("gem_class")
		protected final GemClass gemClass;
		protected final Attribute attribute;
		protected final Operation operation;
		protected final Map<LootRarity, StepFunction> values;

		protected Data(GemClass gemClass, Attribute attr, Operation op, Map<LootRarity, StepFunction> values) {
			this.gemClass = gemClass;
			this.attribute = attr;
			this.operation = op;
			this.values = values;
		}

		protected Data validate() {
			gemClass.validate();
			Preconditions.checkNotNull(this.attribute, "Invalid MultiStatGem.Data with null attribute");
			Preconditions.checkNotNull(this.operation, "Invalid MultiStatGem.Data with null operation");
			Preconditions.checkNotNull(this.values, "Invalid MultiStatGem.Data with null values");
			return this;
		}

		protected void write(FriendlyByteBuf buf) {
			this.gemClass.write(buf);
			buf.writeRegistryId(ForgeRegistries.ATTRIBUTES, this.attribute);
			buf.writeEnum(this.operation);
			buf.writeMap(this.values, (b, rarity) -> b.writeUtf(rarity.id()), (b, func) -> func.write(b));
		}

		protected static Data read(FriendlyByteBuf buf) {
			GemClass gemClass = GemClass.read(buf);
			Attribute attr = buf.readRegistryIdSafe(Attribute.class);
			Operation op = buf.readEnum(Operation.class);
			Map<LootRarity, StepFunction> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> StepFunction.read(b));
			return new Data(gemClass, attr, op, values);
		}
	}

}
