package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.PSerializer;

@Deprecated(forRemoval = true, since = "6.1.0")
public final class LegacyGem extends Gem {

	public static final LegacyGem INSTANCE = new LegacyGem(new GemStub());

	public LegacyGem(GemStub stub) {
		super(stub);
	}

	public static final String MODIFIER = "modifier";

	@Override
	public Component getSocketBonusTooltip(ItemStack socketed, ItemStack gem, LootRarity rarity, int facets) {
		var bonus = getStoredBonus(gem);
		return GemItem.toComponent(bonus.getKey(), bonus.getValue());
	}

	@Override
	public void addModifiers(ItemStack stack, LootRarity rarity, int facets, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map, ItemStack gem) {
		LootCategory cat = LootCategory.forItem(stack);
		for (EquipmentSlot s : cat.getSlots(stack)) {
			if (s == type) {
				var bonus = getStoredBonus(gem);
				if (bonus != null) map.accept(bonus.getLeft(), bonus.getRight());
			}
		}
	}

	@Nullable
	public static Pair<Attribute, AttributeModifier> getStoredBonus(ItemStack gem) {
		CompoundTag tag = gem.getTagElement(MODIFIER);
		if (tag == null) return null;
		Attribute attrib = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(tag.getString("attribute")));
		if (attrib == null) return null;
		AttributeModifier modif = AttributeModifier.load(tag);
		if (modif == null) return null;
		return Pair.of(attrib, modif);
	}

	public static void setStoredBonus(ItemStack stack, Attribute attrib, AttributeModifier modif) {
		CompoundTag tag = modif.save();
		tag.putString("attribute", ForgeRegistries.ATTRIBUTES.getKey(attrib).toString());
		stack.getOrCreateTag().put(MODIFIER, tag);
	}

	@Override
	public void setId(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public void setSerializer(PSerializer<Gem> serializer) {
		this.serializer = serializer;
	}

	@Override
	public int getMaxFacets(ItemStack gem, LootRarity rarity) {
		return 0;
	}

}
