package shadows.apotheosis.miscs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MiscsModuleForgeEvents {

    @SubscribeEvent
    public void attachEntitiesCapabilities(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof Player) {
            evt.addCapability(MiscCapability.ID_ITEM_PICKUP_IGNORE_LIST,
                    ItemPickupIgnoreListCapability.createProvider((Player) evt.getObject()));
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event){
        ItemPickupIgnoreListCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void checkIgnoreListForPickup(EntityItemPickupEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        player.getCapability(MiscCapability.ITEM_PICKUP_IGNORE_LIST_CAPABILITY).ifPresent(cap ->
        {
            if(cap.getIgnoreList().contains(event.getItem().getItem().getItem().getRegistryName())){
                event.setCanceled(true);
            }
        });
    }
}
