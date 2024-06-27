package dev.shadowsoffire.apotheosis.adventure.event;

import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired from {@link SocketHelper#getSockets(ItemStack)} to allow modification of the number of sockets an item has.
 * <p>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class GetItemSocketsEvent extends Event {
    protected final ItemStack stack;
    protected int sockets;

    public GetItemSocketsEvent(ItemStack stack, int sockets) {
        this.stack = stack;
        this.sockets = sockets;
    }

    /**
     * @return The item whose socket value is being calculated.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * @return The (possibly event-modified) number of sockets this item has.
     */
    public int getSockets() {
        return this.sockets;
    }

    /**
     * Sets the number of sockets the item will have to a given amount.
     *
     * @param sockets The new socket count.
     */
    public void setSockets(int sockets) {
        this.sockets = sockets;
    }
}
