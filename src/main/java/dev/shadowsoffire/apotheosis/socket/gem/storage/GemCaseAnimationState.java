package dev.shadowsoffire.apotheosis.socket.gem.storage;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Client-side only animation state tracker for gem cases.
 * Manages gem slot positions and smooth transitions between them.
 */
public class GemCaseAnimationState {

    // Duration intervals, in ticks
    private static final int SWITCH_INTERVAL_MIN = 80;
    private static final int SWITCH_INTERVAL_MAX = 240;
    private static final int ANIMATION_DURATION = 40;

    private final RandomSource random;
    private final int[] slotPositions; // Maps gem index to slot position
    private int ticksUntilNextSwitch;
    private int animationTicks;
    private int swappingIndex1 = -1;
    private int swappingIndex2 = -1;
    private boolean isAnimating = false;

    public GemCaseAnimationState(RandomSource random) {
        this.random = random;
        this.slotPositions = new int[16];

        for (int i = 0; i < 16; i++) {
            this.slotPositions[i] = i;
        }

        this.shuffleInitial();

        this.ticksUntilNextSwitch = this.getRandomSwitchInterval();
    }

    /**
     * Shuffles the initial positions so that multiple cases don't all start with the same layout.
     */
    private void shuffleInitial() {
        for (int i = 0; i < 16; i++) {
            int j = this.random.nextInt(16);
            int temp = this.slotPositions[i];
            this.slotPositions[i] = this.slotPositions[j];
            this.slotPositions[j] = temp;
        }
    }

    public void tick(int activeGemCount, boolean isPlayerNearby) {
        // Only animate when there are enough gems to make it really appear random.
        if (activeGemCount < 4) {
            return;
        }

        if (this.isAnimating) {
            this.animationTicks++;
            if (this.animationTicks >= ANIMATION_DURATION) {
                // Complete the swap
                this.completeSwap();
                this.isAnimating = false;
                this.ticksUntilNextSwitch = this.getRandomSwitchInterval();
            }
        }
        else if (isPlayerNearby) {
            this.ticksUntilNextSwitch--;
            if (this.ticksUntilNextSwitch <= 0) {
                // Start a new swap
                this.startRandomSwap(activeGemCount);
            }
        }
    }

    /**
     * Starts a random swap animation between two gem positions
     */
    private void startRandomSwap(int activeGemCount) {
        this.swappingIndex1 = this.random.nextInt(activeGemCount);
        this.swappingIndex2 = this.random.nextInt(activeGemCount);

        // Ensure we're swapping different gems
        if (this.swappingIndex1 == this.swappingIndex2) {
            this.swappingIndex2 = (this.swappingIndex2 + 1) % activeGemCount;
        }

        this.isAnimating = true;
        this.animationTicks = 0;
    }

    /**
     * Completes the current swap by exchanging the slot positions
     */
    private void completeSwap() {
        if (this.swappingIndex1 >= 0 && this.swappingIndex2 >= 0) {
            int temp = this.slotPositions[this.swappingIndex1];
            this.slotPositions[this.swappingIndex1] = this.slotPositions[this.swappingIndex2];
            this.slotPositions[this.swappingIndex2] = temp;
        }
    }

    /**
     * Gets the interpolated position for a gem at the given index.
     * Returns the base slot position plus any animation offset.
     */
    public PositionInfo getPosition(int gemIndex, float partialTicks) {
        int baseSlot = this.slotPositions[gemIndex];

        if (!this.isAnimating || (gemIndex != this.swappingIndex1 && gemIndex != this.swappingIndex2)) {
            // No animation for this gem
            return new PositionInfo(baseSlot, 0, 0);
        }

        // Calculate animation progress (0.0 to 1.0)
        float progress = (this.animationTicks + partialTicks) / (float) ANIMATION_DURATION;
        progress = Mth.clamp(progress, 0.0F, 1.0F);

        // Smooth easing function (ease-in-out)
        progress = smoothStep(progress);

        int targetSlot;
        if (gemIndex == this.swappingIndex1) {
            targetSlot = this.slotPositions[this.swappingIndex2];
        }
        else {
            targetSlot = this.slotPositions[this.swappingIndex1];
        }

        // Calculate offset from base position to target position
        int baseX = baseSlot % 4;
        int baseZ = baseSlot / 4;
        int targetX = targetSlot % 4;
        int targetZ = targetSlot / 4;

        float offsetX = (targetX - baseX) * progress;
        float offsetZ = (targetZ - baseZ) * progress;

        return new PositionInfo(baseSlot, offsetX, offsetZ);
    }

    /**
     * Smooth step interpolation function
     */
    private static float smoothStep(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    private int getRandomSwitchInterval() {
        return SWITCH_INTERVAL_MIN + this.random.nextInt(SWITCH_INTERVAL_MAX - SWITCH_INTERVAL_MIN);
    }

    /**
     * Contains position information for rendering a gem
     */
    public record PositionInfo(int baseSlot, float offsetX, float offsetZ) {}
}
