package shadows.apotheosis.deadly;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nonnull;

@Cancelable
public class ItemUseEvent extends PlayerEvent {

    protected final UseOnContext ctx;
    protected InteractionResult cancellationResult = null;

    public ItemUseEvent(UseOnContext ctx) {
        super(ctx.getPlayer());
        this.ctx = ctx;
    }

    public ItemUseEvent(Player player, InteractionHand hand, BlockHitResult res) {
        this(new UseOnContext(player, hand, res));
    }

    public BlockPos getPos() {
        return this.ctx.getClickedPos();
    }

    public Direction getFace() {
        return this.ctx.getClickedFace();
    }

    public Vec3 getHitVec() {
        return this.ctx.getClickLocation();
    }

    public boolean isInside() {
        return this.ctx.isInside();
    }

    public UseOnContext getContext() {
        return this.ctx;
    }

    /**
     * @return The hand involved in this interaction. Will never be null.
     */
    @Nonnull
    public InteractionHand getHand() {
        return this.ctx.getHand();
    }

    /**
     * @return The ItemStack involved in this interaction, {@code ItemStack.EMPTY} if the hand was empty.
     */
    @Nonnull
    public ItemStack getItemStack() {
        return this.getPlayer().getItemInHand(this.getHand());
    }

    /**
     * @return Convenience method to get the world of this interaction.
     */
    public Level getWorld() {
        return this.getPlayer().getCommandSenderWorld();
    }

    /**
     * @return The InteractionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
     * method of the event. By default, this is {@link InteractionResult#PASS}, meaning cancelled events will cause
     * the client to keep trying more interactions until something works.
     */
    public InteractionResult getCancellationResult() {
        return this.cancellationResult;
    }

    /**
     * Set the EnumActionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
     * method of the event.
     * Note that this only has an effect on {@link RightClickBlock}, {@link RightClickItem}, {@link EntityInteract}, and {@link EntityInteractSpecific}.
     */
    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }
}