package dev.shadowsoffire.apotheosis.compat.curios;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.neoforged.bus.api.IEventBus;

/**
 * Experimental curios compat for enabling a loot category for a curios item.
 */
public class CuriosCompat {

    private static DeferredHelper R = DeferredHelper.create(Apotheosis.MODID);

//    public static final TagKey<Item> CHARM_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", "charm"));
//
//    public static final Holder<EntityEquipmentSlot> CHARM = R.custom("charm", BuiltInRegs.ENTITY_EQUIPMENT_SLOT.key(), () -> new CurioEquipmentSlot("charm"));
//    public static final EntitySlotGroup CHARM_G = R.custom("charm", BuiltInRegs.ENTITY_SLOT_GROUP.key(), new EntitySlotGroup(Apotheosis.loc("charm"),
//        HolderSet.direct(CHARM)));
//
//    public static final LootCategory CHARM_C = R.custom("charm", Apoth.BuiltInRegs.LOOT_CATEGORY.key(), new LootCategory(s -> s.is(CHARM_TAG), CHARM_G));

    public static void register(IEventBus bus) {
        bus.register(R);

    }
}
