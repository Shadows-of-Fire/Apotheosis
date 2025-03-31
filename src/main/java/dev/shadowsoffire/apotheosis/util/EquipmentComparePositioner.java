package dev.shadowsoffire.apotheosis.util;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;

public class EquipmentComparePositioner {

    protected final int scnWidth;
    protected final int scnHeight;

    protected Rect compareTo;
    protected Rect equipped;

    public EquipmentComparePositioner(int scnWidth, int scnHeight) {
        this.scnWidth = scnWidth;
        this.scnHeight = scnHeight;
    }

    /**
     * Attempts to position two rectangles on the screen. If successful, this method returns {@code true} and the values of {@link #getComparePos()} and
     * {@link #getEquippedPos()} will return the updated positions of the rectangles.
     * <p>
     * This method may not change the size of the screen or either of the rectangles to fit. If no configuration exists where both rectangles can be on-screen, this
     * method returns false, and the values of the two positions are undefined.
     * <p>
     * The first rectangle will always be placed to the left of the second rectangle, with 30px of horizontal padding between them (if possible).
     * Both rectangles will always have the same y-level, regardless of their heights.
     *
     * @param equipPos The position of the first rectangle.
     * @param equipW   The width of the first rectangle.
     * @param equipH   The height of the first rectangle.
     * @param compPos  The position of the second rectangle.
     * @param compW    The width of the second rectangle.
     * @param compH    The height of the second rectangle.
     * @return True if the rectangles were placed, false otherwise.
     */
    public boolean position(Vector2ic equipPos, int equipW, int equipH, Vector2ic compPos, int compW, int compH) {
        this.equipped = rect(equipPos, equipW, equipH);
        this.compareTo = rect(compPos, compW, compH);

        if (canRenderNow()) {
            return true;
        }

        int padding = AdventureModuleClient.COMPARE_PADDING - 6; // We have to subtract 6 here to counter magic padding added by the tooltip border.

        // Calculate required horizontal space
        final int totalWidth = equipW + padding + compW;

        // Check if horizontal placement is possible.
        // Reduce the padding (down to zero) if necessary.
        if (totalWidth > scnWidth) {
            if (totalWidth - padding <= scnWidth) {
                padding = totalWidth - scnWidth;
            }
            else {
                return false;
            }
        }

        // Calculate maximum valid X for equipped (left rectangle)
        final int maxEquippedX = scnWidth - totalWidth - 6;
        int newEquippedX = Math.min(equipPos.x(), maxEquippedX);
        newEquippedX = Math.max(newEquippedX, 6);  // Minimum left margin

        // Calculate compare position (right of equipped with padding)
        int compareX = newEquippedX + equipW + padding;
        if (compareX + compW >= scnWidth) {
            // Try to shrink the padding as much as possible so we can still display the tooltip.
            // Anything below 2 and the tooltips start to bleed too hard.
            padding = Math.max(2, scnWidth - compareX - compW);
            compareX = newEquippedX + equipW + padding;
            if (compareX + compW >= scnWidth) {
                return false;
            }
        }

        // Calculate vertical placement (shared Y)
        final int maxHeight = Math.max(equipH, compH);
        final int maxY = scnHeight - maxHeight - 1;
        if (maxY < 1) {
            return false;
        }

        int sharedY = Math.min(Math.min(equipPos.y(), compPos.y()), maxY);
        sharedY = Math.max(sharedY, 1);  // Minimum top margin

        // Update positions with corrected naming
        this.equipped = rect(newEquippedX, sharedY, equipW, equipH);
        this.compareTo = rect(compareX, sharedY, compW, compH);

        return !this.equipped.overlaps(this.compareTo);
    }

    public Vector2i getComparePos() {
        return new Vector2i(this.compareTo.x, this.compareTo.y);
    }

    public Vector2i getEquippedPos() {
        return new Vector2i(this.equipped.x, this.equipped.y);
    }

    private Rect rect(Vector2ic pos, int width, int height) {
        return rect(pos.x(), pos.y(), width, height);
    }

    private Rect rect(int x, int y, int width, int height) {
        return new Rect(x, y, width, height, this.scnWidth, this.scnHeight);
    }

    /**
     * Returns true if the current positions are legal and placement is complete.
     */
    private boolean canRenderNow() {
        return this.compareTo.isOnScreen() && this.equipped.isOnScreen() && !this.compareTo.overlaps(this.equipped);
    }

    protected static record Rect(int x, int y, int width, int height, int scnWidth, int scnHeight) {

        boolean overlaps(Rect other) {
            return this.x() < other.x() + other.width &&
                other.x() < this.x() + this.width &&
                this.y() < other.y() + other.height &&
                other.y() < this.y() + this.height;
        }

        boolean isOnScreen() {
            return this.x() >= 0 && this.x() + this.width <= this.scnWidth
                && this.y() >= 0 && this.y() + this.height <= this.scnHeight;
        }

    }

}
