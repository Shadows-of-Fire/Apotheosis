package dev.shadowsoffire.apotheosis.adventure.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.util.GearSet;
import dev.shadowsoffire.apotheosis.util.GearSet.SetPredicate;
import dev.shadowsoffire.placebo.reload.WeightedJsonReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;

public class BossArmorManager extends WeightedJsonReloadListener<GearSet> {

    public static final BossArmorManager INSTANCE = new BossArmorManager();

    public BossArmorManager() {
        super(AdventureModule.LOGGER, "boss_gear", false, false);
    }

    /**
     * Returns a random weighted armor set based on the given random (and predicate, if applicable).
     */
    public <T extends Predicate<GearSet>> GearSet getRandomSet(RandomSource rand, float luck, @Nullable List<SetPredicate> armorSets) {
        if (armorSets == null || armorSets.isEmpty()) return this.getRandomItem(rand, luck);
        List<GearSet> valid = this.registry.values().stream().filter(e -> {
            for (Predicate<GearSet> f : armorSets)
                if (f.test(e)) return true;
            return false;
        }).collect(Collectors.toList());
        if (valid.isEmpty()) {
            AdventureModule.LOGGER.error("Failed to locate any gear sets matching the following predicates: ");
            armorSets.forEach(s -> AdventureModule.LOGGER.error(s.toString()));
            return this.getRandomItem(rand, luck);
        }

        List<Wrapper<GearSet>> list = new ArrayList<>(valid.size());
        valid.stream().map(l -> l.<GearSet>wrap(luck)).forEach(list::add);
        return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).orElse(null);
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT, GearSet.SERIALIZER);
    }

}
