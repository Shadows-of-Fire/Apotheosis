package dev.shadowsoffire.apotheosis.adventure.event;

import dev.shadowsoffire.apotheosis.adventure.socket.SocketingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * Parent class of events that are fired when a gem is socketed into an item.
 */
public abstract class ItemSocketingEvent extends Event {
    protected final ItemStack stack;
    protected final ItemStack gem;

    public ItemSocketingEvent(ItemStack stack, ItemStack gem) {
        this.stack = stack.copy();
        this.gem = gem.copy();
    }

    /**
     * Gets the item being socketed into.
     *
     * @return A copy of the left input item.
     */
    public ItemStack getInputStack() {
        return this.stack;
    }

    /**
     * Gets the gem that is being socketed into {@link #getInputStack()}
     *
     * @return A copy of the right input item.
     */
    public ItemStack getInputGem() {
        return this.gem;
    }

    /**
     * Fired when {@link SocketingRecipe} checks if a gem can be inserted into an item.<br>
     * <p>
     * This event {@linkplain HasResult has a result}.<br>
     * To change the result of this event, use {@link #setResult}.<br>
     * Results are interpreted in the following manner:
     * <ul>
     * <li>Allow - The check will succeed, and the gem will be accepted as socketable.</li>
     * <li>Default - The normal check will be used.</li>
     * <li>Deny - The check will fail, and the gem will not be accepted.</li>
     * </ul>
     * <br>
     * Note that forcibly allowing a socketing to occur will not work correctly if the gem has no bonus for that category.
     * <p>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @HasResult
    public static class CanSocket extends ItemSocketingEvent {

        public CanSocket(ItemStack inputStack, ItemStack inputGem) {
            super(inputStack, inputGem);
        }

    }

    /**
     * Fired when {@link SocketingRecipe} computes the result of a socketing operation.<br>
     * This event allows modification of the output item.
     * <p>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     *
     * @throws IllegalArgumentException if this event produces an empty output stack. Use {@link CanSocket} to prevent the operation.
     */
    public static class ModifyResult extends ItemSocketingEvent {
        protected ItemStack output;

        public ModifyResult(ItemStack stack, ItemStack gem, ItemStack output) {
            super(stack, gem);
            this.output = output;
        }

        /**
         * By default, the result is a copy of the input item with the input gem added in the first open socket.
         *
         * @return The (possibly event-modified) result item.
         */
        public ItemStack getOutput() {
            return this.output;
        }

        /**
         * Sets the output of the socketing operation.<br>
         *
         * @param output The new output.
         * @throws IllegalArgumentException if the stack is empty.
         */
        public void setOutput(ItemStack output) {
            if (output.isEmpty()) throw new IllegalArgumentException("Setting an empty output is undefined behavior");
            this.output = output;
        }

    }
}
