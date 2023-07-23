package shadows.apotheosis.core.attributeslib.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.core.attributeslib.api.AttributeHelper;
import shadows.apotheosis.core.attributeslib.client.ModifierSource.EffectModifierSource;
import shadows.apotheosis.core.attributeslib.client.ModifierSource.ItemModifierSource;
import shadows.apotheosis.util.Comparators;

/**
 * A Modifier Source Type is a the registration component of a ModifierSource.
 *
 * @param <T>
 */
public abstract class ModifierSourceType<T> {

    private static final List<ModifierSourceType<?>> SOURCE_TYPES = new ArrayList<>();

    public static final ModifierSourceType<ItemStack> EQUIPMENT = register(new ModifierSourceType<>(){

        @Override
        public void extract(LivingEntity entity, BiConsumer<AttributeModifier, ModifierSource<?>> map) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = entity.getItemBySlot(slot);
                item.getAttributeModifiers(slot).values().forEach(modif -> {
                    map.accept(modif, new ItemModifierSource(item));
                });
            }
        }

        @Override
        public int getPriority() {
            return 0;
        }

    });

    public static final ModifierSourceType<MobEffectInstance> MOB_EFFECT = register(new ModifierSourceType<>(){

        @Override
        public void extract(LivingEntity entity, BiConsumer<AttributeModifier, ModifierSource<?>> map) {
            for (MobEffectInstance effectInst : entity.getActiveEffects()) {
                effectInst.getEffect().getAttributeModifiers().values().forEach(modif -> {
                    map.accept(modif, new EffectModifierSource(effectInst));
                });
            }
        }

        @Override
        public int getPriority() {
            return 100;
        }

    });

    public static Collection<ModifierSourceType<?>> getTypes() {
        return Collections.unmodifiableCollection(SOURCE_TYPES);
    }

    public static <T extends ModifierSourceType<?>> T register(T type) {
        SOURCE_TYPES.add(type);
        return type;
    }

    public static Comparator<AttributeModifier> compareBySource(Map<UUID, ModifierSource<?>> sources) {

        Comparator<AttributeModifier> comp = Comparators.chained(
            Comparator.comparingInt(a -> sources.get(a.getId()).getType().getPriority()),
            Comparator.comparing(a -> sources.get(a.getId())),
            AttributeHelper.modifierComparator());

        return (a1, a2) -> {
            var src1 = sources.get(a1.getId());
            var src2 = sources.get(a2.getId());

            if (src1 != null && src2 != null) return comp.compare(a1, a2);

            return src1 != null ? -1 : src2 != null ? 1 : 0;
        };
    }

    /**
     * Extracts all ModifierSource(s) of this type from the source entity.
     *
     * @param entity
     * @param map
     */
    public abstract void extract(LivingEntity entity, BiConsumer<AttributeModifier, ModifierSource<?>> map);

    /**
     * Integer priority for display sorting.<br>
     * Lower priority values will be displayed at the top of the list.
     */
    public abstract int getPriority();

}
