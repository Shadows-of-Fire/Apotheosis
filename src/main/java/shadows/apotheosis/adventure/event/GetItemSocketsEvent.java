package shadows.apotheosis.adventure.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.HasResult;

/**
 * Fired when the number of sockets of an item is being calculated. <br>
 * <br>
 * {@link #itemStack} contains the original ItemStack of an item whose socket number is being calculated. <br>
 * {@link #sockets} contains the default amount of sockets on an item. <br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 * 
 * @author Daripher
 */
public class GetItemSocketsEvent extends Event {
	private final ItemStack itemStack;
	private int sockets;

	public GetItemSocketsEvent(ItemStack itemStack, int sockets) {
		this.itemStack = itemStack;
		this.sockets = sockets;
	}

	public ItemStack getItemStack() { return itemStack; }

	public int getSockets() { return sockets; }

	public void setSockets(int sockets) { this.sockets = sockets; }
}
