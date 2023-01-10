package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.JsonUtil;
import shadows.placebo.util.StepFunction;

public final class StatGem extends Gem {

	protected final Attribute attribute;
	protected final Operation operation;
	protected final Map<LootRarity, StepFunction> values;

	public StatGem(GemStub stub, Attribute attrib, Operation op, Map<LootRarity, StepFunction> values) {
		super(stub);
		this.attribute = attrib;
		this.operation = op;
		this.values = values;
	}

	@Override
	public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity, int facets) {
		return GemItem.toComponent(attribute, read(gem, rarity, facets));
	}

	@Override
	public int getMaxFacets(ItemStack gem, LootRarity rarity) {
		return this.values.get(rarity).steps();
	}

	@Override
	public void addModifiers(ItemStack stack, LootRarity rarity, int facets, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map, ItemStack gem) {
		LootCategory cat = LootCategory.forItem(stack);
		for (EquipmentSlot s : cat.getSlots(stack)) {
			if (s == type) {
				map.accept(this.attribute, read(gem, rarity, facets));
			}
		}
	}

	protected AttributeModifier read(ItemStack gem, LootRarity rarity, int facets) {
		return new AttributeModifier(GemItem.getUUIDs(gem).get(0), "apoth.gem_modifier", this.values.get(rarity).getForStep(facets), this.operation);
	}

	public static StatGem read(JsonObject obj) {
		GemStub stub = GemManager.GSON.fromJson(obj, GemStub.class);
		Attribute attr = JsonUtil.getRegistryObject(obj, "attribute", ForgeRegistries.ATTRIBUTES);
		Operation op = Operation.valueOf(GsonHelper.getAsString(obj, "operation"));
		var values = AffixHelper.readValues(GsonHelper.getAsJsonObject(obj, "values"));
		return new StatGem(stub, attr, op, values);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		GemStub.write(buf, this);
		buf.writeRegistryId(ForgeRegistries.ATTRIBUTES, this.attribute);
		buf.writeEnum(this.operation);
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, func) -> func.write(b));
	}

	public static StatGem read(FriendlyByteBuf buf) {
		GemStub stub = GemStub.read(buf);
		Attribute attr = buf.readRegistryIdSafe(Attribute.class);
		Operation op = buf.readEnum(Operation.class);
		Map<LootRarity, StepFunction> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> StepFunction.read(b));
		return new StatGem(stub, attr, op, values);
	}

}
