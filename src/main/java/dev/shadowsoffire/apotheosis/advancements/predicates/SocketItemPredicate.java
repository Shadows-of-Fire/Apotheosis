package dev.shadowsoffire.apotheosis.advancements.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.item.component.ItemContainerContents;

public class SocketItemPredicate implements SingleComponentItemPredicate<ItemContainerContents>, TypeAwareDCP<SocketItemPredicate> {

    public static final SocketItemPredicate INSTANCE = new SocketItemPredicate();
    public static final Codec<SocketItemPredicate> CODEC = MapCodec.unit(INSTANCE).codec();

    @Override
    public DataComponentType<ItemContainerContents> componentType() {
        return Components.SOCKETED_GEMS;
    }

    @Override
    public boolean matches(ItemContainerContents value) {
        return value.nonEmptyItemCopyStream().map(GemInstance::unsocketed).anyMatch(GemInstance::isValid);
    }

    @Override
    public DataComponentPredicate.Type<SocketItemPredicate> type() {
        return Apoth.DataComponentPredicates.SOCKETED_ITEM;
    }
}
