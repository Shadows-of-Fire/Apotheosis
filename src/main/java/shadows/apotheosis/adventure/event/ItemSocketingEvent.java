package shadows.apotheosis.adventure.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when a gem is being inserted into an item. <br>
 * <br>
 * {@link #getItemStack} contains ItemStack copy of an item that is being socketed with a gem. <br>
 * {@link #getGemStack} contains ItemStack copy of a gem that is being inserted into an item. <br>
 * <br>
 * This event is {@link Cancelable}.<br>
 * Canceling this event will prevent the item from being socketed with the gem. <br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 * 
 * @author Daripher
 */
@Cancelable
public class ItemSocketingEvent extends Event {
	private ItemStack itemStack;
	private ItemStack gemStack;

	public ItemSocketingEvent(ItemStack itemStack, ItemStack gemStack) {
		this.itemStack = itemStack;
		this.gemStack = gemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemStack getGemStack() {
		return gemStack;
	}

	public void setGemStack(ItemStack gemStack) {
		this.gemStack = gemStack;
	}
}
