package shadows.apotheosis.core.attributeslib.api;

import java.util.List;
import java.util.ListIterator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * This event is used to add additional attribute tooltip lines without having to manually locate the inject point.
 * <p>
 * This event is fired on {@linkplain MinecraftForge#EVENT_BUS the main event bus}.<br>
 * This event is only fired on the {@linkplain Dist#CLIENT physical client}.
 */
public class AddAttributeTooltipsEvent extends PlayerEvent {

    protected final ItemStack stack;
    protected final List<Component> tooltip;
    protected final ListIterator<Component> attributeTooltipIterator;
    protected final TooltipFlag flag;

    public AddAttributeTooltipsEvent(ItemStack stack, @Nullable Player player, List<Component> tooltip, ListIterator<Component> attributeTooltipIterator, TooltipFlag flag) {
        super(player);
        this.stack = stack;
        this.tooltip = tooltip;
        this.attributeTooltipIterator = attributeTooltipIterator;
        this.flag = flag;
    }

    /**
     * Use to determine if the advanced information on item tooltips is being shown, toggled by F3+H.
     */
    public TooltipFlag getFlags() {
        return this.flag;
    }

    /**
     * The {@link ItemStack} with the tooltip.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * The {@link ItemStack}'s full tooltip.
     */
    public List<Component> getTooltip() {
        return this.tooltip;
    }

    /**
     * Returns an iterator pointed at the tail of the attribute tooltips.
     */
    public ListIterator<Component> getAttributeTooltipIterator() {
        return this.attributeTooltipIterator;
    }

    /**
     * This event is fired with a null player during startup when populating search trees for tooltips.
     */
    @Override
    @Nullable
    public Player getEntity() {
        return super.getEntity();
    }
}
