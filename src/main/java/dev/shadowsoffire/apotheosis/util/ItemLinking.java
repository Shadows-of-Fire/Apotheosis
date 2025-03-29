package dev.shadowsoffire.apotheosis.util;

import java.util.UUID;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.net.LinkItemToChatPayload;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

public class ItemLinking {

    /**
     * Cooldown tracker map from player id -> game time of last link.
     */
    private static final Object2LongMap<UUID> LAST_LINK_TIMES = new Object2LongOpenHashMap<>();

    public static void sendHoveredItem() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Client.sendHoveredItem();
        }
    }

    public static boolean isOnCooldown(UUID id, long gameTime) {
        return LAST_LINK_TIMES.getOrDefault(id, -AdventureConfig.itemLinkingCooldown) + AdventureConfig.itemLinkingCooldown >= gameTime;
    }

    public static void startCooldown(UUID id, long gameTime) {
        LAST_LINK_TIMES.put(id, gameTime);
    }

    public static class Client {
        public static void sendHoveredItem() {
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen scn) {
                Slot slot = scn.getSlotUnderMouse();
                if (slot != null && slot.hasItem()) {
                    PacketDistributor.sendToServer(new LinkItemToChatPayload(scn.getMenu().containerId, slot.index, slot.getItem().getItem()));
                }
            }
        }
    }

}
