package shadows.apotheosis.core.attributeslib.api;

import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class GatherSkippedAttributeTooltipsEvent extends PlayerEvent {

    protected final ItemStack stack;
    protected final Set<UUID> skips;
    protected final TooltipFlag flag;

    public GatherSkippedAttributeTooltipsEvent(ItemStack stack, @Nullable Player player, Set<UUID> skips, TooltipFlag flag) {
        super(player);
        this.stack = stack;
        this.skips = skips;
        this.flag = flag;
    }

    /**
     * Use to determine if the advanced information on item tooltips is being shown, toggled by F3+H.
     */
    public TooltipFlag getFlags() {
        return flag;
    }

    /**
     * The {@link ItemStack} with the tooltip.
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * Mark the UUID of a specific attribute modifier as skipped, causing it to not be displayed in the tooltip.
     */
    public void skipUUID(UUID id) {
        this.skips.add(id);
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
