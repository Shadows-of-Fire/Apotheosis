package dev.shadowsoffire.apotheosis.adventure.boss;

import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import dev.shadowsoffire.placebo.json.WeightedJsonReloadListener;

public class MinibossManager extends WeightedJsonReloadListener<MinibossItem> {

    public static final MinibossManager INSTANCE = new MinibossManager();

    public MinibossManager() {
        super(AdventureModule.LOGGER, "minibosses", false, false);
    }

    @Override
    protected void validateItem(MinibossItem item) {
        super.validateItem(item);
        item.validate();
    }

    @Override
    protected void registerBuiltinSerializers() {
        this.registerSerializer(DEFAULT, MinibossItem.SERIALIZER);
    }

    /**
     * An item that is limited on a per-entity basis.
     */
    public static interface IEntityMatch {

        /**
         * Null or empty means "all entities". To make an item invalid, return 0 weight.
         *
         * @return A set of all entities that this item can be applied to.
         */
        @Nullable
        Set<EntityType<?>> getEntities();

        public static <T extends IEntityMatch> Predicate<T> matches(EntityType<?> type) {
            return obj -> {
                var types = obj.getEntities();
                return types == null || types.isEmpty() || types.contains(type);
            };
        }

        public static <T extends IEntityMatch> Predicate<T> matches(Entity entity) {
            return matches(entity.getType());
        }
    }

}
