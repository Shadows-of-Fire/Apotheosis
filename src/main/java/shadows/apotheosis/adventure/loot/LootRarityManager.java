package shadows.apotheosis.adventure.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixManager;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootRarity.LootRule;
import shadows.apotheosis.adventure.loot.LootRarity.RarityStub;
import shadows.placebo.json.DynamicRegistryObject;
import shadows.placebo.json.PlaceboJsonReloadListener;

/**
 * Handles loading the configurable portion of rarities.
 */
public class LootRarityManager extends PlaceboJsonReloadListener<RarityStub> {

	public static final LootRarityManager INSTANCE = new LootRarityManager();

	public static final DynamicRegistryObject<RarityStub> COMMON = INSTANCE.makeObj(Apotheosis.loc("common"));
	public static final DynamicRegistryObject<RarityStub> UNCOMMON = INSTANCE.makeObj(Apotheosis.loc("uncommon"));
	public static final DynamicRegistryObject<RarityStub> RARE = INSTANCE.makeObj(Apotheosis.loc("rare"));
	public static final DynamicRegistryObject<RarityStub> EPIC = INSTANCE.makeObj(Apotheosis.loc("epic"));
	public static final DynamicRegistryObject<RarityStub> MYTHIC = INSTANCE.makeObj(Apotheosis.loc("mythic"));
	public static final DynamicRegistryObject<RarityStub> ANCIENT = INSTANCE.makeObj(Apotheosis.loc("ancient"));

	protected Map<String, LootRarity> byId = new HashMap<>();
	protected List<LootRarity> list = new ArrayList<>(6);

	private LootRarityManager() {
		super(AdventureModule.LOGGER, "rarities", false, false);
	}

	@Override
	protected void onReload() {
		super.onReload();
		Preconditions.checkArgument(COMMON.get() != null, "Common rarity not registered!");
		Preconditions.checkArgument(UNCOMMON.get() != null, "Uncommon rarity not registered!");
		Preconditions.checkArgument(RARE.get() != null, "Rare rarity not registered!");
		Preconditions.checkArgument(EPIC.get() != null, "Epic rarity not registered!");
		Preconditions.checkArgument(MYTHIC.get() != null, "Mythic rarity not registered!");
		Preconditions.checkArgument(ANCIENT.get() != null, "Ancient rarity not registered!");
		Preconditions.checkArgument(this.registry.size() == 6, "Registration of additional rarity levels is not supported!");
		Preconditions.checkArgument(this.registry.values().stream().mapToInt(RarityStub::getWeight).sum() > 0, "The total weight of all rarities must be above 0");

		LootRarity.COMMON.update(COMMON.get());
		LootRarity.UNCOMMON.update(UNCOMMON.get());
		LootRarity.RARE.update(RARE.get());
		LootRarity.EPIC.update(EPIC.get());
		LootRarity.MYTHIC.update(MYTHIC.get());
		// LootRarity.ANCIENT.update(ANCIENT.get()); Ancient is NYI, so changes should not be reflected.

		for (LootRarity rarity : LootRarity.values()) {
			if (rarity == LootRarity.ANCIENT) continue;
			Map<AffixType, List<LootRule>> sorted = new HashMap<>();
			rarity.rules().stream().filter(r -> r.type().needsValidation()).forEach(rule -> {
				sorted.computeIfAbsent(rule.type(), r -> new ArrayList<>());
				sorted.get(rule.type()).add(rule);
			});
			sorted.forEach((type, rules) -> {
				for (LootCategory cat : LootCategory.VALUES) {
					if (cat.isNone()) continue;
					List<Affix> affixes = AffixManager.INSTANCE.getValues().stream().filter(a -> a.canApplyTo(ItemStack.EMPTY, cat, rarity) && a.getType() == type).toList();

					if (affixes.size() < rules.size()) {
						var errMsg = new StringBuilder();
						errMsg.append("Insufficient number of affixes to satisfy the loot rules (ignoring backup rules) of rarity " + rarity.id() + " for category " + cat.getName());
						errMsg.append("Required: " + rules.size());
						errMsg.append("; Provided: " + affixes.size());
						//errMsg.append("The following affixes exist for this category/rarity combination: ");
						//affixes.forEach(a -> errMsg.append(a.getId() + " "));
						AdventureModule.LOGGER.error(errMsg.toString());
					}
				}
			});
		}
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, RarityStub.SERIALIZER);
	}

	@Override
	protected void validateItem(RarityStub item) {
		super.validateItem(item);
		Preconditions.checkArgument(item.getWeight() >= 0, "A rarity may not have negative weight!");
		Preconditions.checkArgument(item.getQuality() >= 0, "A rarity may not have negative quality!");
		Preconditions.checkArgument(!item.rules().isEmpty(), "A rarity may not have no rules!");
	}

}