package shadows.apotheosis.adventure.affix.effect;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.util.StepFunction;

public class DamageReductionAffix extends Affix {

	protected final DamageType type;
	protected final Map<LootRarity, StepFunction> values;
	protected final Set<EquipmentSlot> armorTypes;

	public DamageReductionAffix(DamageType type, Map<LootRarity, StepFunction> levelFuncs, Set<EquipmentSlot> armorTypes) {
		super(AffixType.EFFECT);
		this.type = type;
		this.values = levelFuncs;
		this.armorTypes = armorTypes;
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.ARMOR && this.values.containsKey(rarity) && (this.armorTypes.isEmpty() || this.armorTypes.contains(((ArmorItem) stack.getItem()).getSlot()));
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(Component.translatable("affix.apotheosis:damage_reduction.desc", Component.translatable("misc.apotheosis." + this.type.id), fmt(100 * this.getTrueLevel(rarity, level))).withStyle(ChatFormatting.YELLOW));
	}

	public static void onHurt(LivingHurtEvent e) {
		DamageSource src = e.getSource();
		if (src.isBypassInvul() || src.isBypassMagic()) return;
		LivingEntity ent = e.getEntity();
		float amount = e.getAmount();
		for (ItemStack s : ent.getArmorSlots()) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(s);
			for (AffixInstance inst : affixes.values()) {
				if (inst.affix() instanceof DamageReductionAffix dmg && dmg.type.test(src)) {
					amount *= 1 - dmg.getTrueLevel(inst.rarity(), inst.level());
				}
			}
		}
		e.setAmount(amount);
	}

	private float getTrueLevel(LootRarity rarity, float level) {
		return this.values.get(rarity).get(level);
	}

	public static enum DamageType implements Predicate<DamageSource> {
		PHYSICAL("physical", d -> !d.isMagic() && !d.isFire() && !d.isExplosion() && !d.isFall()),
		MAGIC("magic", DamageSource::isMagic),
		FIRE("fire", DamageSource::isFire),
		FALL("fall", DamageSource::isFall),
		EXPLOSION("explosion", DamageSource::isExplosion);

		private final String id;
		private final Predicate<DamageSource> predicate;

		private DamageType(String id, Predicate<DamageSource> predicate) {
			this.id = id;
			this.predicate = predicate;
		}

		public String getId() {
			return this.id;
		}

		@Override
		public boolean test(DamageSource t) {
			return this.predicate.test(t);
		}
	}

	public static DamageReductionAffix read(JsonObject obj) {
		DamageType type = DamageType.valueOf(GsonHelper.getAsString(obj, "damage_type"));
		var values = AffixHelper.readValues(GsonHelper.getAsJsonObject(obj, "values"));
		Set<EquipmentSlot> armorTypes = GSON.fromJson(GsonHelper.getAsJsonArray(obj, "armor_types", new JsonArray()), new TypeToken<Set<EquipmentSlot>>() {
		}.getType());
		return new DamageReductionAffix(type, values, armorTypes);
	}

	public JsonObject write() {
		return new JsonObject();
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.type);
		buf.writeMap(this.values, (b, key) -> b.writeUtf(key.id()), (b, func) -> func.write(b));
		buf.writeByte(this.armorTypes.size());
		this.armorTypes.forEach(c -> buf.writeEnum(c));
	}

	public static DamageReductionAffix read(FriendlyByteBuf buf) {
		DamageType type = buf.readEnum(DamageType.class);
		Map<LootRarity, StepFunction> values = buf.readMap(b -> LootRarity.byId(b.readUtf()), b -> StepFunction.read(b));
		Set<EquipmentSlot> armorTypes = new HashSet<>();
		int size = buf.readByte();
		for (int i = 0; i < size; i++) {
			armorTypes.add(buf.readEnum(EquipmentSlot.class));
		}
		return new DamageReductionAffix(type, values, armorTypes);
	}

}
