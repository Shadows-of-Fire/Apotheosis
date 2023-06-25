package shadows.apotheosis.adventure.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.HasResult;

/**
 * Fired when the number of sockets of an item is being calculated. <br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class GetItemSocketsEvent extends Event {
	private final ItemStack itemStack;
	private int sockets;

	public GetItemSocketsEvent(ItemStack itemStack, int sockets) {
		this.itemStack = itemStack;
		this.sockets = sockets;
	}

	/**
	 * @return The original item whose socket number is being calculated.
	 */
	public ItemStack getItemStack() {
		return itemStack;
	}

	/**
	 * @return The number of sockets the item will have after the event has been fired.
	 */
	public int getSockets() {
		return sockets;
	}

	/**
	 * Sets the number of sockets the item will have to a given amount.
	 * @param sockets The number of sockets the item will have.
	 */
	public void setSockets(int sockets) {
		this.sockets = sockets;
	}
}
