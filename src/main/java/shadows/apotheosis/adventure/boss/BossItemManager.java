package shadows.apotheosis.adventure.boss;

import shadows.apotheosis.adventure.AdventureModule;
import shadows.placebo.json.WeightedJsonReloadListener;

public class BossItemManager extends WeightedJsonReloadListener<BossItem> {

	public static final BossItemManager INSTANCE = new BossItemManager();

	public BossItemManager() {
		super(AdventureModule.LOGGER, "bosses", false, false);
	}

	@Override
	protected void validateItem(BossItem item) {
		super.validateItem(item);
		item.validate();
	}

	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(DEFAULT, BossItem.SERIALIZER);
	}

}