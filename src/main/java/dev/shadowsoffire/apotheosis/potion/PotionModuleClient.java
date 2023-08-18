package dev.shadowsoffire.apotheosis.potion;

import dev.shadowsoffire.apotheosis.Apoth;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PotionModuleClient {

    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Item e) {
        e.register((stack, tint) -> PotionUtils.getColor(stack), Apoth.Items.POTION_CHARM.get());
    }

}
