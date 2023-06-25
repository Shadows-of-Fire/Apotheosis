package shadows.apotheosis.adventure.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when a gem is being inserted into an item. <br>
 * <br>
 * {@link #getGemStack} contains ItemStack copy of a gem that is being inserted into an item. <br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class ItemSocketingEvent extends Event {
	private ItemStack inputStack;
	private ItemStack inputGem;
	private ItemStack outputStack;

	public ItemSocketingEvent(ItemStack inputStack, ItemStack inputGem, ItemStack outputStack) {
		this.inputStack = inputStack;
		this.inputGem = inputGem;
		this.outputStack = outputStack;
	}

	/**
	 * @return Copy of an item that is being socketed with a gem.
	 */
	public ItemStack getInputStack() {
		return inputStack;
	}

	/**
	 * @return Copy of a gem that is being inserted into an item.
	 */
	public ItemStack getInputGem() {
		return inputGem;
	}

	/**
	 * @return The result item after the gem has been inserted into it.
	 */
	public ItemStack getOutputStack() {
		return outputStack;
	}
	
	/**
	 * Sets the output item to a given itemstack.
	 * @param outputStack The stack to change the output to.
	 */
	public void setOutputStack(ItemStack outputStack) {
		this.outputStack = outputStack;
	}
}
