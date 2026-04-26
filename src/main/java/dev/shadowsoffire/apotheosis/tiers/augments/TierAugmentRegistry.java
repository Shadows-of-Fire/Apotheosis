package dev.shadowsoffire.apotheosis.tiers.augments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;
import dev.shadowsoffire.placebo.dynreg.SubtypedSerializer;

public final class TierAugmentRegistry extends DynamicRegistry<TierAugment> {

    /**
     * Public serializer so external mods can register additional TierAugment subtypes during their setup phase.
     */
    public static final SubtypedSerializer<TierAugment> SERIALIZER = RegistrySerializer.<TierAugment>subtypedSynced("tier_augments")
        .register(Apotheosis.loc("attribute"), AttributeAugment.CODEC);

    public static TierAugmentRegistry INSTANCE = new TierAugmentRegistry();

    protected Map<Key, List<TierAugment>> augmentsPerTier = new HashMap<>();

    private TierAugmentRegistry() {
        super(Apotheosis.LOGGER, Apotheosis.loc("tier_augments"), SERIALIZER);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.augmentsPerTier.clear();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        for (TierAugment aug : this.registry.values()) {
            this.augmentsPerTier.computeIfAbsent(new Key(aug.tier(), aug.target()), t -> new ArrayList<>()).add(aug);
        }
        for (List<TierAugment> augList : this.augmentsPerTier.values()) {
            augList.sort(Comparator.comparing(TierAugment::sortIndex));
        }
    }

    /**
     * Returns a list of all augments for the target tier.
     * <p>
     * This list may be empty.
     */
    public static List<TierAugment> getAugments(WorldTier tier, Target target) {
        Key key = new Key(tier, target);
        return Collections.unmodifiableList(INSTANCE.augmentsPerTier.getOrDefault(key, List.of()));
    }

    private record Key(WorldTier tier, Target target) {}

}
