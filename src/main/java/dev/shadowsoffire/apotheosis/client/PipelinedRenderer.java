package dev.shadowsoffire.apotheosis.client;

import dev.shadowsoffire.apotheosis.Apoth;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public final class PipelinedRenderer {

    public static void ghostFakeItem(GuiGraphicsExtractor gfx, ItemStack stack, int x, int y, float alpha) {
        int tierIdx = nearestTier(alpha);
        float tierAlpha = AdventureModuleClient.GHOST_ALPHA_TIERS[tierIdx] / 255f;
        ItemStack copy = stack.copy();
        copy.set(Apoth.Components.RENDER_ALPHA, tierAlpha);
        gfx.fakeItem(copy, x, y);
    }

    public static void ghostFakeItem(GuiGraphicsExtractor gfx, ItemStack stack, int x, int y) {
        ghostFakeItem(gfx, stack, x, y, 0x44 / 255f);
    }

    public static void grayFakeItem(GuiGraphicsExtractor gfx, ItemStack stack, int x, int y) {
        ItemStack copy = stack.copy();
        copy.set(Apoth.Components.RENDER_ALPHA, Float.NaN);
        gfx.fakeItem(copy, x, y);
    }

    public static int nearestTier(float alpha) {
        int[] tiers = AdventureModuleClient.GHOST_ALPHA_TIERS;
        int target = Math.round(alpha * 255f);
        int best = 0;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < tiers.length; i++) {
            int d = Math.abs(tiers[i] - target);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    private PipelinedRenderer() {}
}
