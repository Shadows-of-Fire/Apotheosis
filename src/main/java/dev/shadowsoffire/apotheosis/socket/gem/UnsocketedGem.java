package dev.shadowsoffire.apotheosis.socket.gem;

import java.util.Objects;
import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * Represents an unsocketed {@link GemItem}. This class retrieves the relevant data components into one convenient object.
 * TODO: Remove gemStack from here so this is an immutable record.
 */
public record UnsocketedGem(DynamicHolder<Gem> gem, Purity purity, ItemStack gemStack) implements GemView {

    /**
     * Creates a new {@link UnsocketedGem} from a given gem stack.
     */
    public static UnsocketedGem of(ItemStack gemStack) {
        DynamicHolder<Gem> gem = GemItem.getGem(gemStack);
        Purity purity = GemItem.getPurity(gemStack);

        if (gem.isBound()) {
            purity = Purity.max(gem.get().getMinPurity(), purity);
        }

        return new UnsocketedGem(gem, purity, gemStack.copy());
    }

    /**
     * Checks if the underlying {@link #gem} is bound.
     * <p>
     * Does not validate the {@link #purity}, since if the gem is bound, the purity will be automatically clamped.
     */
    public boolean isValid() {
        return this.gem.isBound();
    }

    /**
     * Checks if this gem is a {@link Purity#PERFECT perfect} gem, which can no longer be upgraded.
     */
    public boolean isPerfect() {
        return this.purity == Purity.PERFECT;
    }

    /**
     * @see Gem#canApplyTo(ItemStack, ItemStack, LootRarity)
     */
    public boolean canApplyTo(ItemStack stack) {
        return this.gem.get().canApplyTo(stack, this.gemStack, this.purity);
    }

    /**
     * @see Gem#addInformation(GemInstance, Consumer, AttributeTooltipContext)
     */
    public void addInformation(Consumer<Component> list, AttributeTooltipContext ctx) {
        this.gem().get().addInformation(this, list, ctx);
    }

    /**
     * Performs an equals comparison. Two unsocketed gems are equals under the following conditions:
     * <ol>
     * <li>Both are valid, and the underlying gem and purity are identical, <i>or</i></li>
     * <li>Both are invalid.</li>
     * </ol>
     */
    @Override
    public final boolean equals(Object arg0) {
        if (this == arg0) {
            return true;
        }

        if (arg0 instanceof UnsocketedGem other) {
            if (this.isValid()) {
                return other.isValid() && other.gem.equals(this.gem) && other.purity == this.purity;
            }
            else {
                return !other.isValid();
            }
        }

        return false;
    }

    /**
     * Computes the hash code for an unsocketed gem. The {@link #gemStack} does not impact the hash code.
     * <p>
     * An {@link #isValid() invalid} gem will always have a hash code of -1.
     */
    @Override
    public final int hashCode() {
        return this.isValid() ? Objects.hash(this.gem, this.purity) : -1;
    }

    /**
     * @deprecated Do not rely on retrieving the original ItemStack. This will be removed in a future update.
     */
    @Deprecated
    public ItemStack gemStack() {
        return this.gemStack;
    }
}
